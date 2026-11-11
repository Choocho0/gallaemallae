package com.gallaemallae.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** 웹 프로토타입과 같은 24×24 선형 아이콘 */
object GIcons {
    private fun c(cx: Float, cy: Float, r: Float) = "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

    private fun rect(x: Float, y: Float, w: Float, h: Float, rx: Float = 0f): String {
        if (rx == 0f) return "M$x ${y}h${w}v${h}h${-w}z"
        val iw = w - 2 * rx
        val ih = h - 2 * rx
        return "M${x + rx} ${y}h${iw}a$rx $rx 0 0 1 $rx ${rx}v${ih}a$rx $rx 0 0 1 ${-rx} ${rx}" +
            "h${-iw}a$rx $rx 0 0 1 ${-rx} ${-rx}v${-ih}a$rx $rx 0 0 1 $rx ${-rx}z"
    }

    private fun build(name: String, vararg paths: String, fill: Boolean = false): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            paths.forEach {
                addPath(
                    pathData = addPathNodes(it),
                    fill = if (fill) SolidColor(Color.Black) else null,
                    stroke = if (fill) null else SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    val route = build("route", c(6f, 19f, 2f), c(18f, 5f, 2f), "M8 19h7a3.5 3.5 0 0 0 0-7H9a3.5 3.5 0 0 1 0-7h7")
    val car = build("car", "M4 17v-5l2.2-5.2A2 2 0 0 1 8 5.5h8a2 2 0 0 1 1.8 1.3L20 12v5", "M3 12h18v5H3z", "M6 17v2M18 17v2")
    val msg = build("msg", "M4 5h16v11H9l-5 4z", "M8 9.5h8M8 12.5h5")
    val link = build("link", "M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1", "M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1-1")
    val next = build("next", "M5 12h13M13 6l6 6-6 6")
    val back = build("back", "M19 12H6M11 6l-6 6 6 6")
    val phone = build("phone", rect(7f, 2.5f, 10f, 19f, 2.5f), "M11 18.5h2")
    val list = build("list", "M9 6h11M9 12h11M9 18h11", c(4.5f, 6f, 1f), c(4.5f, 12f, 1f), c(4.5f, 18f, 1f))
    val spark = build("spark", "M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z", "M19 16l.7 2 2 .7-2 .7-.7 2-.7-2-2-.7 2-.7z")
    val users = build("users", c(9f, 8f, 3.5f), "M2.5 20a6.5 6.5 0 0 1 13 0", "M16 4.5a3.5 3.5 0 0 1 0 7M18 14a6.5 6.5 0 0 1 3.5 6")
    val bell = build("bell", "M6 16v-5a6 6 0 0 1 12 0v5l2 2H4z", "M10 21h4")
    val game = build("game", rect(2.5f, 7f, 19f, 11f, 5f), "M7 10.5v4M5 12.5h4", c(15.5f, 11.5f, 1f), c(18f, 14f, 1f))
    val qr = build("qr", rect(3f, 3f, 7f, 7f), rect(14f, 3f, 7f, 7f), rect(3f, 14f, 7f, 7f), "M14 14h3v3h-3zM21 14v.01M14 21h.01M17 21h4v-4")
    val plus = build("plus", "M12 5v14M5 12h14")
    val minus = build("minus", "M5 12h14")
    val target = build("target", c(12f, 12f, 7f), c(12f, 12f, 2f), "M12 2v3M12 19v3M2 12h3M19 12h3")
    val sync = build("sync", "M20 11a8 8 0 0 0-14.5-4.5L4 8", "M4 4v4h4", "M4 13a8 8 0 0 0 14.5 4.5L20 16", "M20 20v-4h-4")
    val check = build("check", "M5 12.5l4.5 4.5L19 7.5")
    val close = build("close", "M6 6l12 12M18 6L6 18")
    val mic = build("mic", rect(9f, 3f, 6f, 11f, 3f), "M5 11a7 7 0 0 0 14 0M12 18v3")
    val left = build("left", "M17 21v-8a3 3 0 0 0-3-3H5", "M9 6l-4 4 4 4")
    val right = build("right", "M7 21v-8a3 3 0 0 1 3-3h9", "M15 6l4 4-4 4")
    val slightLeft = build("sleft", "M15 21v-6L7 7", "M7 13V7h6")
    val slightRight = build("sright", "M9 21v-6l8-8", "M11 7h6v6")
    val flag = build("flag", "M5 21V4M5 4h12l-2.5 4L17 12H5")
    val pin = build("pin", "M12 22s7-6.2 7-12a7 7 0 1 0-14 0c0 5.8 7 12 7 12z", c(12f, 10f, 2.6f))
    val rest = build("rest", "M8 3h8l5 5v8l-5 5H8l-5-5V8z", "M12 8v5M12 16v.01")
    val ladder = build("ladder", "M7 3v18M17 3v18M7 7h10M7 12h10M7 17h10")
    val dice = build("dice", rect(3.5f, 3.5f, 17f, 17f, 4f), c(8.5f, 8.5f, 1.2f), c(15.5f, 15.5f, 1.2f), c(12f, 12f, 1.2f))
    val trophy = build("trophy", "M8 4h8v5a4 4 0 0 1-8 0z", "M8 6H5a3 3 0 0 0 3 4M16 6h3a3 3 0 0 1-3 4M12 13v4M8 21h8M9 17h6")
    val drag = build(
        "drag",
        c(9f, 6f, 1.6f), c(15f, 6f, 1.6f), c(9f, 12f, 1.6f), c(15f, 12f, 1.6f), c(9f, 18f, 1.6f), c(15f, 18f, 1.6f),
        fill = true,
    )
    val user = build("user", c(12f, 8f, 4f), "M4 21a8 8 0 0 1 16 0")
    val lock = build("lock", rect(5f, 11f, 14f, 10f, 2.5f), "M8 11V8a4 4 0 0 1 8 0v3")
    val shield = build("shield", "M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z", "M8.5 12l2.5 2.5 4.5-5")
    val clock = build("clock", c(12f, 12f, 8.5f), "M12 7.5V12l3 2")
    val wifi = build("wifi", "M2.5 9a14 14 0 0 1 19 0M5.5 12.5a9.5 9.5 0 0 1 13 0M8.8 16a5 5 0 0 1 6.4 0", c(12f, 19.5f, 1f))
    val wifiOff = build(
        "wifioff",
        "M3 3l18 18M8.8 16a5 5 0 0 1 6.4 0M5.5 12.5a9.5 9.5 0 0 1 4-2.3M2.5 9a14 14 0 0 1 5-3M14.5 10.3a9.5 9.5 0 0 1 4 2.2M16.5 6a14 14 0 0 1 5 3",
        c(12f, 19.5f, 1f),
    )

    private val byName = mapOf(
        "route" to route, "car" to car, "msg" to msg, "link" to link, "next" to next, "back" to back,
        "phone" to phone, "list" to list, "spark" to spark, "users" to users, "bell" to bell, "game" to game,
        "qr" to qr, "plus" to plus, "sync" to sync, "check" to check, "close" to close, "mic" to mic,
        "flag" to flag, "pin" to pin, "rest" to rest, "ladder" to ladder, "dice" to dice, "trophy" to trophy,
        "drag" to drag, "user" to user, "lock" to lock, "shield" to shield, "clock" to clock, "wifi" to wifi,
        "wifioff" to wifiOff,
    )

    fun of(name: String): ImageVector = byName[name] ?: bell
}
