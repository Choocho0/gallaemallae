@file:OptIn(ExperimentalTextApi::class)

package com.gallaemallae.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gallaemallae.app.R
import com.gallaemallae.app.model.Tone

/**
 * Pleos System Color 체계 이름을 따른 토큰. 실제 값은 Pleos Figma 토큰으로 교체.
 * 웹 프로토타입(styles.css)과 같은 값.
 */
@Immutable
data class Tokens(
    val dark: Boolean,
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val surfacePrimary: Color,
    val surfaceSecondary: Color,
    val surfaceTertiary: Color,
    val line: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnPrimary: Color,
    val buttonPrimary: Color,
    val buttonPrimaryPressed: Color,
    val buttonSecondary: Color,
    val buttonSecondaryPressed: Color,
    val info: Color,
    val positive: Color,
    val warning: Color,
    val negative: Color,
    val textOnNegative: Color,
    val textOnInfo: Color,
    val textOnWarning: Color,
    val dim: Color,
)

val DarkTokens = Tokens(
    dark = true,
    backgroundPrimary = Color(0xFF0E1014),
    backgroundSecondary = Color(0xFF15181E),
    surfacePrimary = Color(0xFF1C2028),
    surfaceSecondary = Color(0xFF252A34),
    surfaceTertiary = Color(0xFF303644),
    line = Color(0xFF343B49),
    textPrimary = Color(0xFFF4F6F9),
    textSecondary = Color(0xFFC3C9D3),
    textTertiary = Color(0xFF9AA2B0),
    textOnPrimary = Color(0xFF1A0D03),
    buttonPrimary = Color(0xFFFF8A3D),
    buttonPrimaryPressed = Color(0xFFE8732A),
    buttonSecondary = Color(0xFF2C323D),
    buttonSecondaryPressed = Color(0xFF3A4250),
    info = Color(0xFF5B9DFF),
    positive = Color(0xFF3AD38F),
    warning = Color(0xFFFFC24A),
    negative = Color(0xFFFF6B6B),
    textOnNegative = Color(0xFF240404),
    textOnInfo = Color(0xFF07142B),
    textOnWarning = Color(0xFF1A1305),
    dim = Color(0x9E000000),
)

val LightTokens = Tokens(
    dark = false,
    backgroundPrimary = Color(0xFFEEF0F4),
    backgroundSecondary = Color(0xFFE4E7EC),
    surfacePrimary = Color(0xFFFFFFFF),
    surfaceSecondary = Color(0xFFF2F4F7),
    surfaceTertiary = Color(0xFFE3E7ED),
    line = Color(0xFFD3D8E0),
    textPrimary = Color(0xFF11141A),
    textSecondary = Color(0xFF3E4450),
    textTertiary = Color(0xFF5A616D),
    textOnPrimary = Color(0xFFFFFFFF),
    buttonPrimary = Color(0xFFC24E00),
    buttonPrimaryPressed = Color(0xFFA34100),
    buttonSecondary = Color(0xFFE3E7ED),
    buttonSecondaryPressed = Color(0xFFD1D7E0),
    info = Color(0xFF1F5FCF),
    positive = Color(0xFF127A4A),
    warning = Color(0xFF8A5A00),
    negative = Color(0xFFC0322F),
    textOnNegative = Color(0xFFFFFFFF),
    textOnInfo = Color(0xFFFFFFFF),
    textOnWarning = Color(0xFFFFFFFF),
    dim = Color(0x73141820),
)

/** 차량 구분색 (지도·칩 공통, 테마와 무관) */
object CarColors {
    val car1 = Color(0xFF5B9DFF)
    val car2 = Color(0xFFFF8A3D)
    val car3 = Color(0xFF3AD38F)
    val ink = Color(0xFF10131A)
}

val AstaSans = FontFamily(
    listOf(400, 500, 600, 700, 800).map { w ->
        Font(R.font.asta_sans, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))
    },
)

/**
 * 타이포 위계. 12.8" 2560×1440(≈229ppi) 기준으로 중요 정보 ≥5mm, 보조 정보 ≥3mm.
 * 1dp = 1px(160dpi) 화면을 기준으로 잡은 값이라 실제 밀도에 맞춰 확인 필요.
 */
object Type {
    private fun s(size: Int, weight: Int, lh: Float = 1.3f) = TextStyle(
        fontFamily = AstaSans,
        fontSize = size.sp,
        lineHeight = (size * lh).sp,
        fontWeight = FontWeight(weight),
    )

    val display = s(64, 800, 1.1f)
    val headline = s(52, 800, 1.15f)
    val title = s(42, 700, 1.2f)
    val body = s(34, 500)
    val bodyStrong = s(34, 700)
    val label = s(30, 600)
    val caption = s(28, 500)
}

object Radius {
    val lg = 32.dp
    val md = 24.dp
    val sm = 18.dp
}

val LocalTokens = staticCompositionLocalOf { DarkTokens }

object Th {
    val c: Tokens
        @Composable @ReadOnlyComposable get() = LocalTokens.current
}

@Composable
@ReadOnlyComposable
fun Tone.color(): Color {
    val t = LocalTokens.current
    return when (this) {
        Tone.INFO -> t.info
        Tone.POSITIVE -> t.positive
        Tone.WARNING -> t.warning
        Tone.NEGATIVE -> t.negative
        Tone.BRAND -> t.buttonPrimary
        Tone.CAR1 -> CarColors.car1
        Tone.CAR2 -> CarColors.car2
        Tone.CAR3 -> CarColors.car3
    }
}

/** 주간/야간은 시스템 설정(차량 조도)을 따르고, 디버그에서만 강제 지정 */
@Composable
fun GallaeTheme(darkOverride: Boolean? = null, content: @Composable () -> Unit) {
    val dark = darkOverride ?: isSystemInDarkTheme()
    CompositionLocalProvider(LocalTokens provides if (dark) DarkTokens else LightTokens, content = content)
}
