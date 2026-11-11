package com.gallaemallae.app.util

import java.time.LocalTime
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

fun hm(offsetMin: Double = 0.0): String {
    val t = LocalTime.now().plusSeconds((offsetMin * 60).toLong())
    return String.format(Locale.US, "%02d:%02d", t.hour, t.minute)
}

fun fmtMin(minutes: Double): String {
    val m = max(0, minutes.roundToInt())
    if (m < 60) return "${m}분"
    return if (m % 60 != 0) "${m / 60}시간 ${m % 60}분" else "${m / 60}시간"
}

fun fmtKm(km: Double): String = when {
    km < 1 -> "${max(10, (km * 100).roundToInt() * 10)}m"
    km < 10 -> String.format(Locale.US, "%.1fkm", km)
    else -> String.format(Locale.US, "%.0fkm", km)
}

/** 주행 중 LIMIT_STRING_LENGTH 제한이 걸리면 긴 문자열을 줄여요 */
fun String.limitTo(enabled: Boolean, n: Int = 26): String =
    if (enabled && length > n) take(n - 1) + "…" else this
