package com.gallaemallae.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gallaemallae.app.TripViewModel
import com.gallaemallae.app.data.SampleData
import com.gallaemallae.app.model.Tab
import com.gallaemallae.app.ui.components.Btn
import com.gallaemallae.app.ui.components.Card
import com.gallaemallae.app.ui.components.Ico
import com.gallaemallae.app.ui.components.Press
import com.gallaemallae.app.ui.components.Row2
import com.gallaemallae.app.ui.components.Tag
import com.gallaemallae.app.ui.components.Txt
import com.gallaemallae.app.ui.theme.GIcons
import com.gallaemallae.app.ui.theme.Radius
import com.gallaemallae.app.ui.theme.Th
import com.gallaemallae.app.ui.theme.Type
import com.gallaemallae.app.util.fmtKm
import com.gallaemallae.app.util.fmtMin
import com.gallaemallae.app.util.hm

/**
 * 여정 화면. 지도·속도·TBT는 시스템 Navigation/Driving view가 보여 주므로 넣지 않아요.
 * 세로(1:1 분할)는 위아래로, 가로(2:0 전체)는 좌우로 배치.
 */
@Composable
fun TripScreen(vm: TripViewModel, landscape: Boolean) {
    val pad = PaddingValues(horizontal = 40.dp, vertical = 32.dp)
    if (landscape) {
        Row(Modifier.fillMaxSize().padding(pad), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Column(Modifier.weight(0.46f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                TripHead(vm)
                DestCard(vm, Modifier.fillMaxWidth().weight(1f))
                QuickChips(vm)
            }
            TabsArea(vm, Modifier.weight(0.54f).fillMaxHeight())
        }
    } else {
        Column(Modifier.fillMaxSize().padding(pad), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            TripHead(vm)
            DestCard(vm, Modifier.fillMaxWidth())
            QuickChips(vm)
            TabsArea(vm, Modifier.fillMaxWidth().weight(1f))
        }
    }
}

@Composable
private fun TripHead(vm: TripViewModel) {
    val c = Th.c
    Row2(Modifier.fillMaxWidth(), 16.dp) {
        Txt(SampleData.TRIP_NAME, Type.title, maxLines = 1, modifier = Modifier.weight(1f))
        when {
            !vm.online -> StateChip(GIcons.wifiOff, "연결 끊김", c.negative)
            !vm.share -> StateChip(GIcons.shield, "위치 비공개", c.textSecondary)
        }
        Press(vm::showInvite, color = c.buttonSecondary, pressedColor = c.buttonSecondaryPressed) {
            Row2(Modifier.padding(horizontal = 24.dp, vertical = 16.dp), 10.dp) {
                Ico(GIcons.qr, 36.dp)
                Txt("초대", Type.label, weight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StateChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row2(
        Modifier.clip(RoundedCornerShape(999.dp)).background(color.copy(alpha = 0.14f)).padding(horizontal = 20.dp, vertical = 10.dp),
        10.dp,
    ) {
        Ico(icon, 32.dp, color)
        Txt(text, Type.caption, color, weight = FontWeight.Bold)
    }
}

@Composable
private fun DestCard(vm: TripViewModel, modifier: Modifier) {
    val c = Th.c
    val s = vm.curStop ?: return
    val awaiting = vm.awaiting != null
    val remain = if (awaiting) s.min.toDouble() else vm.remainMin
    val km = if (awaiting) s.km else vm.remainKm
    val label = when {
        awaiting -> "안내 시작"
        vm.arrived -> if (vm.isLast) "여정 마치기" else "다음 일정 안내"
        vm.isLast -> "마지막 목적지"
        else -> "다음 일정으로"
    }
    Card(
        modifier.then(if (vm.arrived) Modifier.border(3.dp, c.positive, RoundedCornerShape(Radius.lg)) else Modifier),
        padding = PaddingValues(36.dp),
    ) {
        Row2(gap = 14.dp) {
            Tag("일정 ${vm.leg + 1}/${vm.stops.size}", c.info)
            Txt(
                when {
                    vm.arrived -> "도착했어요"
                    awaiting -> "안내 대기"
                    else -> "현재 목적지"
                },
                Type.label, if (vm.arrived) c.positive else c.textSecondary, weight = FontWeight.Bold,
            )
        }
        Txt(s.name, Type.headline, maxLines = 2, modifier = Modifier.padding(top = 14.dp, bottom = 24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Metric("도착 예정", if (vm.arrived) "도착" else hm(remain), Modifier.weight(1.2f), big = true)
            Metric("남은 시간", if (vm.arrived) "-" else fmtMin(remain), Modifier.weight(1f))
            Metric("남은 거리", if (vm.arrived) "-" else fmtKm(km), Modifier.weight(1f))
        }
        val wps = vm.waypoints.filter { !it.passed }
        if (wps.isNotEmpty()) {
            Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                wps.forEachIndexed { i, w ->
                    Row2(Modifier.fillMaxWidth(), 16.dp) {
                        Txt(
                            "경유 ${i + 1}", Type.caption, c.textOnWarning, weight = FontWeight.ExtraBold,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(c.warning).padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                        Txt(vm.lim(w.name), Type.body, maxLines = 1, modifier = Modifier.weight(1f))
                        Txt("+${w.extraMin}분", Type.label, c.textSecondary)
                    }
                }
            }
        }
        Box(Modifier.weight(1f, fill = false).padding(top = 24.dp))
        Btn(label, vm::nextLegRequest, Modifier.fillMaxWidth(), icon = GIcons.next, enabled = awaiting || vm.arrived || !vm.isLast)
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier, big: Boolean = false) {
    val c = Th.c
    Column(modifier) {
        Txt(label, Type.caption, c.textTertiary)
        Txt(value, if (big) Type.display else Type.title, maxLines = 1)
    }
}

@Composable
private fun QuickChips(vm: TripViewModel) {
    val c = Th.c
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (vm.ux.driving) {
            Row2(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(c.surfacePrimary).padding(horizontal = 30.dp, vertical = 20.dp),
                16.dp,
            ) {
                Ico(GIcons.lock, 36.dp, c.textTertiary)
                Column(Modifier.weight(1f)) {
                    Txt("주행 중에는 간편 메세지를 보낼 수 없어요", Type.label, weight = FontWeight.Bold)
                    Txt("“늦는다고 보내줘”처럼 음성으로 보내 주세요", Type.caption, c.textSecondary)
                }
            }
        } else Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SampleData.QUICK.forEach { q ->
                Press({ vm.sendQuick(q) }, shape = RoundedCornerShape(999.dp), color = c.surfacePrimary, pressedColor = c.surfaceTertiary) {
                    Txt(q, Type.label, weight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp))
                }
            }
        }
        if (vm.queue.isNotEmpty()) Txt("전송 대기 ${vm.queue.size}건 · 연결되면 보내요", Type.caption, c.warning)
    }
}

@Composable
private fun TabsArea(vm: TripViewModel, modifier: Modifier) {
    val c = Th.c
    Column(modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(c.surfacePrimary).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val tabs = listOf(Tab.CARS to ("일행" to GIcons.car), Tab.FEED to ("알림" to GIcons.bell), Tab.PLAN to ("일정" to GIcons.list), Tab.GAME to ("게임" to GIcons.game))
            tabs.forEach { (t, p) ->
                val on = vm.tab == t
                Press(
                    { vm.selectTab(t) }, Modifier.weight(1f), RoundedCornerShape(Radius.sm),
                    if (on) c.surfaceTertiary else Color.Transparent, c.surfaceSecondary, contentAlignment = Alignment.Center,
                ) {
                    Row2(Modifier.padding(vertical = 20.dp), 10.dp) {
                        Ico(p.second, 36.dp, if (on) c.textPrimary else c.textSecondary)
                        Txt(p.first, Type.label, if (on) c.textPrimary else c.textSecondary, weight = FontWeight.Bold)
                        if (t == Tab.FEED && vm.unread > 0) {
                            Box(Modifier.size(44.dp).clip(CircleShape).background(c.negative), contentAlignment = Alignment.Center) {
                                Txt("${vm.unread.coerceAtMost(9)}", Type.caption, c.textOnNegative, weight = FontWeight.ExtraBold)
                            }
                        }
                        if (t == Tab.GAME && vm.ux.driving) Ico(GIcons.lock, 30.dp, c.textTertiary)
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            when (vm.tab) {
                Tab.CARS -> CarsPane(vm)
                Tab.FEED -> FeedPane(vm)
                Tab.PLAN -> PlanPane(vm)
                Tab.GAME -> GamePane(vm)
            }
        }
    }
}