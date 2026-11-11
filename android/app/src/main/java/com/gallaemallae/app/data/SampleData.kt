package com.gallaemallae.app.data

import com.gallaemallae.app.model.Car
import com.gallaemallae.app.model.KnownPlace
import com.gallaemallae.app.model.Place
import com.gallaemallae.app.model.Poi
import com.gallaemallae.app.model.Stop
import com.gallaemallae.app.model.Tone
import com.gallaemallae.app.platform.Privacy

object SampleData {
    const val TRIP_ID = "gn-2026-1111"
    const val TRIP_NAME = "강릉 1박 2일 여행"
    const val INVITE_CODE = "GLML-7K2Q"

    val SAMPLE_PLAN = """
        1. 안목해변 커피거리
        2. 초당 순두부 (점심)
        3. 경포대 산책
        4. 숙소 https://naver.me/stjohns_gn
    """.trimIndent()

    /** 지도 DB (내비가 poiId로 경로를 계산할 때 사용) */
    val POI_DB: MutableMap<String, Poi> = listOf(
        Poi("anmok", "안목해변 커피거리", "강원 강릉시 창해로14번길", 228.0, 160, 37.7719, 128.9476),
        Poi("chodang1", "초당할머니순두부", "강원 강릉시 초당순두부길 77", 4.2, 12, 37.7912, 128.9147),
        Poi("chodang2", "동화가든", "강원 강릉시 초당순두부길77번길 15", 4.5, 13, 37.7905, 128.9151),
        Poi("chodang3", "초당 공영주차장", "강원 강릉시 초당동 309", 4.0, 11, 37.7899, 128.9139),
        Poi("gyeongpo", "경포대", "강원 강릉시 경포로 365", 3.1, 9, 37.7951, 128.8966),
        Poi("stjohns", "세인트존스 호텔", "강원 강릉시 창해로 307", 2.4, 7, 37.7929, 128.9205),
        Poi("rest1", "평창휴게소", "영동고속도로 강릉 방향", 12.0, 8, 37.5612, 128.4231, extraMin = 15),
        Poi("rest2", "대관령휴게소", "영동고속도로 강릉 방향", 30.0, 20, 37.6880, 128.7570, extraMin = 15),
        Poi("rest3", "강릉 주유소", "강원 강릉시 경강로", 2.0, 3, 37.7558, 128.8910, extraMin = 8),
        Poi("cvs1", "GS25 초당점", "강원 강릉시 초당동", 0.6, 2, 37.7920, 128.9160, extraMin = 6),
        Poi("cvs2", "스타벅스 경포점", "강원 강릉시 경포로", 1.1, 3, 37.7940, 128.9010, extraMin = 10),
        Poi("cvs3", "초당 공영화장실", "강원 강릉시 초당동", 0.4, 1, 37.7895, 128.9130, extraMin = 5),
    ).associateBy { it.id }.toMutableMap()

    val KNOWN = listOf(
        KnownPlace(listOf("안목"), poiId = "anmok"),
        KnownPlace(
            listOf("순두부"),
            ask = "비슷한 장소가 3곳 있어요",
            candidates = listOf("chodang1" to "평점 4.6 · 대기 15분", "chodang2" to "대기 40분", "chodang3" to "주차 후 도보"),
        ),
        KnownPlace(listOf("경포"), poiId = "gyeongpo"),
        KnownPlace(listOf("stjohns", "세인트존스"), poiId = "stjohns", link = true),
    )

    fun place(poiId: String, note: String? = null): Place {
        val p = POI_DB.getValue(poiId)
        return Place(p.id, p.name, p.addr, p.km, p.min, note)
    }

    fun presetStops(): List<Stop> = listOf("anmok", "chodang1", "gyeongpo", "stjohns").mapIndexed { i, id ->
        val p = POI_DB.getValue(id)
        Stop("s$i", p.id, p.name, p.addr, p.km, p.min)
    }

    val CARS = listOf(
        Car(1, "1호차", "민준", listOf("민준", "지은", "하은"), Tone.CAR1, me = true, offset = 0f),
        Car(2, "2호차", "서연", listOf("서연", "도윤"), Tone.CAR2, offset = 0.035f),
        Car(3, "3호차", "지훈", listOf("지훈", "수아", "태오", "유나"), Tone.CAR3, offset = -0.085f),
    )

    /** 서버에는 차량 번호 대신 여정별 익명 ID만 오가요 */
    fun anonCar(id: Int): String = Privacy.anonId("$TRIP_ID:car:$id")

    val CAR_BY_ANON: Map<String, Car> by lazy { CARS.associateBy { anonCar(it.id) } }

    val JOIN_SEQ = listOf("지은", "하은", "서연", "도윤", "지훈", "수아", "태오", "유나")

    val QUICK = listOf("곧 출발해요", "휴게소 들러요", "먼저 가세요", "좋아요")
    val REPLIES = listOf("넵! 확인했어요", "좋아요", "알겠어요", "오케이~")
    val PROFILES = listOf("민준", "지은", "게스트")
}
