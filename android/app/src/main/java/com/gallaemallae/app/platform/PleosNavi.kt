package com.gallaemallae.app.platform

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.constants.WaypointIndex
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.DrivingInfo
import ai.pleos.playground.navi.data.RequestWaypointInfo
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.WaypointArrivedInfo
import ai.pleos.playground.navi.data.WaypointInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context
import com.gallaemallae.app.model.Poi
import com.gallaemallae.app.model.Waypoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Pleos 시스템 내비(NaviHelper) 연동 */
class PleosNaviGateway(private val helper: NaviHelper, private val scope: CoroutineScope) : NaviGateway {
    override val label = "Pleos 내비"

    private class Added(var slot: Int, val poi: Poi, var passed: Boolean = false)

    private val added = mutableListOf<Added>()
    private var listener: NaviEvents? = null

    /** SDK 콜백은 바인더 스레드에서 올 수 있어 메인으로 옮겨요 */
    private fun main(block: () -> Unit) {
        scope.launch(Dispatchers.Main) { block() }
    }

    private val sdkListener = object : NaviHelperEventListener {
        override fun onDrivingInfo(info: DrivingInfo) = main {
            val d = info.destination
            listener?.onDrivingInfo(
                Leg(d.name, d.duration, d.distance),
                info.waypoints.map { Leg(it.name, it.duration, it.distance) },
            )
        }

        override fun onWayPointInfo(list: List<WaypointInfo>) = main {
            // 내비에서 지워진 경유지는 지난 것으로 처리
            val names = list.map { it.name }.toSet()
            added.filter { !it.passed && it.poi.name !in names }.forEach { it.passed = true }
            listener?.onWaypoints(waypoints())
        }

        override fun onWayPointArrived(info: WaypointArrivedInfo) = main {
            added.firstOrNull { !it.passed && it.poi.name == info.name }?.passed = true
            listener?.onWaypointArrived(info.name)
            listener?.onWaypoints(waypoints())
        }

        override fun onDestinationArrived(info: DestinationArrivedInfo) = main {
            listener?.onDestinationArrived(info.name)
        }

        override fun onError(code: NaviErrorCode) = main {
            SafeLog.w("PleosNavi", "error $code")
            listener?.onNaviError(
                when (code) {
                    NaviErrorCode.CAUSE_LOW_BATTERY -> "배터리가 부족해 경로를 만들 수 없어요"
                    NaviErrorCode.CAUSE_SEARCH_ROUTE_OVER_DISTANCE -> "거리가 너무 멀어요"
                    NaviErrorCode.CAUSE_SEARCH_ROUTE_TOO_SHORT_DISTANCE -> "거리가 너무 가까워요"
                    else -> "경로를 찾지 못했어요"
                },
            )
        }
    }

    init {
        helper.addListener(sdkListener)
    }

    override fun setListener(listener: NaviEvents?) {
        this.listener = listener
    }

    override fun requestRoute(poi: Poi) {
        added.clear()
        SafeLog.d("PleosNavi", "requestRoute ${poi.id} ${poi.lat},${poi.lon}")
        helper.requestRoute(
            RouteInfo(
                latitude = poi.lat,
                longitude = poi.lon,
                displayLatitude = null,
                displayLongitude = null,
                poiName = poi.name,
                poiId = poi.id,
                address = poi.addr,
                poiSubId = "0",
            ),
        )
    }

    override fun cancelRoute() {
        added.clear()
        helper.cancelRoute()
    }

    override fun addWaypoint(poi: Poi, slot: Int) {
        added.removeAll { it.slot == slot && !it.passed }
        added += Added(slot, poi)
        helper.addWaypoint(
            RequestWaypointInfo(
                latitude = poi.lat,
                longitude = poi.lon,
                poiName = poi.name,
                waypointIndex = if (slot == 0) WaypointIndex.FIRST else WaypointIndex.SECOND,
                poiId = poi.id,
                address = poi.addr,
                poiSubId = "0",
            ),
        )
        listener?.onWaypoints(waypoints())
    }

    override fun removeWaypoint(slot: Int) {
        added.removeAll { it.slot == slot && !it.passed }
        helper.removeWaypoint(if (slot == 0) WaypointIndex.FIRST else WaypointIndex.SECOND)
        listener?.onWaypoints(waypoints())
    }

    override fun waypoints(): List<Waypoint> =
        added.map { Waypoint(it.slot, it.poi.id, it.poi.name, it.poi.extraMin, it.passed) }

    override fun release() {
        runCatching { helper.removeListener(sdkListener) }
        runCatching { helper.release() }
        listener = null
    }
}

object PleosNavi {
    private const val TIMEOUT_MS = 3000L

    /** 연결되면 onReady로 넘겨 주고, 실패·시간 초과면 시뮬레이터를 그대로 써요 */
    fun connect(context: Context, scope: CoroutineScope, onReady: (NaviGateway) -> Unit) {
        val helper = try {
            NaviHelper(context.applicationContext)
        } catch (t: Throwable) {
            SafeLog.w("PleosNavi", "NaviHelper 생성 실패", t)
            return
        }
        var done = false
        val timeout = scope.launch {
            delay(TIMEOUT_MS)
            if (!done) {
                done = true
                runCatching { helper.release() }
                SafeLog.w("PleosNavi", "연결 시간 초과 · 시뮬레이터 사용")
            }
        }
        try {
            helper.initialize(
                {
                    scope.launch(Dispatchers.Main) {
                        if (done) return@launch
                        done = true
                        timeout.cancel()
                        onReady(PleosNaviGateway(helper, scope))
                    }
                },
                { t ->
                    scope.launch(Dispatchers.Main) {
                        if (done) return@launch
                        done = true
                        timeout.cancel()
                        SafeLog.w("PleosNavi", "연결 실패 · 시뮬레이터 사용", t)
                    }
                },
            )
        } catch (t: Throwable) {
            done = true
            timeout.cancel()
            SafeLog.w("PleosNavi", "initialize 예외 · 시뮬레이터 사용", t)
        }
    }
}
