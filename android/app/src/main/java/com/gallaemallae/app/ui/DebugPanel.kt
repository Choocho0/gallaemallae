package com.gallaemallae.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gallaemallae.app.TripViewModel
import com.gallaemallae.app.model.VoiceIntent
import com.gallaemallae.app.ui.components.Press
import com.gallaemallae.app.ui.components.Txt
import com.gallaemallae.app.ui.theme.Type

private val PanelBg = Color(0xF0101318)
private val Ink = Color(0xFFE8EBF0)
private val Sub = Color(0xFF9AA2B0)
private val On = Color(0xFFFF8A3D)
private val Off = Color(0xFF2C323D)

/** 디버그 빌드에서만 보이는 시연 도구 (릴리스 빌드에는 포함되지 않음) */
@Composable
fun DebugPanel(vm: TripViewModel) {
    var open by remember { mutableStateOf(false) }
    Box(Modifier.padding(16.dp)) {
        if (!open) {
            Press({ open = true }, Modifier.size(88.dp), color = PanelBg, contentAlignment = Alignment.Center) {
                Txt("DEV", Type.caption, Ink, weight = FontWeight.ExtraBold)
            }
            return@Box
        }
        Column(
            Modifier.width(560.dp).fillMaxHeight().clip(RoundedCornerShape(24.dp)).background(PanelBg)
                .verticalScroll(rememberScrollState()).padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Chip("닫기", false) { open = false }
            Txt("내비: ${vm.naviLabel} · 주행 제한: ${vm.restrictions.source}", Type.caption, Sub)
            Section("속도") {
                listOf(1f, 4f, 10f).forEach { m -> Chip("${m.toInt()}x", vm.speedMul == m) { vm.setSpeed(m) } }
            }
            Section("상태") {
                Chip("자동 시나리오", vm.auto) { vm.auto = !vm.auto }
                Chip("신호 대기(정차)", vm.parkLock) { vm.setPark(!vm.parkLock) }
                Chip("온라인", vm.online) { vm.setOnlineDemo(!vm.online) }
            }
            Section("주행 제한") {
                Chip("실제 값", vm.uxOverride == null) { vm.setUxOverrideDemo(null) }
                Chip("주행", vm.uxOverride == true) { vm.setUxOverrideDemo(true) }
                Chip("정차", vm.uxOverride == false) { vm.setUxOverrideDemo(false) }
            }
            Section("테마") {
                Chip("시스템", vm.themeOverride == null) { vm.themeOverride = null }
                Chip("야간", vm.themeOverride == true) { vm.themeOverride = true }
                Chip("주간", vm.themeOverride == false) { vm.themeOverride = false }
            }
            Section("이벤트") {
                Chip("정차 요청", false, vm::demoStop)
                Chip("메세지", false, vm::demoMsg)
                Chip("웹 사다리", false, vm::demoLadderWeb)
                Chip("일정 변경", false, vm::demoChange)
                Chip("도착", false, vm::demoArrive)
                Chip("처음으로", false, vm::resetAll)
            }
            Section("Gleo 음성") {
                Chip("“응”", false) { vm.onVoice(VoiceIntent.YES) }
                Chip("“아니”", false) { vm.onVoice(VoiceIntent.NO) }
                Chip("“다음 일정”", false) { vm.onVoice(VoiceIntent.NEXT) }
                Chip("“늦어요”", false) { vm.onVoice(VoiceIntent.LATE) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Txt(title, Type.caption, Sub, weight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}

@Composable
private fun Chip(label: String, on: Boolean, onClick: () -> Unit) {
    Press(onClick, shape = RoundedCornerShape(999.dp), color = if (on) On else Off, pressedColor = Color(0xFF3A4250)) {
        Txt(label, Type.caption, if (on) Color(0xFF1A0D03) else Ink, weight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp))
    }
}
