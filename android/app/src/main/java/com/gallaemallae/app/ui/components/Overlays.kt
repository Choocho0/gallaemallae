package com.gallaemallae.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gallaemallae.app.model.DialogBody
import com.gallaemallae.app.model.DialogSpec
import com.gallaemallae.app.ui.theme.GIcons
import com.gallaemallae.app.ui.theme.Radius
import com.gallaemallae.app.ui.theme.Th
import com.gallaemallae.app.ui.theme.Type
import com.gallaemallae.app.ui.theme.color
import com.gallaemallae.app.util.fmtMin

/** Pleos Dialog 형태 (앱 창 안에서만 뜸) */
@Composable
fun DialogHost(
    spec: DialogSpec?,
    lim: (String) -> String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    onProfile: (String) -> Unit,
) {
    AnimatedVisibility(spec != null, enter = fadeIn(), exit = fadeOut()) {
        val d = spec ?: return@AnimatedVisibility
        val c = Th.c
        Box(
            Modifier.fillMaxSize().background(c.dim)
                .clickable(remember { MutableInteractionSource() }, null) {},
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier.padding(40.dp).widthIn(max = 900.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.lg)).background(c.surfacePrimary).padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                val tint = d.tone?.color() ?: c.info
                IconBadge(GIcons.of(d.icon), tint, 96.dp, 52.dp)
                Txt(d.title, Type.title, align = TextAlign.Center)
                d.desc?.let { Txt(it, Type.body, c.textSecondary, align = TextAlign.Center) }
                when (val b = d.body) {
                    is DialogBody.Dest -> DestBody(b, lim)
                    is DialogBody.JoinCode -> CodeBox(b.code, big = true)
                    is DialogBody.CodeOnly -> CodeBox(b.code, big = true)
                    is DialogBody.Qr -> Row2(gap = 32.dp) {
                        QrCode(b.seed, Modifier.size(300.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            b.caption?.let { Txt(it, Type.body, c.textSecondary) }
                            b.code?.let { CodeBox(it, big = false) }
                        }
                    }
                    is DialogBody.Profiles -> Row2(gap = 16.dp) {
                        b.list.forEach { p ->
                            Btn(
                                p, { onProfile(p) }, Modifier.weight(1f), icon = GIcons.user, iconEnd = false,
                                kind = if (p == b.current) BtnKind.PRIMARY else BtnKind.SECONDARY,
                            )
                        }
                    }
                    null -> Unit
                }
                if (d.voice) {
                    Row2(Modifier.clip(RoundedCornerShape(999.dp)).background(c.surfaceSecondary).padding(horizontal = 28.dp, vertical = 12.dp), 12.dp) {
                        Ico(GIcons.mic, 34.dp, c.info)
                        Txt("“응” 또는 “아니”라고 말해 보세요", Type.caption, c.textSecondary)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    d.secondary?.let { Btn(it.label, onSecondary, Modifier.weight(1f), BtnKind.SECONDARY) }
                    d.primary?.let { Btn(it.label, onPrimary, Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun DestBody(b: DialogBody.Dest, lim: (String) -> String) {
    val c = Th.c
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row2(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(c.surfaceSecondary).padding(28.dp), 24.dp) {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(Radius.sm)).background(c.buttonPrimary), contentAlignment = Alignment.Center) {
                Txt("${b.index + 1}", Type.title, c.textOnPrimary)
            }
            Column(Modifier.weight(1f)) {
                Txt(b.stop.name, Type.bodyStrong, maxLines = 1)
                Txt(lim(b.stop.addr), Type.caption, c.textTertiary, maxLines = 1)
            }
            Txt(fmtMin(b.stop.min.toDouble()), Type.label, c.textSecondary, weight = FontWeight.Bold)
        }
        b.cars?.let { cars ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                cars.forEach { ce ->
                    Row2(Modifier.weight(1f).clip(RoundedCornerShape(Radius.sm)).background(c.surfaceSecondary).padding(16.dp), 12.dp) {
                        CarChip(ce.car, 48.dp)
                        Txt(ce.car.driver, Type.caption, maxLines = 1, modifier = Modifier.weight(1f))
                        Txt(
                            if (ce.arrived) "도착" else fmtMin(ce.etaMin), Type.caption,
                            if (ce.arrived) c.positive else c.textSecondary, weight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CodeBox(code: String, big: Boolean) {
    val c = Th.c
    Txt(
        code, if (big) Type.headline else Type.title, c.textPrimary, align = TextAlign.Center,
        modifier = Modifier.clip(RoundedCornerShape(Radius.md)).background(c.surfaceSecondary).padding(horizontal = 40.dp, vertical = 20.dp),
    )
}

/** 주행 중에는 일정 입력·초대·동의 화면을 가리고 정차 후 이어서 하도록 안내 */
@Composable
fun ParkedOnlyOverlay(visible: Boolean) {
    AnimatedVisibility(visible, enter = fadeIn() + scaleIn(initialScale = 0.98f), exit = fadeOut()) {
        val c = Th.c
        Box(
            Modifier.fillMaxSize().background(c.backgroundPrimary.copy(alpha = 0.96f))
                .clickable(remember { MutableInteractionSource() }, null) {},
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                IconBadge(GIcons.lock, c.warning, 120.dp, 64.dp)
                Txt("주행 중에는 사용할 수 없어요", Type.title, align = TextAlign.Center)
                Txt("정차하면 이어서 할 수 있어요", Type.body, c.textSecondary, align = TextAlign.Center)
            }
        }
    }
}
