package com.gallaemallae.app.platform

import com.gallaemallae.app.data.SampleData
import com.gallaemallae.app.model.CarPos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** 서버 → 앱 */
sealed interface Inbound {
    data class Cars(val cars: Map<String, CarPos>, val at: Long) : Inbound
    data class RouteAck(val car: String, val leg: Int) : Inbound
    data class Msg(val car: String, val name: String, val text: String) : Inbound
    data class StopRequest(val car: String, val name: String, val reason: String, val poiId: String) : Inbound
    data class GameResult(val kind: String, val text: String) : Inbound
    data object PlanChange : Inbound
}

/** 앱 → 서버 */
sealed interface Outbound {
    data class Route(val leg: Int, val poiId: String) : Outbound
    data class AddWaypoint(val poiId: String) : Outbound
    data class Msg(val text: String, val feedId: Int? = null) : Outbound
    data class Reorder(val from: Int, val to: Int) : Outbound
}

/** 내 차 진행 상황 (다른 차 위치를 흉내 낼 때 기준) */
data class ConvoyBasis(val progress: Float, val legMin: Int, val pendingExtraMin: Int)

interface TripServer {
    val online: Boolean
    fun connect(handler: (Inbound) -> Unit)
    fun start()
    fun stop()

    /** 연결이 끊겨 있으면 false (호출한 쪽이 대기열에 보관) */
    fun send(msg: Outbound): Boolean
}

/** WebSocket 서버 시뮬레이션. 오프라인 동안 받은 메세지는 모았다가 다시 연결되면 전달해요 */
class SimulatedTripServer(
    private val scope: CoroutineScope,
    private val basis: () -> ConvoyBasis?,
) : TripServer {
    override var online = true
        private set
    private var handler: ((Inbound) -> Unit)? = null
    private val inbox = mutableListOf<Inbound>()
    private var pushJob: Job? = null

    override fun connect(handler: (Inbound) -> Unit) {
        this.handler = handler
    }

    override fun start() {
        pushJob?.cancel()
        pushJob = scope.launch {
            while (isActive) {
                pushCars()
                delay(2000)
            }
        }
    }

    override fun stop() {
        pushJob?.cancel()
        pushJob = null
    }

    fun deliver(msg: Inbound) {
        if (!online) {
            inbox += msg
            return
        }
        handler?.invoke(msg)
    }

    fun deliverLater(ms: Long, msg: Inbound) {
        scope.launch {
            delay(ms)
            deliver(msg)
        }
    }

    override fun send(msg: Outbound): Boolean {
        if (!online) return false
        if (msg is Outbound.Route) {
            deliverLater(1200, Inbound.RouteAck(SampleData.anonCar(2), msg.leg))
            deliverLater(2200, Inbound.RouteAck(SampleData.anonCar(3), msg.leg))
        }
        return true
    }

    fun setOnline(v: Boolean) {
        online = v
        if (v) {
            val q = inbox.toList()
            inbox.clear()
            q.forEach { handler?.invoke(it) }
            pushCars()
        }
    }

    private fun pushCars() {
        if (!online || pushJob == null) return
        val b = basis() ?: return
        val cars = SampleData.CARS.filter { !it.me }.associate { c ->
            val p = (b.progress + c.offset).coerceIn(0f, 1f)
            val eta = (1 - p).toDouble() * b.legMin + if (p < 1f) b.pendingExtraMin else 0
            SampleData.anonCar(c.id) to CarPos(p, eta, System.currentTimeMillis())
        }
        deliver(Inbound.Cars(cars, System.currentTimeMillis()))
    }
}
