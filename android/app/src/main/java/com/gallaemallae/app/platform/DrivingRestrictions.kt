package com.gallaemallae.app.platform

import android.car.Car
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** CarUxRestrictions 플래그 (android.car.drivingstate.CarUxRestrictions 값과 같음) */
object Ux {
    const val BASELINE = 0
    const val LIMIT_STRING_LENGTH = 4
    const val NO_KEYBOARD = 8
    const val NO_VIDEO = 16
    const val LIMIT_CONTENT = 32
    const val NO_SETUP = 64

    /** 국내 주행 중 적용되는 제한 (키보드는 허용) */
    const val KR_DRIVING = LIMIT_STRING_LENGTH or NO_VIDEO or LIMIT_CONTENT or NO_SETUP
}

data class UxState(val driving: Boolean, val flags: Int) {
    fun has(flag: Int) = flags and flag != 0

    companion object {
        val PARKED = UxState(false, Ux.BASELINE)
        val DRIVING = UxState(true, Ux.KR_DRIVING)
    }
}

/**
 * 차량에서는 CarUxRestrictionsManager 값을 그대로 따르고,
 * 차량 서비스가 없으면(일반 에뮬레이터·폰) 내비 시뮬레이터의 주행 여부로 대신해요.
 */
class DrivingRestrictions(private val context: Context) {
    private val _state = MutableStateFlow(UxState.PARKED)
    val state: StateFlow<UxState> = _state

    var source = "시뮬레이션"
        private set

    private var car: Car? = null
    private var manager: CarUxRestrictionsManager? = null
    private var carState = UxState.PARKED
    private var simState = UxState.PARKED
    private var override: Boolean? = null

    private val listener = CarUxRestrictionsManager.OnUxRestrictionsChangedListener { r -> onCar(r) }

    fun start() {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) return
        try {
            car = Car.createCar(context, null, Car.CAR_WAIT_TIMEOUT_DO_NOT_WAIT) { c, ready ->
                if (!ready) return@createCar
                val m = c.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as? CarUxRestrictionsManager ?: return@createCar
                manager = m
                source = "CarUxRestrictions"
                m.registerListener(listener)
                m.currentCarUxRestrictions?.let(::onCar)
            }
        } catch (t: Throwable) {
            SafeLog.w("Ux", "차량 서비스 연결 실패 · 시뮬레이션으로 동작", t)
        }
    }

    fun stop() {
        runCatching { manager?.unregisterListener() }
        runCatching { car?.disconnect() }
        manager = null
        car = null
    }

    private fun onCar(r: CarUxRestrictions) {
        carState = UxState(r.isRequiresDistractionOptimization, r.activeRestrictions)
        publish()
    }

    /** 차량 서비스가 없을 때만 쓰여요 */
    fun simulate(moving: Boolean) {
        simState = if (moving) UxState.DRIVING else UxState.PARKED
        publish()
    }

    /** 디버그 패널에서 주행/정차를 강제로 지정 (null이면 실제 값) */
    fun setOverride(driving: Boolean?) {
        override = driving
        publish()
    }

    private fun publish() {
        val base = if (manager != null) carState else simState
        val next = when (override) {
            true -> UxState.DRIVING
            false -> UxState.PARKED
            null -> base
        }
        if (next != _state.value) _state.value = next
    }
}
