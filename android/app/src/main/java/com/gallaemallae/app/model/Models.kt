package com.gallaemallae.app.model

import androidx.compose.ui.geometry.Offset

enum class Screen { HOME, PLAN, INVITE, CONSENT, TRIP, SUMMARY }

enum class Tab { CARS, FEED, PLAN, GAME }

/** 색은 화면 테마(주간/야간)에 맞춰 UI 쪽에서 토큰으로 풀어요 */
enum class Tone { INFO, POSITIVE, WARNING, NEGATIVE, BRAND, CAR1, CAR2, CAR3 }

/** 지도 DB의 장소. 내비 경로 요청(RouteInfo)에 그대로 쓰는 값 */
data class Poi(
    val id: String,
    val name: String,
    val addr: String,
    val km: Double,
    val min: Int,
    val lat: Double = 37.77,
    val lon: Double = 128.9,
    val extraMin: Int = 8,
)

data class Place(val poiId: String, val name: String, val addr: String, val km: Double, val min: Int, val note: String? = null)

data class Stop(val id: String, val poiId: String, val name: String, val addr: String, val km: Double, val min: Int)

data class KnownPlace(
    val keys: List<String>,
    val poiId: String? = null,
    val link: Boolean = false,
    val ask: String? = null,
    val candidates: List<Pair<String, String>> = emptyList(),
)

enum class ParseStatus { OK, LINK, CHECK }

data class ParsedItem(
    val id: String,
    val raw: String,
    val note: String?,
    val status: ParseStatus,
    val name: String,
    val place: Place? = null,
    val ask: String? = null,
    val candidates: List<Place> = emptyList(),
)

data class Car(
    val id: Int,
    val label: String,
    val driver: String,
    val members: List<String>,
    val tone: Tone,
    val me: Boolean = false,
    val offset: Float,
)

/** 다른 차량 위치 (서버가 익명 ID로 보내 줌) */
data class CarPos(val progress: Float, val etaMin: Double, val at: Long)

data class Waypoint(val slot: Int, val poiId: String, val name: String, val extraMin: Int, val passed: Boolean)

data class FeedAction(val label: String, val primary: Boolean, val id: String)

data class FeedState(val ok: Boolean, val text: String)

data class FeedItem(
    val id: Int,
    val icon: String,
    val tone: Tone,
    val title: String,
    val text: String?,
    val time: String,
    val actions: List<FeedAction> = emptyList(),
    val state: FeedState? = null,
)

data class DlgButton(val label: String, val onClick: () -> Unit = {})

data class CarEta(val car: Car, val arrived: Boolean, val etaMin: Double)

sealed interface DialogBody {
    data class Dest(val index: Int, val stop: Stop, val cars: List<CarEta>?) : DialogBody
    data class JoinCode(val code: String) : DialogBody
    data class Qr(val seed: Int, val caption: String?, val code: String?) : DialogBody
    data class CodeOnly(val code: String) : DialogBody
    data class Profiles(val list: List<String>, val current: String) : DialogBody
}

data class DialogSpec(
    val icon: String,
    val title: String,
    val desc: String? = null,
    val body: DialogBody? = null,
    val primary: DlgButton? = null,
    val secondary: DlgButton? = null,
    val voice: Boolean = false,
    /** 정차 중에만 보여도 되는 대화상자 (주행이 시작되면 닫음) */
    val parkedOnly: Boolean = false,
    val tone: Tone? = null,
    val key: Long = System.nanoTime(),
)

data class Stats(val msgs: Int = 0, val waypoints: Int = 0, val changes: Int = 0, val games: Int = 0)

data class LadderTrace(val points: List<Offset>, val end: Int, val length: Float)

sealed interface GameState {
    data class Ladder(
        val names: List<String>,
        val tones: List<Tone>,
        val prizes: List<String>,
        val columns: List<Float>,
        val rungs: List<List<Boolean>>,
        val traces: List<LadderTrace>,
        val step: Int,
        val revealed: Int,
    ) : GameState

    data class Draw(val names: List<String>, val current: Int, val final: Boolean) : GameState
}

data class GameResult(val text: String, val time: String)

enum class VoiceIntent { YES, NO, NEXT, LATE }
