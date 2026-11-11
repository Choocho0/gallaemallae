package com.gallaemallae.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gallaemallae.app.TripViewModel
import com.gallaemallae.app.data.SampleData
import com.gallaemallae.app.model.ParseStatus
import com.gallaemallae.app.model.ParsedItem
import com.gallaemallae.app.platform.Ux
import com.gallaemallae.app.ui.components.BounceColumn
import com.gallaemallae.app.ui.components.Btn
import com.gallaemallae.app.ui.components.BtnKind
import com.gallaemallae.app.ui.components.Card
import com.gallaemallae.app.ui.components.CarChip
import com.gallaemallae.app.ui.components.CodeBox
import com.gallaemallae.app.ui.components.Gap
import com.gallaemallae.app.ui.components.Ico
import com.gallaemallae.app.ui.components.IconBadge
import com.gallaemallae.app.ui.components.Press
import com.gallaemallae.app.ui.components.QrCode
import com.gallaemallae.app.ui.components.Row2
import com.gallaemallae.app.ui.components.Tag
import com.gallaemallae.app.ui.components.Txt
import com.gallaemallae.app.ui.theme.GIcons
import com.gallaemallae.app.ui.theme.Radius
import com.gallaemallae.app.ui.theme.Th
import com.gallaemallae.app.ui.theme.Type

private val ScreenPad = PaddingValues(horizontal = 48.dp, vertical = 40.dp)

@Composable
fun ScreenHeader(title: String, sub: String?, onBack: (() -> Unit)?, trailing: @Composable () -> Unit = {}) {
    Row2(Modifier.fillMaxWidth(), 24.dp) {
        if (onBack != null) {
            Press(onBack, Modifier.size(88.dp), color = Th.c.buttonSecondary, pressedColor = Th.c.buttonSecondaryPressed, contentAlignment = Alignment.Center) {
                Ico(GIcons.back, 44.dp)
            }
        }
        Column(Modifier.weight(1f)) {
            Txt(title, Type.headline, maxLines = 1)
            sub?.let { Txt(it, Type.body, Th.c.textSecondary, maxLines = 2) }
        }
        trailing()
    }
}

@Composable
private fun Logo(size: androidx.compose.ui.unit.Dp) {
    Canvas(Modifier.size(size)) {
        scale(this.size.width / 120f, pivot = Offset.Zero) {
            drawRoundRect(Color(0xFF1D222C), cornerRadius = CornerRadius(32f))
            val left = Path().apply { moveTo(60f, 104f); lineTo(60f, 66f); cubicTo(60f, 50f, 44f, 46f, 36f, 30f) }
            val right = Path().apply { moveTo(60f, 66f); cubicTo(60f, 50f, 76f, 46f, 84f, 30f) }
            drawPath(left, Color(0xFFFF8A3D), style = Stroke(12f, cap = StrokeCap.Round))
            drawPath(right, Color(0xFF3D8BFF), style = Stroke(12f, cap = StrokeCap.Round))
            drawCircle(Color(0xFFFF9A40), 9f, Offset(36f, 26f))
            drawCircle(Color(0xFF3D8BFF), 9f, Offset(84f, 26f))
        }
    }
}

// =====================================================
// HOME
// =====================================================
@Composable
fun HomeScreen(vm: TripViewModel, landscape: Boolean) {
    val c = Th.c
    BounceColumn(Modifier.fillMaxSize(), 28.dp, ScreenPad) {
        Row2(Modifier.fillMaxWidth(), 24.dp) {
            Logo(104.dp)
            Column(Modifier.weight(1f)) {
                Txt("갈래말래", Type.headline)
                Txt("여러 차가 함께 가는 여행, 한 번에 안내해요", Type.body, c.textSecondary, maxLines = 2)
            }
            Press(vm::profileDialog, color = c.buttonSecondary, pressedColor = c.buttonSecondaryPressed, contentAlignment = Alignment.Center) {
                Row2(Modifier.padding(horizontal = 28.dp, vertical = 20.dp), 12.dp) {
                    Ico(GIcons.user, 38.dp)
                    Txt(vm.profile, Type.label, weight = FontWeight.Bold)
                }
            }
        }
        Gap(8.dp)
        vm.saved?.let { s ->
            HomeCard(
                GIcons.sync, c.positive, "진행 중인 여정 이어하기",
                "${SampleData.TRIP_NAME} · 일정 ${s.leg + 1}/${s.stops.size}", vm::resume, Modifier.fillMaxWidth(), highlight = true,
            )
        }
        val cards: @Composable (Modifier) -> Unit = { m ->
            HomeCard(GIcons.route, c.buttonPrimary, "새 여정 만들기", "일정을 붙여 넣으면 목적지를 순서대로 정리해요", vm::openPlan, m)
            HomeCard(GIcons.link, c.info, "초대 코드로 참여", "일행이 보낸 QR이나 코드로 함께 가요", vm::joinTripDialog, m)
        }
        if (landscape) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(28.dp)) { cards(Modifier.weight(1f)) }
        } else {
            cards(Modifier.fillMaxWidth())
        }
        Gap(8.dp)
        Row2 {
            Ico(GIcons.shield, 34.dp, c.textTertiary)
            Txt("위치는 여정 중에만 일행과 공유돼요", Type.caption, c.textTertiary)
        }
    }
}

@Composable
private fun HomeCard(
    icon: ImageVector,
    tint: Color,
    title: String,
    desc: String,
    onClick: () -> Unit,
    modifier: Modifier,
    highlight: Boolean = false,
) {
    val c = Th.c
    Press(
        onClick,
        modifier.then(if (highlight) Modifier.border(3.dp, c.positive, RoundedCornerShape(Radius.lg)) else Modifier),
        RoundedCornerShape(Radius.lg), c.surfacePrimary, c.surfaceSecondary,
    ) {
        Row2(Modifier.fillMaxWidth().padding(40.dp), 32.dp) {
            IconBadge(icon, tint, 112.dp, 60.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Txt(title, Type.title)
                Txt(desc, Type.body, c.textSecondary)
            }
            Ico(GIcons.next, 44.dp, c.textTertiary)
        }
    }
}

// =====================================================
// PLAN
// =====================================================
@Composable
fun PlanScreen(vm: TripViewModel, landscape: Boolean) {
    val c = Th.c
    Column(Modifier.fillMaxSize().padding(ScreenPad), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        ScreenHeader("일정 입력", "줄마다 한 곳씩 적어 주세요 · 지도 링크도 괜찮아요", vm::goHome)
        val input: @Composable (Modifier) -> Unit = { m -> PlanInput(vm, m) }
        val result: @Composable (Modifier) -> Unit = { m -> ParseResult(vm, m) }
        if (landscape) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                input(Modifier.weight(1f).fillMaxHeight())
                result(Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            input(Modifier.fillMaxWidth().height(460.dp))
            result(Modifier.fillMaxWidth().weight(1f))
        }
        Btn("일행 초대하기", vm::openInvite, Modifier.fillMaxWidth(), icon = GIcons.next, enabled = vm.canInvite)
        if (vm.ux.has(Ux.NO_KEYBOARD)) Txt("주행 중에는 키보드를 쓸 수 없어요", Type.caption, c.textTertiary)
    }
}

@Composable
private fun PlanInput(vm: TripViewModel, modifier: Modifier) {
    val c = Th.c
    Card(modifier, padding = PaddingValues(28.dp)) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            BasicTextField(
                vm.planText, { vm.planText = it }, Modifier.fillMaxSize(),
                enabled = !vm.ux.has(Ux.NO_KEYBOARD),
                textStyle = Type.body.copy(color = c.textPrimary),
                cursorBrush = SolidColor(c.buttonPrimary),
            )
            if (vm.planText.isEmpty()) {
                Txt("예) 1. 안목해변 커피거리\n2. 초당 순두부 (점심)\n3. 경포대", Type.body, c.textTertiary)
            }
        }
        Gap(20.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Btn("휴대폰 QR", vm::phoneQrDialog, Modifier.weight(1f), BtnKind.SECONDARY, GIcons.qr, iconEnd = false)
            Btn("휴대폰에서 받기", vm::pullFromPhone, Modifier.weight(1f), BtnKind.SECONDARY, GIcons.phone, iconEnd = false)
        }
        Gap(16.dp)
        Btn("AI로 정리하기", vm::runParse, Modifier.fillMaxWidth(), icon = GIcons.spark, iconEnd = false, enabled = !vm.parsing)
    }
}

@Composable
private fun ParseResult(vm: TripViewModel, modifier: Modifier) {
    val c = Th.c
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row2(Modifier.fillMaxWidth()) {
            Txt("정리된 일정", Type.title, modifier = Modifier.weight(1f))
            val pending = vm.parsed.count { it.status == ParseStatus.CHECK }
            if (vm.parsed.isNotEmpty()) {
                Txt(if (pending > 0) "확인 필요 ${pending}곳" else "${vm.parsed.size}곳", Type.label, if (pending > 0) c.warning else c.textSecondary, weight = FontWeight.Bold)
            }
        }
        when {
            vm.parsing -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Row2 {
                    Ico(GIcons.spark, 44.dp, c.buttonPrimary)
                    Txt("AI가 일정을 정리하고 있어요", Type.body, c.textSecondary)
                }
            }
            vm.parsed.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Txt("일정을 입력하면 목적지를 순서대로 정리해요", Type.body, c.textTertiary, align = TextAlign.Center)
            }
            else -> BounceColumn(Modifier.fillMaxWidth().weight(1f), 16.dp) {
                vm.parsed.forEachIndexed { i, p -> ParsedRow(i, p, vm) }
            }
        }
    }
}

@Composable
private fun ParsedRow(i: Int, p: ParsedItem, vm: TripViewModel) {
    val c = Th.c
    Card(Modifier.fillMaxWidth(), padding = PaddingValues(28.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(Radius.sm)).background(c.surfaceTertiary), contentAlignment = Alignment.Center) {
                Txt("${i + 1}", Type.title)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row2(gap = 14.dp) {
                    Txt(p.name, Type.bodyStrong, maxLines = 1, modifier = Modifier.weight(1f, fill = false))
                    when (p.status) {
                        ParseStatus.OK -> Tag("확인됨", c.positive)
                        ParseStatus.LINK -> Tag("링크 인식", c.info)
                        ParseStatus.CHECK -> Tag("확인 필요", c.warning)
                    }
                }
                if (p.status == ParseStatus.CHECK) {
                    p.ask?.let { Txt(it, Type.label, c.textSecondary) }
                    p.candidates.forEachIndexed { ci, cand ->
                        Press(
                            { vm.pickCandidate(i, ci) }, Modifier.fillMaxWidth(), RoundedCornerShape(Radius.sm),
                            c.surfaceSecondary, c.surfaceTertiary,
                        ) {
                            Row2(Modifier.padding(20.dp), 16.dp) {
                                Ico(GIcons.pin, 36.dp, c.buttonPrimary)
                                Txt(cand.name, Type.label, weight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
                                cand.note?.let { Txt(it, Type.caption, c.textTertiary, maxLines = 1) }
                            }
                        }
                    }
                } else {
                    Txt(p.place?.addr ?: "", Type.label, c.textTertiary, maxLines = 1)
                }
            }
        }
    }
}

// =====================================================
// INVITE
// =====================================================
@Composable
fun InviteScreen(vm: TripViewModel, landscape: Boolean) {
    val c = Th.c
    Column(Modifier.fillMaxSize().padding(ScreenPad), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        ScreenHeader("일행 초대", "동승자는 QR, 다른 차량은 코드로 참여해요", vm::backToPlan)
        val qr: @Composable (Modifier) -> Unit = { m ->
            Card(m) {
                Row2(Modifier.fillMaxWidth(), 32.dp) {
                    QrCode(23, Modifier.size(if (landscape) 360.dp else 280.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Txt(SampleData.TRIP_NAME, Type.title)
                        Txt("일정 ${vm.stops.size}곳", Type.body, c.textSecondary)
                        CodeBox(SampleData.INVITE_CODE, big = false)
                    }
                }
            }
        }
        val members: @Composable (Modifier) -> Unit = { m -> MemberGroups(vm, m) }
        if (landscape) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                qr(Modifier.weight(1f))
                members(Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            qr(Modifier.fillMaxWidth())
            members(Modifier.fillMaxWidth().weight(1f))
        }
        Btn("여정 시작", vm::toConsent, Modifier.fillMaxWidth(), icon = GIcons.next)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemberGroups(vm: TripViewModel, modifier: Modifier) {
    val c = Th.c
    val total = SampleData.CARS.sumOf { it.members.size }
    val joinedCars = SampleData.CARS.count { it.driver in vm.joined }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row2(Modifier.fillMaxWidth()) {
            Txt("참여 현황", Type.title, modifier = Modifier.weight(1f))
            Txt("차량 $joinedCars/3 · ${vm.joined.size}/${total}명", Type.label, c.textSecondary, weight = FontWeight.Bold)
        }
        BounceColumn(Modifier.fillMaxWidth().weight(1f), 16.dp) {
            SampleData.CARS.forEach { car ->
                val on = car.driver in vm.joined
                Card(Modifier.fillMaxWidth(), padding = PaddingValues(28.dp)) {
                    Row2(Modifier.fillMaxWidth()) {
                        CarChip(car)
                        Txt("${car.label} · ${car.driver}" + if (car.me) " (나)" else "", Type.bodyStrong, modifier = Modifier.weight(1f), maxLines = 1)
                        Tag(if (on) "참여함" else "대기 중", if (on) c.positive else c.textTertiary)
                    }
                    Gap(14.dp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        car.members.filter { it in vm.joined }.forEach {
                            Txt(it, Type.label, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(c.surfaceSecondary).padding(horizontal = 22.dp, vertical = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// 위치 공유 동의
// =====================================================
@Composable
fun ConsentScreen(vm: TripViewModel) {
    val c = Th.c
    BounceColumn(Modifier.fillMaxSize(), 24.dp, ScreenPad) {
        ScreenHeader("위치 공유 동의", null, vm::toInviteBack)
        Gap(12.dp)
        Card(Modifier.fillMaxWidth()) {
            Row2(gap = 28.dp) {
                IconBadge(GIcons.shield, c.positive, 112.dp, 60.dp)
                Column(Modifier.weight(1f)) {
                    Txt("일행에게 내 차 위치를 보여 줄까요?", Type.title)
                    Txt("도착 예정 시간과 거리를 함께 보며 이동해요", Type.body, c.textSecondary)
                }
            }
            Gap(28.dp)
            ConsentLine(GIcons.clock, "여정 중에만 공유하고, 여정이 끝나면 자동으로 꺼져요")
            ConsentLine(GIcons.users, "서버에는 차량 번호 대신 여정별 익명 ID만 보내요")
            ConsentLine(GIcons.lock, "공유하지 않아도 도착 예정 시간(ETA)은 일행에게 보여요")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Btn("공유 안 함", { vm.consent(false) }, Modifier.weight(1f), BtnKind.SECONDARY)
            Btn("동의하고 시작", { vm.consent(true) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ColumnScope.ConsentLine(icon: ImageVector, text: String) {
    Row2(Modifier.fillMaxWidth().padding(vertical = 10.dp), 20.dp) {
        Ico(icon, 40.dp, Th.c.textSecondary)
        Txt(text, Type.body, Th.c.textSecondary, modifier = Modifier.weight(1f))
    }
}

// =====================================================
// SUMMARY
// =====================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SummaryScreen(vm: TripViewModel, landscape: Boolean) {
    val c = Th.c
    val totalKm = vm.stops.sumOf { it.km }
    val stats = listOf(
        vm.stops.size to "방문 장소", totalKm.toInt() to "총 이동 km", 3 to "함께한 차량",
        vm.stats.waypoints to "정차 경유", vm.stats.msgs to "간편 메세지", vm.stats.changes + vm.stats.games to "일정 변경 · 게임",
    )
    BounceColumn(Modifier.fillMaxSize(), 28.dp, ScreenPad) {
        Row2(gap = 28.dp) {
            IconBadge(GIcons.flag, c.positive, 112.dp, 60.dp)
            Column {
                Txt("여정을 마쳤어요", Type.headline)
                Txt("${SampleData.TRIP_NAME} · 위치 공유가 꺼졌어요", Type.body, c.textSecondary)
            }
        }
        val cols = if (landscape) 6 else 3
        stats.chunked(cols).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { (v, l) ->
                    Card(Modifier.weight(1f), padding = PaddingValues(28.dp)) {
                        Txt("$v", Type.display)
                        Txt(l, Type.label, c.textSecondary, maxLines = 1)
                    }
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Txt("이동 경로", Type.title)
            Gap(16.dp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                (listOf("출발") + vm.stops.map { it.name }).forEachIndexed { i, n ->
                    if (i > 0) Txt("→", Type.body, c.textTertiary)
                    Txt(n, Type.body)
                }
            }
        }
        Btn("홈으로", vm::goHome, Modifier.fillMaxWidth(), icon = GIcons.next)
    }
}
