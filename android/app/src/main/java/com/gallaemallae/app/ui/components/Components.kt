package com.gallaemallae.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.gallaemallae.app.model.Car
import com.gallaemallae.app.ui.theme.CarColors
import com.gallaemallae.app.ui.theme.Radius
import com.gallaemallae.app.ui.theme.Th
import com.gallaemallae.app.ui.theme.Type
import com.gallaemallae.app.ui.theme.color
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Composable
fun Txt(
    text: String,
    style: TextStyle = Type.body,
    color: Color = Th.c.textPrimary,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
    weight: FontWeight? = null,
) {
    Text(
        text, modifier, color = color, style = if (weight != null) style.copy(fontWeight = weight) else style,
        maxLines = maxLines, overflow = TextOverflow.Ellipsis, textAlign = align,
    )
}

@Composable
fun Ico(v: ImageVector, size: Dp = 40.dp, tint: Color = Th.c.textPrimary, modifier: Modifier = Modifier) {
    Icon(v, null, modifier.size(size), tint = tint)
}

@Composable
fun Gap(h: Dp) = Spacer(Modifier.height(h))

@Composable
fun HGap(w: Dp) = Spacer(Modifier.width(w))

/**
 * 눌림 효과: 0.95 축소 + 배경 변경 + 짧은 햅틱.
 * (Pleos 인터랙션 가이드의 pressed 상태)
 */
@Composable
fun Press(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radius.md),
    color: Color = Color.Transparent,
    pressedColor: Color = color,
    enabled: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.95f else 1f, tween(90), label = "press")
    val view = LocalView.current
    Box(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(if (enabled) 1f else 0.45f)
            .clip(shape)
            .background(if (pressed && enabled) pressedColor else color)
            .clickable(src, null, enabled) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            },
        contentAlignment = contentAlignment,
        content = content,
    )
}

enum class BtnKind { PRIMARY, SECONDARY }

@Composable
fun Btn(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: BtnKind = BtnKind.PRIMARY,
    icon: ImageVector? = null,
    iconEnd: Boolean = true,
    enabled: Boolean = true,
    height: Dp = 96.dp,
) {
    val c = Th.c
    val (bg, pressed, fg) = when (kind) {
        BtnKind.PRIMARY -> Triple(c.buttonPrimary, c.buttonPrimaryPressed, c.textOnPrimary)
        BtnKind.SECONDARY -> Triple(c.buttonSecondary, c.buttonSecondaryPressed, c.textPrimary)
    }
    Press(onClick, modifier.heightIn(min = height), RoundedCornerShape(Radius.md), bg, pressed, enabled, Alignment.Center) {
        Row(
            Modifier.padding(horizontal = 36.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (icon != null && !iconEnd) Ico(icon, 38.dp, fg)
            Txt(label, Type.label, fg, weight = FontWeight.Bold, maxLines = 1)
            if (icon != null && iconEnd) Ico(icon, 38.dp, fg)
        }
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    color: Color = Th.c.surfacePrimary,
    padding: PaddingValues = PaddingValues(32.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.clip(RoundedCornerShape(Radius.lg)).background(color).padding(padding), content = content)
}

@Composable
fun Tag(text: String, color: Color, modifier: Modifier = Modifier) {
    Txt(
        text, Type.caption, color, weight = FontWeight.Bold, maxLines = 1,
        modifier = modifier.clip(RoundedCornerShape(999.dp)).background(color.copy(alpha = 0.16f)).padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
fun CarChip(car: Car, size: Dp = 56.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(car.tone.color()), contentAlignment = Alignment.Center) {
        Txt(car.id.toString(), Type.label, CarColors.ink, weight = FontWeight.ExtraBold)
    }
}

/** 원형 아이콘 배지 (톤 색 16% 배경) */
@Composable
fun IconBadge(v: ImageVector, color: Color, size: Dp = 72.dp, iconSize: Dp = 38.dp) {
    Box(Modifier.size(size).clip(RoundedCornerShape(Radius.sm)).background(color.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
        Ico(v, iconSize, color)
    }
}

@Composable
fun Row2(modifier: Modifier = Modifier, gap: Dp = 16.dp, content: @Composable RowScope.() -> Unit) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically, content = content)
}

/**
 * 스크롤 끝에서 살짝 늘어났다 돌아오는 바운스 효과.
 * 루트에서 기본 overscroll(늘어남 효과)을 끄고 이 컨테이너로 감싸서 써요.
 */
@Composable
fun BounceBox(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val conn = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val o = offset.value
                val dy = available.y
                if (o == 0f || source != NestedScrollSource.UserInput) return Offset.Zero
                if ((o > 0 && dy < 0) || (o < 0 && dy > 0)) {
                    val next = if (o > 0) max(0f, o + dy) else min(0f, o + dy)
                    scope.launch { offset.snapTo(next) }
                    return Offset(0f, next - o)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput || available.y == 0f) return Offset.Zero
                val next = (offset.value + available.y * 0.35f).coerceIn(-180f, 180f)
                scope.launch { offset.snapTo(next) }
                return Offset(0f, available.y)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val v = if (offset.value == 0f && abs(available.y) > 300f) available.y * 0.15f else 0f
                offset.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow), initialVelocity = v)
                return available
            }
        }
    }
    Box(modifier.nestedScroll(conn).graphicsLayer { translationY = offset.value }, content = content)
}

@Composable
fun BounceColumn(
    modifier: Modifier = Modifier,
    gap: Dp = 16.dp,
    padding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    BounceBox(modifier) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding),
            verticalArrangement = Arrangement.spacedBy(gap),
            content = content,
        )
    }
}

/** 데모용 QR (실서비스는 초대 링크로 생성) */
@Composable
fun QrCode(seed: Int, modifier: Modifier = Modifier) {
    val cells = remember(seed) {
        val r = Random(seed * 7919 + 13)
        val n = 25
        fun finder(x: Int, y: Int) = (x < 8 && y < 8) || (x > 16 && y < 8) || (x < 8 && y > 16)
        buildList { for (y in 0 until n) for (x in 0 until n) if (!finder(x, y) && r.nextFloat() < 0.47f) add(x to y) }
    }
    Box(modifier.clip(RoundedCornerShape(Radius.md)).background(Color.White).padding(20.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val k = size.minDimension / 25f
            val ink = Color(0xFF111111)
            cells.forEach { (x, y) -> drawRect(ink, Offset(x * k, y * k), Size(k + 0.5f, k + 0.5f)) }
            listOf(0 to 0, 18 to 0, 0 to 18).forEach { (x, y) ->
                drawRect(ink, Offset(x * k, y * k), Size(7 * k, 7 * k))
                drawRect(Color.White, Offset((x + 1) * k, (y + 1) * k), Size(5 * k, 5 * k))
                drawRect(ink, Offset((x + 2) * k, (y + 2) * k), Size(3 * k, 3 * k))
            }
        }
    }
}
