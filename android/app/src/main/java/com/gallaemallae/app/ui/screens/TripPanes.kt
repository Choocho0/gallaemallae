package com.gallaemallae.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.gallaemallae.app.TripViewModel
import com.gallaemallae.app.data.SampleData
import com.gallaemallae.app.model.FeedItem
import com.gallaemallae.app.model.GameState
import com.gallaemallae.app.platform.Ux
import com.gallaemallae.app.ui.components.BounceColumn
import com.gallaemallae.app.ui.components.Btn
import com.gallaemallae.app.ui.components.BtnKind
import com.gallaemallae.app.ui.components.Card
import com.gallaemallae.app.ui.components.CarChip
import com.gallaemallae.app.ui.components.Ico
import com.gallaemallae.app.ui.components.IconBadge
import com.gallaemallae.app.ui.components.Press
import com.gallaemallae.app.ui.components.Row2
import com.gallaemallae.app.ui.components.Txt
import com.gallaemallae.app.ui.theme.AstaSans
import com.gallaemallae.app.ui.theme.CarColors
import com.gallaemallae.app.ui.theme.GIcons
import com.gallaemallae.app.ui.theme.Radius
import com.gallaemallae.app.ui.theme.Th
import com.gallaemallae.app.ui.theme.Type
import com.gallaemallae.app.ui.theme.color
import com.gallaemallae.app.util.fmtKm
import com.gallaemallae.app.util.fmtMin
import com.gallaemallae.app.util.hm
import kotlin.math.abs
import kotlin.math.roundToInt

// =====================================================
// 일행
// =====================================================
@Composable
fun CarsPane(vm: TripViewModel) {
    val c = Th.c
    val s = vm.curStop ?: return
    val myP = vm.carInfo(SampleData.CARS.first()).progress
    val stale = !vm.online && vm.carsAt > 0
    BounceColumn(Modifier.fillMaxSize(), 14.dp) {
        Card(Modifier.fillMaxWidth(), padding = PaddingValues(28.dp)) {
            Row2(Modifier.fillMaxWidth()) {
                Txt(
                    "일행 진행 상황" + if (stale) " · ${hm((vm.carsAt - System.currentTimeMillis()) / 60000.0)} 기준" else "",
                    Type.label, if (stale) c.warning else c.textSecondary, weight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1,
                )
                Txt(vm.lim(s.name, 14), Type.label, c.textSecondary, maxLines = 1)
            }
            ConvoyTrack(vm, myP)
        }
        SampleData.CARS.forEach { car ->
            val info = vm.carInfo(car)
            val arrived = info.progress >= 1f
            val late = !car.me && info.progress < myP - 0.05f
            val gap = (info.progress - myP) * s.km
            val sub = if (car.me) {
                car.members.joinToString(" · ") + if (vm.share) "" else " · 위치 비공개"
            } else {
                car.members.joinToString(" · ") + " · " + if (abs(gap) < 0.05) "내 차와 함께" else "${fmtKm(abs(gap))} ${if (gap > 0) "앞" else "뒤"}"
            }
            Card(Modifier.fillMaxWidth(), padding = PaddingValues(24.dp)) {
                Row2(Modifier.fillMaxWidth(), 20.dp) {
                    CarChip(car)
                    Column(Modifier.weight(1f)) {
                        Row2(gap = 12.dp) {
                            Txt("${car.label} · ${car.driver}", Type.bodyStrong, maxLines = 1)
                            if (car.me) {
                                Txt(
                                    "내 차", Type.caption, c.textOnInfo, weight = FontWeight.Bold,
                                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(c.info).padding(horizontal = 12.dp, vertical = 2.dp),
                                )
                            }
                        }
                        Txt(vm.lim(sub, 30), Type.caption, c.textTertiary, maxLines = 1)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Txt(
                            if (arrived) "도착" else hm(info.etaMin), Type.title,
                            when {
                                arrived -> c.positive
                                late -> c.warning
                                else -> c.textPrimary
                            },
                        )
                        Txt(if (arrived) "목적지" else "${fmtMin(info.etaMin)} 남음", Type.caption, c.textTertiary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConvoyTrack(vm: TripViewModel, myP: Float) {
    val c = Th.c
    BoxWithConstraints(Modifier.fillMaxWidth().height(84.dp).padding(top = 16.dp)) {
        val dot = 52.dp
        val usable = maxWidth - dot - 40.dp
        Box(Modifier.align(Alignment.CenterStart).fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)).background(c.surfaceTertiary))
        Box(Modifier.align(Alignment.CenterStart).fillMaxWidth(myP.coerceIn(0.02f, 1f)).height(14.dp).clip(RoundedCornerShape(7.dp)).background(c.info))
        Ico(GIcons.flag, 40.dp, c.textSecondary, Modifier.align(Alignment.CenterEnd))
        SampleData.CARS.forEach { car ->
            val p = vm.carInfo(car).progress
            Box(
                Modifier.align(Alignment.CenterStart).offset(x = usable * p).size(dot).clip(CircleShape).background(car.tone.color()),
                contentAlignment = Alignment.Center,
            ) {
                Txt("${car.id}", Type.caption, CarColors.ink, weight = FontWeight.ExtraBold)
            }
        }
    }
}

// =====================================================
// 알림 (주행 중에는 최근 3개만)
// =====================================================
@Composable
fun FeedPane(vm: TripViewModel) {
    val c = Th.c
    val limited = vm.ux.has(Ux.LIMIT_CONTENT)
    val list = if (limited) vm.feed.take(3) else vm.feed
    BounceColumn(Modifier.fillMaxSize(), 14.dp) {
        list.forEach { FeedRow(it, vm) }
        if (limited && vm.feed.size > 3) {
            Row2(Modifier.fillMaxWidth().padding(16.dp), 12.dp) {
                Ico(GIcons.lock, 32.dp, c.textTertiary)
                Txt("주행 중에는 최근 알림 3개만 보여요", Type.caption, c.textTertiary)
            }
        }
    }
}

@Composable
private fun FeedRow(f: FeedItem, vm: TripViewModel) {
    val c = Th.c
    val open = f.actions.isNotEmpty() && f.state == null
    Card(Modifier.fillMaxWidth(), color = if (open) c.surfaceSecondary else c.surfacePrimary, padding = PaddingValues(24.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            IconBadge(GIcons.of(f.icon), f.tone.color(), 72.dp, 38.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row2(Modifier.fillMaxWidth()) {
                    Txt(f.title, Type.bodyStrong, maxLines = 1, modifier = Modifier.weight(1f))
                    Txt(f.time, Type.caption, c.textTertiary)
                }
                f.text?.let { Txt(vm.lim(it, 40), Type.label, c.textSecondary, maxLines = 2) }
                if (open) {
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        f.actions.forEach { a ->
                            Btn(
                                a.label, { vm.onFeedAction(f.id, a.id) }, Modifier.weight(1f),
                                if (a.primary) BtnKind.PRIMARY else BtnKind.SECONDARY, height = 84.dp,
                            )
                        }
                    }
                }
                f.state?.let { Txt(it.text, Type.label, if (it.ok) c.positive else c.textTertiary, weight = FontWeight.Bold) }
            }
        }
    }
}

// =====================================================
// 일정 (정차 중에만 순서 변경)
// =====================================================
@Composable
fun PlanPane(vm: TripViewModel) {
    val c = Th.c
    val locked = vm.ux.has(Ux.NO_SETUP)
    val rowH = 132.dp
    val gap = 14.dp
    val stepPx = with(LocalDensity.current) { (rowH + gap).toPx() }
    var dragIdx by remember { mutableIntStateOf(-1) }
    var dragY by remember { mutableFloatStateOf(0f) }

    BounceColumn(Modifier.fillMaxSize(), gap) {
        Row2(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), 12.dp) {
            Ico(if (locked) GIcons.lock else GIcons.sync, 34.dp, if (locked) c.warning else c.textSecondary)
            Txt(
                if (locked) "주행 중에는 순서를 바꿀 수 없어요" else "손잡이를 끌어 순서를 바꿔요 · 일행에 동기화",
                Type.label, if (locked) c.warning else c.textSecondary,
            )
        }
        vm.stops.forEachIndexed { i, s ->
            val done = i < vm.leg
            val current = i == vm.leg
            val canDrag = !locked && i > vm.leg
            val dragging = i == dragIdx
            Row2(
                Modifier.fillMaxWidth().height(rowH)
                    .zIndex(if (dragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (dragging) dragY else 0f
                        shadowElevation = if (dragging) 24f else 0f
                    }
                    .clip(RoundedCornerShape(Radius.md))
                    .background(if (dragging) c.surfaceTertiary else c.surfacePrimary)
                    .padding(horizontal = 24.dp),
                20.dp,
            ) {
                Box(
                    Modifier.size(64.dp).clip(RoundedCornerShape(Radius.sm)).background(
                        when {
                            current -> c.info
                            done -> c.surfaceSecondary
                            else -> c.surfaceTertiary
                        },
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (done) Ico(GIcons.check, 36.dp, c.positive) else Txt("${i + 1}", Type.title, if (current) c.textOnInfo else c.textPrimary)
                }
                Column(Modifier.weight(1f)) {
                    Txt(s.name, Type.bodyStrong, if (done) c.textTertiary else c.textPrimary, maxLines = 1)
                    Txt("${vm.lim(s.addr)} · ${fmtKm(s.km)}", Type.caption, c.textTertiary, maxLines = 1)
                }
                if (canDrag) {
                    Row2(
                        Modifier.fillMaxHeight().clip(RoundedCornerShape(Radius.sm)).background(c.surfaceSecondary)
                            .pointerInput(i, vm.stops.size, vm.leg) {
                                detectDragGestures(
                                    onDragStart = {
                                        dragIdx = i
                                        dragY = 0f
                                    },
                                    onDragEnd = {
                                        val to = (i + (dragY / stepPx).roundToInt()).coerceIn(vm.leg + 1, vm.stops.lastIndex)
                                        dragIdx = -1
                                        dragY = 0f
                                        if (to != i) vm.reorderStop(i, to)
                                    },
                                    onDragCancel = {
                                        dragIdx = -1
                                        dragY = 0f
                                    },
                                ) { change, amount ->
                                    change.consume()
                                    dragY += amount.y
                                }
                            }
                            .padding(horizontal = 20.dp),
                        8.dp,
                    ) {
                        Ico(GIcons.drag, 36.dp, c.textSecondary)
                        Txt("순서", Type.caption, c.textSecondary)
                    }
                } else {
                    Txt(
                        when {
                            done -> "완료"
                            current -> "이동 중"
                            else -> ""
                        },
                        Type.label, if (current) c.info else c.textTertiary, weight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

// =====================================================
// 게임 (정차 중에만)
// =====================================================
@Composable
fun GamePane(vm: TripViewModel) {
    val c = Th.c
    BounceColumn(Modifier.fillMaxSize(), 16.dp) {
        val g = vm.game
        when {
            vm.ux.driving -> Card(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBadge(GIcons.lock, c.textSecondary, 96.dp, 52.dp)
                    Txt("게임은 정차 중에만 할 수 있어요", Type.title, align = TextAlign.Center)
                    Txt("동승자는 모바일 웹에서 함께해요", Type.body, c.textSecondary, align = TextAlign.Center)
                }
            }
            g is GameState.Ladder -> LadderView(g)
            g is GameState.Draw -> DrawView(g)
            else -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GameCard("사다리타기", "커피 내기", GIcons.ladder, c.warning, vm::runLadder, Modifier.weight(1f))
                GameCard("뽑기", "다음 구간 DJ", GIcons.dice, c.info, vm::runDraw, Modifier.weight(1f))
            }
        }
        if (vm.gameResults.isEmpty()) {
            Txt("아직 게임 결과가 없어요", Type.label, c.textTertiary, modifier = Modifier.padding(8.dp))
        }
        vm.gameResults.forEach { r ->
            Row2(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(c.surfacePrimary).padding(24.dp), 16.dp) {
                Ico(GIcons.trophy, 36.dp, c.warning)
                Txt(vm.lim(r.text, 30), Type.label, maxLines = 1, modifier = Modifier.weight(1f))
                Txt(r.time, Type.caption, c.textTertiary)
            }
        }
    }
}

@Composable
private fun GameCard(title: String, sub: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: androidx.compose.ui.graphics.Color, onClick: () -> Unit, modifier: Modifier) {
    val c = Th.c
    Press(onClick, modifier, RoundedCornerShape(Radius.lg), c.surfacePrimary, c.surfaceSecondary) {
        Column(Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(icon, tint, 96.dp, 52.dp)
            Txt(title, Type.title)
            Txt(sub, Type.label, c.textSecondary)
        }
    }
}

private fun partialPath(points: List<Offset>, length: Float): Path {
    val p = Path()
    if (points.isEmpty()) return p
    p.moveTo(points[0].x, points[0].y)
    var left = length
    for (i in 1 until points.size) {
        val a = points[i - 1]
        val b = points[i]
        val seg = (b - a).getDistance()
        if (left >= seg) {
            p.lineTo(b.x, b.y)
            left -= seg
        } else {
            val t = left / seg
            p.lineTo(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
            break
        }
    }
    return p
}

@Composable
private fun LadderView(g: GameState.Ladder) {
    val c = Th.c
    val anim = remember { Animatable(0f) }
    LaunchedEffect(g.step) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(900))
    }
    val measurer = rememberTextMeasurer()
    val tones = g.tones.map { it.color() }
    val top = 60f
    val bottom = 330f
    val levels = g.rungs.size
    fun ly(l: Int) = top + 20f + (l + 0.5f) * ((bottom - top - 40f) / levels)
    val line = c.surfaceTertiary
    val sub = c.textSecondary

    Card(Modifier.fillMaxWidth(), padding = PaddingValues(28.dp)) {
        Txt("사다리타기 · 커피 내기", Type.title)
        Canvas(Modifier.fillMaxWidth().aspectRatio(760f / 400f)) {
            val k = size.width / 760f
            scale(k, pivot = Offset.Zero) {
                g.columns.forEach { x -> drawLine(line, Offset(x, top), Offset(x, bottom), 6f) }
                g.rungs.forEachIndexed { l, row ->
                    row.forEachIndexed { i, on -> if (on) drawLine(line, Offset(g.columns[i], ly(l)), Offset(g.columns[i + 1], ly(l)), 6f) }
                }
                g.traces.forEachIndexed { i, tr ->
                    val f = when {
                        i < g.step -> 1f
                        i == g.step -> anim.value
                        else -> 0f
                    }
                    if (f > 0f) drawPath(partialPath(tr.points, tr.length * f), tones[i], style = Stroke(10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                g.names.forEachIndexed { i, n ->
                    val layout = measurer.measure(n, TextStyle(color = tones[i], fontSize = 30.sp, fontFamily = AstaSans, fontWeight = FontWeight.Bold))
                    drawText(layout, topLeft = Offset(g.columns[i] - layout.size.width / 2f, 40f - layout.size.height * 0.8f))
                }
                g.prizes.forEachIndexed { col, prize ->
                    val who = g.traces.withIndex().firstOrNull { (idx, tr) -> idx < g.revealed && tr.end == col }?.index
                    val text = if (who != null) "$prize · ${g.names[who]}" else prize
                    val layout = measurer.measure(text, TextStyle(color = if (who != null) tones[who] else sub, fontSize = 28.sp, fontFamily = AstaSans, fontWeight = FontWeight.Bold))
                    drawText(layout, topLeft = Offset(g.columns[col] - layout.size.width / 2f, 378f - layout.size.height * 0.8f))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DrawView(g: GameState.Draw) {
    val c = Th.c
    Card(Modifier.fillMaxWidth(), padding = PaddingValues(28.dp)) {
        Txt("뽑기 · 다음 구간 DJ", Type.title)
        Box(
            Modifier.fillMaxWidth().padding(vertical = 20.dp).height(220.dp).clip(RoundedCornerShape(Radius.md)).background(c.backgroundSecondary),
            contentAlignment = Alignment.Center,
        ) {
            Txt(g.names[g.current], Type.display.copy(fontSize = 96.sp, lineHeight = 104.sp), if (g.final) c.buttonPrimary else c.textPrimary)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            g.names.forEachIndexed { i, n ->
                val hit = i == g.current
                Txt(
                    n, Type.label, if (hit) c.textOnPrimary else c.textSecondary, weight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (hit) c.buttonPrimary else c.surfaceTertiary)
                        .padding(horizontal = 22.dp, vertical = 8.dp),
                )
            }
        }
    }
}
