package com.gallaemallae.app.data

import com.gallaemallae.app.model.ParseStatus
import com.gallaemallae.app.model.ParsedItem
import com.gallaemallae.app.model.Poi
import com.gallaemallae.app.platform.Privacy

object PlanParser {
    private val urlRe = Regex("https?://\\S+")
    private val noteRe = Regex("\\(([^)]+)\\)")
    private val numberingRe = Regex("^\\s*(\\d+[.)]|[-•*])\\s*")
    private val parenRe = Regex("\\([^)]*\\)")

    fun parse(text: String): List<ParsedItem> =
        text.lines().map { it.trim() }.filter { it.isNotEmpty() }.mapIndexed { i, raw ->
            val url = urlRe.find(raw)?.value
            val note = noteRe.find(raw)?.groupValues?.get(1)
            val clean = raw.replace(numberingRe, "").replace(urlRe, "").replace(parenRe, "").trim()
            val lower = raw.lowercase()
            val hit = SampleData.KNOWN.firstOrNull { k -> k.keys.any { lower.contains(it) } }
            val id = "p$i"
            when {
                hit != null && hit.candidates.isNotEmpty() -> ParsedItem(
                    id, raw, note, ParseStatus.CHECK, name = clean, ask = hit.ask,
                    candidates = hit.candidates.map { (poi, n) -> SampleData.place(poi, n) },
                )
                hit?.poiId != null -> {
                    val place = SampleData.place(hit.poiId)
                    ParsedItem(
                        id, raw, note, if (hit.link || url != null) ParseStatus.LINK else ParseStatus.OK,
                        name = place.name, place = place,
                    )
                }
                else -> {
                    val name = clean.ifEmpty { "지도 링크 장소" }
                    val poiId = "u" + Privacy.anonId(name)
                    SampleData.POI_DB[poiId] = Poi(poiId, name, "강원 강릉시", 5.2, 14)
                    ParsedItem(
                        id, raw, note, ParseStatus.CHECK, name = name, ask = "이 장소가 맞나요?",
                        candidates = listOf(SampleData.place(poiId, "가장 가까움")),
                    )
                }
            }
        }
}
