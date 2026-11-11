package com.gallaemallae.app.platform

import com.gallaemallae.app.model.Poi
import com.gallaemallae.app.model.Waypoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class Leg(val name: String, val durationSec: Int, val distanceM: Int)

/** NaviHelperEventListener 중 앱이 쓰는 콜백만 추린 형태 */
interface NaviEvents {
    fun onDrivingInfo(destination: Leg, waypoints: List<Leg>) {}
    fun onWaypoints(list: List<Waypoint>) {}
    fun onWaypointArrived(name: String) {}
    fun onDestinationArrived(name: String) {}
    fun onNaviError(message: String) {}
}

/** 시스템 Navigation 앱과 연결하는 창구. 실차는 Pleos NaviHelper, 그 외에는 시뮬레이터 */
interface NaviGateway {
    val label: String
    fun setListener(listener: NaviEvents?)
    fun requestRoute(poi: Poi)
    fun cancelRoute()

    /** slot 0 = FIRST, 1 = SECOND (경유지는 최대 2곳) */
    fun addWaypoint(poi: Poi, slot: Int)
    fun removeWaypoint(slot: Int)
    fun waypoints(): List<Waypoint>
    fun release()
}

const val MAX_WAYPOINTS = 2

/**
 * NaviHelper가 없는 환경에서 쓰는 내비 시뮬레이터.
 * 실제 내비처럼 경로 진행·경유지 도착·목적지 도착 이벤트를 보내요.
 */
class SimulatedNavi(scope: CoroutineScope) : NaviGateway {
    override val label = "내비 시뮬레이터"

    private class SimWp(var slot: Int, val poi: Poi, val at: Float, var passed: Boolean = false)

    private var listener: NaviEvents? = null
    private var poi: Poi? = null
    private var running = false
    private var arrived = false
    private val wps = mutableListOf<SimWp>()
    private var restJob: Job? = null
    private val scope = scope

    var progress = 0f
        private set
    var speedMul = 1f

    /** 디버그 패널의 '정차' 스위치 (신호 대기 등) */
    var parkLock = false

    /** 주행 여부가 바뀌면 알려 줘요 (차량 서비스가 없을 때 주행 제한 대신 사용) */
    var onMovingChanged: ((Boolean) -> Unit)? = null
    private var lastMoving = false

    init {
        scope.launch {
            var n = 0
            while (isActive) {
                delay(100)
                tick(0.1f)
                if (++n % 10 == 0) report()
                val m = moving()
                if (m != lastMoving) {
                    lastMoving = m
                    onMovingChanged?.invoke(m)
                }
            }
        }
    }

    fun moving() = poi != null && running && !parkLock

    private fun legSec(p: Poi) = (p.min * 0.5f).coerceIn(26f, 80f)

    override fun setListener(listener: NaviEvents?) {
        this.listener = listener
    }

    override fun requestRoute(poi: Poi) {
        restJob?.cancel()
        this.poi = poi
        progress = 0f
        arrived = false
        wps.clear()
        running = true
        report()
    }

    override fun cancelRoute() {
        restJob?.cancel()
        poi = null
        running = false
        wps.clear()
    }

    override fun addWaypoint(poi: Poi, slot: Int) {
        if (this.poi == null) return
        wps.removeAll { it.slot == slot && !it.passed }
        val at = (progress + 0.12f + slot * 0.1f).coerceAtMost(0.9f)
        wps += SimWp(slot, poi, at)
        resort()
        listener?.onWaypoints(waypoints())
    }

    override fun removeWaypoint(slot: Int) {
        wps.removeAll { it.slot == slot && !it.passed }
        resort()
        listener?.onWaypoints(waypoints())
    }

    private fun resort() {
        wps.filter { !it.passed }.sortedBy { it.at }.forEachIndexed { i, w -> w.slot = i }
    }

    override fun waypoints(): List<Waypoint> =
        wps.sortedBy { it.at }.map { Waypoint(it.slot, it.poi.id, it.poi.name, it.poi.extraMin, it.passed) }

    override fun release() {
        cancelRoute()
        listener = null
    }

    fun remainMin(): Double {
        val p = poi ?: return 0.0
        return (1 - progress).toDouble() * p.min + wps.filter { !it.passed }.sumOf { it.poi.extraMin }
    }

    private fun tick(dt: Float) {
        val p = poi ?: return
        if (!moving()) return
        progress = (progress + dt * speedMul / legSec(p)).coerceAtMost(1f)
        val wp = wps.filter { !it.passed && progress >= it.at }.minByOrNull { it.at }
        if (wp != null) {
            progress = wp.at
            wp.passed = true
            running = false
            listener?.onWaypointArrived(wp.poi.name)
            listener?.onWaypoints(waypoints())
            restJob = scope.launch {
                delay((12000 / sqrt(speedMul)).toLong())
                if (poi != null && !arrived) running = true
            }
        }
        if (progress >= 1f && !arrived) {
            arrived = true
            running = false
            listener?.onDestinationArrived(p.name)
        }
    }

    private fun report() {
        val p = poi ?: return
        val dest = Leg(p.name, (remainMin() * 60).roundToInt(), ((1 - progress) * p.km * 1000).roundToInt())
        val legs = wps.filter { !it.passed }.sortedBy { it.at }.map {
            val f = it.at - progress
            Leg(it.poi.name, (f * p.min * 60).roundToInt(), (f * p.km * 1000).roundToInt())
        }
        listener?.onDrivingInfo(dest, legs)
    }
}
