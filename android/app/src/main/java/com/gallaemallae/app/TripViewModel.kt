package com.gallaemallae.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gallaemallae.app.data.PlanParser
import com.gallaemallae.app.data.SampleData
import com.gallaemallae.app.data.SavedTrip
import com.gallaemallae.app.data.TripStore
import com.gallaemallae.app.model.Car
import com.gallaemallae.app.model.CarEta
import com.gallaemallae.app.model.CarPos
import com.gallaemallae.app.model.DialogBody
import com.gallaemallae.app.model.DialogSpec
import com.gallaemallae.app.model.DlgButton
import com.gallaemallae.app.model.FeedAction
import com.gallaemallae.app.model.FeedItem
import com.gallaemallae.app.model.FeedState
import com.gallaemallae.app.model.GameResult
import com.gallaemallae.app.model.GameState
import com.gallaemallae.app.model.LadderTrace
import com.gallaemallae.app.model.ParseStatus
import com.gallaemallae.app.model.ParsedItem
import com.gallaemallae.app.model.Screen
import com.gallaemallae.app.model.Stats
import com.gallaemallae.app.model.Stop
import com.gallaemallae.app.model.Tab
import com.gallaemallae.app.model.Tone
import com.gallaemallae.app.model.VoiceIntent
import com.gallaemallae.app.model.Waypoint
import com.gallaemallae.app.platform.AppEvents
import com.gallaemallae.app.platform.ConvoyBasis
import com.gallaemallae.app.platform.DrivingRestrictions
import com.gallaemallae.app.platform.Inbound
import com.gallaemallae.app.platform.Leg
import com.gallaemallae.app.platform.MAX_WAYPOINTS
import com.gallaemallae.app.platform.NaviEvents
import com.gallaemallae.app.platform.NaviGateway
import com.gallaemallae.app.platform.Notifier
import com.gallaemallae.app.platform.Outbound
import com.gallaemallae.app.platform.PleosNavi
import com.gallaemallae.app.platform.SafeLog
import com.gallaemallae.app.platform.SecureStore
import com.gallaemallae.app.platform.SimulatedNavi
import com.gallaemallae.app.platform.SimulatedTripServer
import com.gallaemallae.app.platform.Ux
import com.gallaemallae.app.platform.UxState
import com.gallaemallae.app.util.fmtMin
import com.gallaemallae.app.util.hm
import com.gallaemallae.app.util.limitTo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.hypot
import kotlin.random.Random

class TripViewModel(app: Application) : AndroidViewModel(app), NaviEvents {
    private val notifier = Notifier(app)
    private val store = TripStore(app)
    private val secure = SecureStore(app)
    val restrictions = DrivingRestrictions(app)

    /** 실차가 아니면 이 시뮬레이터로 동작 (NaviHelper 연결에 성공하면 교체) */
    val sim = SimulatedNavi(viewModelScope)
    private var navi: NaviGateway = sim
    var naviLabel by mutableStateOf(sim.label)
        private set
    private val server = SimulatedTripServer(viewModelScope) { basis() }

    // ---------- 공통
    var screen by mutableStateOf(Screen.HOME)
        private set
    var ux by mutableStateOf(UxState.PARKED)
        private set
    var profile by mutableStateOf(secure.profile)
        private set
    var saved by mutableStateOf<SavedTrip?>(null)
        private set
    var dialog by mutableStateOf<DialogSpec?>(null)
        private set
    var clock by mutableStateOf(hm())
        private set

    // ---------- 일정 입력
    var planText by mutableStateOf("")
    var parsing by mutableStateOf(false)
        private set
    val parsed = mutableStateListOf<ParsedItem>()

    // ---------- 초대
    val joined = mutableStateListOf("민준")

    // ---------- 여정
    val stops = mutableStateListOf<Stop>()
    var leg by mutableIntStateOf(0)
        private set
    var remainMin by mutableDoubleStateOf(0.0)
        private set
    var remainKm by mutableDoubleStateOf(0.0)
        private set
    var myProgress by mutableFloatStateOf(0f)
        private set
    var arrived by mutableStateOf(false)
        private set

    /** 안내 시작을 기다리는 중이면 첫 구간 여부, 아니면 null */
    var awaiting by mutableStateOf<Boolean?>(null)
        private set
    var waypoints by mutableStateOf(listOf<Waypoint>())
        private set
    val cars = mutableStateMapOf<String, CarPos>()
    var carsAt by mutableLongStateOf(0L)
        private set
    val feed = mutableStateListOf<FeedItem>()
    var unread by mutableIntStateOf(0)
        private set
    var tab by mutableStateOf(Tab.CARS)
        private set
    var game by mutableStateOf<GameState?>(null)
        private set
    val gameResults = mutableStateListOf<GameResult>()
    var stats by mutableStateOf(Stats())
        private set
    var share by mutableStateOf(true)
        private set
    var online by mutableStateOf(true)
        private set
    val queue = mutableStateListOf<Outbound>()

    // ---------- 디버그 패널
    var auto by mutableStateOf(true)
    var speedMul by mutableFloatStateOf(1f)
        private set
    var parkLock by mutableStateOf(false)
        private set
    var uxOverride by mutableStateOf<Boolean?>(null)
        private set
    var themeOverride by mutableStateOf<Boolean?>(null)

    private class PendingStop(val accept: () -> Unit, val decline: () -> Unit)

    private val pending = mutableMapOf<Int, PendingStop>()
    private var activePending: Int? = null
    private val fired = mutableSetOf<String>()
    private val timers = mutableListOf<Job>()
    private var routeAckLeg = -1
    private var routeAckFeed = -1
    private val routeAckCars = linkedSetOf<String>()
    private var feedSeq = 0
    private var stopSeq = 0
    private var gameJob: Job? = null
    private var saveJob: Job? = null
    private var visible = true

    init {
        if (secure.token == null) secure.token = "demo-" + UUID.randomUUID()
        restrictions.start()
        sim.setListener(this)
        sim.onMovingChanged = { restrictions.simulate(it) }
        server.connect(::onServer)
        viewModelScope.launch { restrictions.state.collect(::onUxRestrictionsChanged) }
        viewModelScope.launch { AppEvents.actions.collect { onFeedAction(it.feedId, it.actionId) } }
        viewModelScope.launch {
            while (true) {
                delay(15_000)
                if (visible) clock = hm()
            }
        }
        PleosNavi.connect(app, viewModelScope) { gw ->
            navi = gw
            gw.setListener(this)
            naviLabel = gw.label
        }
        saved = store.load()
    }

    override fun onCleared() {
        navi.release()
        restrictions.stop()
        server.stop()
    }

    // =====================================================
    // 생명주기 · 주행 제한
    // =====================================================
    /** 앱 창이 다시 보이면 최신 상태로 갱신 */
    fun onAppStart() {
        visible = true
        clock = hm()
        if (screen == Screen.HOME) saved = store.load()
    }

    /** 앱 창이 가려지면 저장 */
    fun onAppStop() {
        visible = false
        saveNow()
    }

    private fun onUxRestrictionsChanged(s: UxState) {
        ux = s
        if (!s.driving) return
        if (dialog?.parkedOnly == true) dialog = null
        if (gameJob?.isActive == true) {
            gameJob?.cancel()
            game = null
            toast("주행이 시작돼 게임을 멈췄어요")
        }
    }

    val limitText get() = ux.has(Ux.LIMIT_STRING_LENGTH)

    fun lim(s: String, n: Int = 26) = s.limitTo(limitText, n)

    /** 정차 중에만 쓸 수 있는 화면 (일정 입력, 초대, 동의) */
    val needsParked get() = ux.driving && screen in setOf(Screen.HOME, Screen.PLAN, Screen.INVITE, Screen.CONSENT)

    // =====================================================
    // 공통 유틸
    // =====================================================
    private fun later(ms: Long, block: () -> Unit) {
        timers += viewModelScope.launch {
            delay(ms)
            block()
        }
    }

    private fun clearTimers() {
        timers.forEach { it.cancel() }
        timers.clear()
    }

    fun toast(text: String) = notifier.toast(text)

    private fun showDialog(d: DialogSpec) {
        dialog = d
    }

    fun closeDialog() {
        dialog = null
    }

    fun onDialogPrimary() {
        val d = dialog ?: return
        dialog = null
        d.primary?.onClick?.invoke()
    }

    fun onDialogSecondary() {
        val d = dialog ?: return
        dialog = null
        d.secondary?.onClick?.invoke()
    }

    /** Gleo AI 음성 명령 (응/아니/다음 일정/늦어요) */
    fun onVoice(intent: VoiceIntent) {
        when (intent) {
            VoiceIntent.YES, VoiceIntent.NO -> {
                val d = dialog
                if (d != null) {
                    if (intent == VoiceIntent.YES) onDialogPrimary() else onDialogSecondary()
                    return
                }
                val p = activePending?.let { pending[it] }
                if (p != null) {
                    if (intent == VoiceIntent.YES) p.accept() else p.decline()
                    return
                }
                toast("응답할 요청이 없어요")
            }
            VoiceIntent.NEXT, VoiceIntent.LATE -> {
                if (screen != Screen.TRIP) {
                    toast("여정을 시작하면 쓸 수 있어요")
                    return
                }
                if (intent == VoiceIntent.NEXT) nextLegRequest() else sendQuick("조금 늦어요", viaVoice = true)
            }
        }
    }

    // =====================================================
    // 홈 · 프로필
    // =====================================================
    fun goHome() {
        clearTimers()
        dialog = null
        screen = Screen.HOME
        saved = store.load()
    }

    fun resume() {
        val s = saved ?: return
        openTrip(s)
    }

    fun profileDialog() = showDialog(
        DialogSpec(
            icon = "user", title = "프로필", desc = "공용 차량이면 내 프로필로 바꿔 주세요",
            body = DialogBody.Profiles(SampleData.PROFILES, profile),
            secondary = DlgButton("로그아웃", ::logout), primary = DlgButton("닫기"),
        ),
    )

    fun setProfileTo(p: String) {
        dialog = null
        if (p != profile) {
            // 다른 사람 프로필로 바뀌면 이전 사람의 여정 기록을 남기지 않아요
            store.clear()
            saved = null
        }
        profile = p
        secure.profile = p
        toast("${p} 프로필로 바꿨어요")
    }

    fun logout() {
        secure.clear()
        store.clear()
        saved = null
        profile = "게스트"
        toast("로그아웃했어요 · 저장된 여정을 지웠어요")
    }

    fun joinTripDialog() = showDialog(
        DialogSpec(
            icon = "link", title = "초대 코드로 참여", desc = "받은 코드를 확인해 주세요", parkedOnly = true,
            body = DialogBody.JoinCode(SampleData.INVITE_CODE),
            secondary = DlgButton("취소"),
            primary = DlgButton("참여하기") {
                stops.clear()
                stops.addAll(SampleData.presetStops())
                joined.clear()
                joined.addAll(listOf("민준") + SampleData.JOIN_SEQ)
                screen = Screen.INVITE
                toast("${SampleData.TRIP_NAME}에 참여했어요")
            },
        ),
    )

    // =====================================================
    // 일정 입력
    // =====================================================
    fun openPlan() {
        planText = ""
        parsed.clear()
        screen = Screen.PLAN
    }

    fun phoneQrDialog() = showDialog(
        DialogSpec(
            icon = "qr", title = "휴대폰으로 입력", desc = "QR을 찍으면 입력 페이지가 열려요", parkedOnly = true,
            body = DialogBody.Qr(11, "입력한 일정은 이 화면으로 바로 전송돼요", null),
            primary = DlgButton("닫기"),
        ),
    )

    fun pullFromPhone() {
        planText = SampleData.SAMPLE_PLAN
        toast("휴대폰에서 일정을 받았어요")
    }

    fun runParse() {
        if (planText.isBlank()) {
            toast("먼저 일정을 입력해 주세요")
            return
        }
        parsing = true
        parsed.clear()
        viewModelScope.launch {
            delay(1200)
            parsed.addAll(PlanParser.parse(planText))
            parsing = false
        }
    }

    fun pickCandidate(i: Int, ci: Int) {
        val p = parsed.getOrNull(i) ?: return
        val c = p.candidates.getOrNull(ci) ?: return
        parsed[i] = p.copy(status = ParseStatus.OK, name = c.name, place = c, candidates = emptyList(), ask = null)
        toast("${c.name}(으)로 정했어요")
    }

    val canInvite get() = parsed.isNotEmpty() && parsed.none { it.status == ParseStatus.CHECK }

    // =====================================================
    // 초대 · 위치 공유 동의
    // =====================================================
    fun openInvite() {
        stops.clear()
        stops.addAll(parsed.mapIndexed { i, p ->
            val pl = p.place!!
            Stop("s$i", pl.poiId, pl.name, pl.addr, pl.km, pl.min)
        })
        joined.clear()
        joined.add("민준")
        screen = Screen.INVITE
        SampleData.JOIN_SEQ.forEachIndexed { i, name -> later(900L + i * 650L) { if (name !in joined) joined.add(name) } }
    }

    fun backToPlan() {
        clearTimers()
        screen = Screen.PLAN
    }

    fun toInviteBack() {
        screen = Screen.INVITE
    }

    fun toConsent() {
        clearTimers()
        screen = Screen.CONSENT
    }

    fun consent(agree: Boolean) {
        share = agree
        openTrip(null)
        if (!agree) toast("위치 비공개로 시작해요 · 일행에게 ETA만 공유돼요")
    }

    // =====================================================
    // 여정
    // =====================================================
    val curStop: Stop? get() = stops.getOrNull(leg)
    val isLast get() = leg >= stops.lastIndex

    private fun openTrip(restore: SavedTrip?) {
        clearTimers()
        dialog = null
        screen = Screen.TRIP
        cars.clear()
        carsAt = 0
        waypoints = emptyList()
        arrived = false
        awaiting = null
        pending.clear()
        activePending = null
        fired.clear()
        game = null
        routeAckLeg = -1
        if (restore != null) {
            stops.clear()
            stops.addAll(restore.stops)
            leg = restore.leg
            tab = restore.tab
            feed.clear()
            feed.addAll(restore.feed)
            stats = restore.stats
            gameResults.clear()
            gameResults.addAll(restore.gameResults)
            share = restore.share
            feedSeq = feed.maxOfOrNull { it.id } ?: 0
        } else {
            leg = 0
            tab = Tab.CARS
            feed.clear()
            gameResults.clear()
            stats = Stats()
            pushFeed("users", Tone.CAR3, "일행 3대 · 9명 참여", "같은 일정을 함께 안내해요")
        }
        unread = 0
        server.start()
        if (restore != null) later(400) { startLeg(first = false, restored = true) } else later(500) { askStart(true) }
    }

    private fun askStart(first: Boolean) {
        val s = curStop ?: return
        awaiting = first
        remainMin = s.min.toDouble()
        remainKm = s.km
        myProgress = 0f
        val prevExists = leg > 0
        showDialog(
            DialogSpec(
                icon = "route",
                title = if (first) "1번 목적지로 안내할까요?" else "다음 목적지로 안내할까요?",
                desc = "일행 차량에도 같은 목적지를 보내요",
                body = DialogBody.Dest(leg, s, if (first || !prevExists) null else lastLegCars()),
                voice = true,
                secondary = DlgButton("나중에") { toast("준비되면 ‘안내 시작’을 눌러 주세요") },
                primary = DlgButton("안내 시작") { startLeg(first) },
            ),
        )
    }

    private fun lastLegCars(): List<CarEta> = SampleData.CARS.map { c ->
        val info = if (c.me) null else cars[SampleData.anonCar(c.id)]
        val done = info == null || info.progress >= 1f
        CarEta(c, done, info?.etaMin ?: 0.0)
    }

    private fun startLeg(first: Boolean, restored: Boolean = false) {
        val s = curStop ?: return
        awaiting = null
        arrived = false
        waypoints = emptyList()
        navi.requestRoute(SampleData.POI_DB[s.poiId] ?: return)
        val out = Outbound.Route(leg, s.poiId)
        val ok = server.send(out)
        val item = pushFeed("route", Tone.INFO, "${leg + 1}번 ${s.name}", if (ok) "일행 차량 확인 중…" else "연결되면 일행 차량에 보내요")
        routeAckLeg = leg
        routeAckFeed = item.id
        routeAckCars.clear()
        if (!ok) queue.add(out)
        toast(
            when {
                restored -> "현재 목적지를 다시 안내해요"
                first -> "내비 안내를 시작했어요"
                else -> "${s.name}(으)로 안내해요"
            },
        )
        save()
    }

    fun nextLegRequest() {
        awaiting?.let {
            askStart(it)
            return
        }
        if (arrived) {
            if (isLast) {
                finishTrip()
                return
            }
            leg++
            askStart(false)
            return
        }
        if (isLast) {
            toast("마지막 목적지로 가고 있어요")
            return
        }
        val next = stops[leg + 1]
        showDialog(
            DialogSpec(
                icon = "next", title = "다음 일정으로 넘어갈까요?",
                desc = "아직 도착 전이에요. 일행 모두 ‘${next.name}’(으)로 바뀌어요", voice = true,
                secondary = DlgButton("취소"),
                primary = DlgButton("넘어가기") {
                    pushFeed("sync", Tone.BRAND, "${curStop?.name} 건너뜀", "호스트가 다음 일정으로 넘겼어요")
                    leg++
                    startLeg(false)
                },
            ),
        )
    }

    fun finishTrip() {
        dialog = null
        navi.cancelRoute()
        server.stop()
        clearTimers()
        store.clear()
        saved = null
        share = false
        notifier.cancelAll()
        screen = Screen.SUMMARY
        toast("여정이 끝나 위치 공유를 껐어요")
    }

    // ---------- NaviHelperEventListener
    override fun onDrivingInfo(destination: Leg, waypoints: List<Leg>) {
        val s = curStop ?: return
        if (screen != Screen.TRIP || awaiting != null) return
        remainMin = destination.durationSec / 60.0
        remainKm = destination.distanceM / 1000.0
        myProgress = (1 - remainKm / s.km).toFloat().coerceIn(0f, 1f)
        autoScenario()
    }

    override fun onWaypoints(list: List<Waypoint>) {
        waypoints = list
    }

    override fun onWaypointArrived(name: String) {
        pushFeed("rest", Tone.WARNING, "$name 도착", "일행이 모이면 다시 출발해요")
        notifier.notify(Notifier.ID_INFO, "${name}에 도착했어요", "정차 중에는 게임을 할 수 있어요", timeoutMs = 3500)
    }

    override fun onDestinationArrived(name: String) {
        if (screen != Screen.TRIP) return
        val s = curStop ?: return
        arrived = true
        myProgress = 1f
        remainMin = 0.0
        remainKm = 0.0
        val last = isLast
        val next = stops.getOrNull(leg + 1)
        pushFeed("flag", Tone.CAR3, "${s.name} 도착", if (last) "마지막 목적지예요" else "다음 · ${next?.name}")
        notifier.notify(Notifier.ID_INFO, "${s.name}에 도착했어요", if (last) "마지막 목적지예요" else "다음 · ${next?.name}", timeoutMs = 3500)
        val legAt = leg
        later(1600) {
            if (leg != legAt || !arrived || screen != Screen.TRIP) return@later
            if (last) {
                showDialog(
                    DialogSpec(
                        icon = "flag", tone = Tone.POSITIVE, title = "모든 일정을 마쳤어요", desc = "일행 모두 수고하셨어요",
                        voice = true, secondary = DlgButton("닫기"), primary = DlgButton("여정 마치기", ::finishTrip),
                    ),
                )
            } else {
                leg++
                askStart(false)
            }
        }
    }

    override fun onNaviError(message: String) = toast("내비 오류 · $message")

    // ---------- 서버
    private fun basis(): ConvoyBasis? {
        val s = curStop ?: return null
        if (screen != Screen.TRIP) return null
        val p = if (awaiting != null) 0f else myProgress
        return ConvoyBasis(p, s.min, waypoints.filter { !it.passed }.sumOf { it.extraMin })
    }

    private fun onServer(msg: Inbound) {
        if (msg is Inbound.Cars) {
            cars.putAll(msg.cars)
            carsAt = msg.at
            return
        }
        if (screen != Screen.TRIP) return
        when (msg) {
            is Inbound.RouteAck -> {
                if (msg.leg != routeAckLeg) return
                val car = SampleData.CAR_BY_ANON[msg.car] ?: return
                routeAckCars += car.label
                updateFeed(routeAckFeed) { it.copy(text = "${routeAckCars.joinToString(" · ")} 내비 적용 완료") }
            }
            is Inbound.Msg -> {
                val car = SampleData.CAR_BY_ANON[msg.car] ?: return
                pushFeed("msg", car.tone, "${car.label} ${msg.name}", "“${msg.text}”")
                notifier.notify(Notifier.ID_MSG, "${car.label} ${msg.name}", lim("“${msg.text}”", 30), timeoutMs = 4000)
            }
            is Inbound.StopRequest -> incomingStop(msg)
            is Inbound.GameResult -> finishGame(msg.kind, msg.text, remote = true)
            Inbound.PlanChange -> {
                val a = leg + 1
                val b = leg + 2
                if (b > stops.lastIndex) return
                reorderStop(b, a, "호스트 모바일 웹")
                notifier.notify(Notifier.ID_INFO, "일정 순서가 바뀌었어요", "다음 · ${stops[a].name}", timeoutMs = 4000)
            }
            else -> Unit
        }
    }

    private fun autoScenario() {
        if (!auto) return
        fun once(key: String, cond: Boolean, block: () -> Unit) {
            if (cond && fired.add(key)) block()
        }
        once("msg0", leg == 0 && myProgress > 0.1f) {
            server.deliver(Inbound.Msg(SampleData.anonCar(3), "지훈", "저희 기름 넣고 따라갈게요!"))
        }
        once("stop0", leg == 0 && myProgress > 0.24f) { demoStop() }
        once("msg1", leg == 1 && myProgress > 0.35f) {
            server.deliver(Inbound.Msg(SampleData.anonCar(2), "서연", "순두부집 웨이팅 걸어둘게요"))
        }
        once("game2", leg == 2 && myProgress > 0.3f) { demoLadderWeb() }
    }

    // ---------- 정차 요청 (경유지 최대 2곳)
    fun demoStop() {
        val highway = (curStop?.km ?: 0.0) > 50
        val pool = if (highway) listOf("rest1", "rest2", "rest3") else listOf("cvs1", "cvs2", "cvs3")
        val (carId, name) = if (stopSeq % 2 == 0) 2 to "서연" else 3 to "수아"
        val poiId = pool[stopSeq % 3]
        stopSeq++
        server.deliver(Inbound.StopRequest(SampleData.anonCar(carId), name, "화장실", poiId))
    }

    private fun incomingStop(msg: Inbound.StopRequest) {
        val car = SampleData.CAR_BY_ANON[msg.car] ?: return
        val poi = SampleData.POI_DB[msg.poiId] ?: return
        if (awaiting != null || arrived) return
        if (myProgress > 0.85f) {
            toast("목적지가 가까워 정차 요청을 보류했어요")
            return
        }
        val active = navi.waypoints().filter { !it.passed }
        val full = active.size >= MAX_WAYPOINTS
        val desc = if (full) "경유지가 가득 찼어요 (최대 2곳) · ${poi.name}" else "${msg.reason} · ${poi.name} · +${poi.extraMin}분"
        val acceptLabel = if (full) "첫 경유지와 바꾸기" else "수락"
        val item = pushFeed(
            "rest", car.tone, "${car.label} ${msg.name} · 정차 요청", desc,
            actions = listOf(FeedAction("거절", false, "decline"), FeedAction(acceptLabel, true, "accept")),
        )
        fun finish(state: FeedState) {
            updateFeed(item.id) { it.copy(state = state) }
            pending.remove(item.id)
            if (activePending == item.id) activePending = null
            notifier.cancel(Notifier.ID_STOP)
        }
        val accept = {
            val slot = if (full) {
                navi.removeWaypoint(active.first().slot)
                active.first().slot
            } else {
                (0 until MAX_WAYPOINTS).first { s -> active.none { it.slot == s } }
            }
            navi.addWaypoint(poi, slot)
            stats = stats.copy(waypoints = stats.waypoints + 1)
            finish(FeedState(true, if (full) "${active.first().name} 대신 ${poi.name} 경유" else "수락 · ${poi.name} 경유"))
            val out = Outbound.AddWaypoint(poi.id)
            if (!server.send(out)) queue.add(out)
            toast("경유지 추가 · ${poi.name}")
            later(2500) { server.deliver(Inbound.Msg(msg.car, msg.name, "감사해요!!")) }
        }
        val decline = {
            finish(FeedState(false, "거절 · “다음 휴게소에서 쉬어요” 전송"))
            val out = Outbound.Msg("다음 휴게소에서 쉬어요")
            if (!server.send(out)) queue.add(out)
            toast("${msg.name}님께 거절 메세지를 보냈어요")
        }
        pending[item.id] = PendingStop(accept, decline)
        activePending = item.id
        notifier.notify(
            Notifier.ID_STOP, "${car.label} ${msg.name} · 정차 요청", lim(desc, 34),
            actions = listOf(Notifier.Action("거절", "decline"), Notifier.Action(if (full) "바꾸기" else "수락", "accept")),
            feedId = item.id, timeoutMs = 15_000,
        )
    }

    fun onFeedAction(feedId: Int, actionId: String) {
        val p = pending[feedId] ?: return
        if (actionId == "accept") p.accept() else p.decline()
    }

    // ---------- 메세지 (오프라인이면 대기열)
    fun sendQuick(text: String, viaVoice: Boolean = false) {
        if (ux.driving && !viaVoice) {
            toast("주행 중에는 음성으로 보내 주세요")
            return
        }
        stats = stats.copy(msgs = stats.msgs + 1)
        val ok = server.send(Outbound.Msg(text))
        val item = pushFeed("msg", Tone.INFO, "나 → 일행 전체", "“$text”", state = if (ok) null else FeedState(false, "전송 대기 중"), countUnread = false)
        if (!ok) {
            queue.add(Outbound.Msg(text, item.id))
            toast("연결되면 보낼게요")
            return
        }
        toast("“$text” 전송")
        val c = SampleData.CARS[1 + Random.nextInt(2)]
        later(2600) { server.deliver(Inbound.Msg(SampleData.anonCar(c.id), c.driver, SampleData.REPLIES.random())) }
    }

    private fun flushQueue() {
        val q = queue.toList()
        queue.clear()
        q.forEach { m ->
            server.send(m)
            if (m is Outbound.Msg && m.feedId != null) updateFeed(m.feedId) { it.copy(state = FeedState(true, "전송됨")) }
        }
        if (q.isNotEmpty()) toast("대기 중이던 ${q.size}건을 보냈어요")
    }

    // ---------- 일정 변경 (정차 중에만)
    fun reorderStop(from: Int, to: Int, by: String = "나") {
        if (from == to || from !in stops.indices || to !in stops.indices) return
        val m = stops.removeAt(from)
        stops.add(to, m)
        stats = stats.copy(changes = stats.changes + 1)
        pushFeed("sync", Tone.CAR3, "일정 순서 변경", "$by · ${m.name} → ${to + 1}번째")
        if (by == "나") {
            server.send(Outbound.Reorder(from, to))
            toast("일정을 바꿨어요 · 일행 차량에 동기화")
        }
        save()
    }

    fun demoChange() = server.deliver(Inbound.PlanChange)

    fun demoArrive() {
        if (screen != Screen.TRIP || awaiting != null || arrived) return
        onDestinationArrived(curStop?.name ?: return)
    }

    fun demoMsg() = server.deliver(Inbound.Msg(SampleData.anonCar(2), "도윤", "다음 휴게소에서 커피 살게요!"))

    // ---------- 게임 (정차 중에만)
    fun runLadder() {
        if (game != null || ux.driving) return
        val names = listOf("민준", "서연", "지훈", "하은")
        val tones = listOf(Tone.CAR1, Tone.CAR2, Tone.CAR3, Tone.WARNING)
        val prizes = listOf("커피 쏘기", "통과", "간식 담당", "통과").shuffled()
        val n = 4
        val levels = 7
        val width = 760f
        val top = 60f
        val bottom = 330f
        val xs = List(n) { 95f + it * ((width - 190f) / (n - 1)) }
        fun ly(l: Int) = top + 20f + (l + 0.5f) * ((bottom - top - 40f) / levels)
        val rungs = List(levels) {
            val row = MutableList(n - 1) { false }
            for (i in 0 until n - 1) if (Random.nextFloat() < 0.5f && !(i > 0 && row[i - 1])) row[i] = true
            row.toList()
        }
        val traces = List(n) { start ->
            var c = start
            val pts = mutableListOf(Offset(xs[c], top))
            for (l in 0 until levels) {
                val y = ly(l)
                if (c > 0 && rungs[l][c - 1]) {
                    pts += Offset(xs[c], y); pts += Offset(xs[c - 1], y); c--
                } else if (c < n - 1 && rungs[l][c]) {
                    pts += Offset(xs[c], y); pts += Offset(xs[c + 1], y); c++
                }
            }
            pts += Offset(xs[c], bottom)
            var len = 0f
            for (i in 1 until pts.size) len += hypot(pts[i].x - pts[i - 1].x, pts[i].y - pts[i - 1].y)
            LadderTrace(pts, c, len)
        }
        var state = GameState.Ladder(names, tones, prizes, xs, rungs, traces, step = 0, revealed = 0)
        game = state
        gameJob = viewModelScope.launch {
            for (i in 0 until n) {
                state = state.copy(step = i)
                game = state
                delay(1000)
                state = state.copy(revealed = i + 1)
                game = state
            }
            val loser = traces.indexOfFirst { prizes[it.end] == "커피 쏘기" }
            val snack = traces.indexOfFirst { prizes[it.end] == "간식 담당" }
            finishGame("사다리타기", "${names[loser]} 커피 · ${names[snack]} 간식")
        }
    }

    fun runDraw() {
        if (game != null || ux.driving) return
        val names = SampleData.CARS.flatMap { it.members }
        val pick = Random.nextInt(names.size)
        gameJob = viewModelScope.launch {
            for (k in 0 until 8) {
                game = GameState.Draw(names, (pick + k + 1) % names.size, final = false)
                delay(160)
            }
            game = GameState.Draw(names, pick, final = true)
            finishGame("뽑기", "다음 구간 DJ는 ${names[pick]}")
        }
    }

    fun demoLadderWeb() {
        val p = listOf("민준", "서연", "지훈", "하은").shuffled()
        server.deliver(Inbound.GameResult("사다리타기", "${p[0]} 커피 · ${p[1]} 간식"))
    }

    private fun finishGame(kind: String, text: String, remote: Boolean = false) {
        stats = stats.copy(games = stats.games + 1)
        gameResults.add(0, GameResult("$kind · $text", hm()))
        pushFeed("trophy", Tone.WARNING, "$kind 결과" + if (remote) " · 동승자 웹" else "", text)
        if (!remote) later(3000) { game = null }
    }

    // ---------- 탭 · 초대
    fun selectTab(t: Tab) {
        tab = t
        if (t == Tab.FEED) unread = 0
        save()
    }

    fun showInvite() {
        if (ux.has(Ux.NO_SETUP)) {
            showDialog(
                DialogSpec(
                    icon = "link", title = "초대 코드", desc = "QR은 정차 중에 보여드려요",
                    body = DialogBody.CodeOnly(SampleData.INVITE_CODE), primary = DlgButton("닫기"),
                ),
            )
            return
        }
        showDialog(
            DialogSpec(
                icon = "qr", title = "일행 추가 초대", desc = "동승자는 QR, 다른 차량은 코드로 참여해요", parkedOnly = true,
                body = DialogBody.Qr(23, null, SampleData.INVITE_CODE), primary = DlgButton("닫기"),
            ),
        )
    }

    // ---------- 피드
    private fun pushFeed(
        icon: String,
        tone: Tone,
        title: String,
        text: String?,
        actions: List<FeedAction> = emptyList(),
        state: FeedState? = null,
        countUnread: Boolean = true,
    ): FeedItem {
        val f = FeedItem(++feedSeq, icon, tone, title, text, hm(), actions, state)
        feed.add(0, f)
        if (tab != Tab.FEED && countUnread) unread++
        save()
        return f
    }

    private fun updateFeed(id: Int, change: (FeedItem) -> FeedItem) {
        val i = feed.indexOfFirst { it.id == id }
        if (i >= 0) feed[i] = change(feed[i])
        save()
    }

    // ---------- 차량 정보
    fun carInfo(c: Car): CarPos {
        val s = curStop
        val now = System.currentTimeMillis()
        if (awaiting != null) return CarPos(0f, s?.min?.toDouble() ?: 0.0, now)
        if (c.me) return CarPos(myProgress, remainMin, now)
        return cars[SampleData.anonCar(c.id)] ?: CarPos(0f, s?.min?.toDouble() ?: 0.0, 0)
    }

    // ---------- 저장
    private fun snapshot() = SavedTrip(stops.toList(), leg, tab, feed.toList(), stats, gameResults.toList(), share)

    private fun save() {
        if (screen != Screen.TRIP) return
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(300)
            store.save(snapshot())
        }
    }

    private fun saveNow() {
        if (screen != Screen.TRIP) return
        saveJob?.cancel()
        store.save(snapshot())
    }

    // =====================================================
    // 디버그 패널
    // =====================================================
    fun setSpeed(m: Float) {
        speedMul = m
        sim.speedMul = m
    }

    fun setPark(v: Boolean) {
        parkLock = v
        sim.parkLock = v
    }

    fun setOnlineDemo(v: Boolean) {
        online = v
        server.setOnline(v)
        if (v) flushQueue()
        SafeLog.d("Server", if (v) "online" else "offline")
    }

    fun setUxOverrideDemo(v: Boolean?) {
        uxOverride = v
        restrictions.setOverride(v)
    }

    fun resetAll() {
        navi.cancelRoute()
        server.stop()
        store.clear()
        notifier.cancelAll()
        queue.clear()
        goHome()
    }
}
