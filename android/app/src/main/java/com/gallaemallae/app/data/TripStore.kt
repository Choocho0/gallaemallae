package com.gallaemallae.app.data

import android.content.Context
import com.gallaemallae.app.model.FeedItem
import com.gallaemallae.app.model.FeedState
import com.gallaemallae.app.model.GameResult
import com.gallaemallae.app.model.Stats
import com.gallaemallae.app.model.Stop
import com.gallaemallae.app.model.Tab
import com.gallaemallae.app.model.Tone
import org.json.JSONArray
import org.json.JSONObject

/** 앱이 백그라운드로 가거나 종료돼도 진행 중인 여정을 이어서 열 수 있게 저장해요 */
data class SavedTrip(
    val stops: List<Stop>,
    val leg: Int,
    val tab: Tab,
    val feed: List<FeedItem>,
    val stats: Stats,
    val gameResults: List<GameResult>,
    val share: Boolean,
)

class TripStore(context: Context) {
    private val prefs = context.getSharedPreferences("trip_state", Context.MODE_PRIVATE)

    fun save(t: SavedTrip) {
        val o = JSONObject()
            .put("v", 2)
            .put("leg", t.leg)
            .put("tab", t.tab.name)
            .put("share", t.share)
            .put("stats", JSONObject().put("msgs", t.stats.msgs).put("wps", t.stats.waypoints).put("changes", t.stats.changes).put("games", t.stats.games))
            .put("stops", JSONArray(t.stops.map {
                JSONObject().put("id", it.id).put("poi", it.poiId).put("name", it.name).put("addr", it.addr).put("km", it.km).put("min", it.min)
            }))
            .put("feed", JSONArray(t.feed.take(30).map { f ->
                // 응답이 필요한 항목은 다시 열었을 때 만료로 표시
                val state = f.state ?: if (f.actions.isNotEmpty()) FeedState(false, "만료됨") else null
                JSONObject().put("id", f.id).put("icon", f.icon).put("tone", f.tone.name).put("title", f.title)
                    .put("text", f.text ?: "").put("time", f.time)
                    .apply { if (state != null) put("ok", state.ok).put("state", state.text) }
            }))
            .put("games", JSONArray(t.gameResults.map { JSONObject().put("text", it.text).put("time", it.time) }))
        prefs.edit().putString(KEY, o.toString()).apply()
    }

    fun load(): SavedTrip? = runCatching {
        val o = JSONObject(prefs.getString(KEY, null) ?: return null)
        val stops = o.getJSONArray("stops").objects().map {
            Stop(it.getString("id"), it.getString("poi"), it.getString("name"), it.getString("addr"), it.getDouble("km"), it.getInt("min"))
        }
        val feed = o.getJSONArray("feed").objects().map {
            FeedItem(
                it.getInt("id"), it.getString("icon"), Tone.valueOf(it.getString("tone")), it.getString("title"),
                it.getString("text").ifEmpty { null }, it.getString("time"),
                state = if (it.has("state")) FeedState(it.getBoolean("ok"), it.getString("state")) else null,
            )
        }
        val s = o.getJSONObject("stats")
        SavedTrip(
            stops = stops,
            leg = o.getInt("leg").coerceIn(0, stops.lastIndex),
            tab = Tab.valueOf(o.getString("tab")),
            feed = feed,
            stats = Stats(s.getInt("msgs"), s.getInt("wps"), s.getInt("changes"), s.getInt("games")),
            gameResults = o.getJSONArray("games").objects().map { GameResult(it.getString("text"), it.getString("time")) },
            share = o.getBoolean("share"),
        )
    }.getOrNull()

    fun clear() = prefs.edit().remove(KEY).apply()

    private fun JSONArray.objects(): List<JSONObject> = List(length()) { getJSONObject(it) }

    private companion object {
        const val KEY = "trip_v2"
    }
}
