package com.gallaemallae.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gallaemallae.app.BuildConfig
import com.gallaemallae.app.TripViewModel
import com.gallaemallae.app.model.Screen
import com.gallaemallae.app.ui.components.DialogHost
import com.gallaemallae.app.ui.components.ParkedOnlyOverlay
import com.gallaemallae.app.ui.screens.ConsentScreen
import com.gallaemallae.app.ui.screens.HomeScreen
import com.gallaemallae.app.ui.screens.InviteScreen
import com.gallaemallae.app.ui.screens.PlanScreen
import com.gallaemallae.app.ui.screens.SummaryScreen
import com.gallaemallae.app.ui.screens.TripScreen
import com.gallaemallae.app.ui.theme.GallaeTheme
import com.gallaemallae.app.ui.theme.Th

/**
 * 앱 창 크기에 맞춰 그려요.
 * 1:1 분할(세로형 창)과 2:0 전체(가로형 창)를 모두 지원하고 고정 해상도 스케일링은 하지 않아요.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GallaeApp(vm: TripViewModel) {
    GallaeTheme(vm.themeOverride) {
        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            BoxWithConstraints(Modifier.fillMaxSize().background(Th.c.backgroundPrimary)) {
                val landscape = maxWidth > maxHeight * 1.2f
                AnimatedContent(vm.screen, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "screen") { s ->
                    when (s) {
                        Screen.HOME -> HomeScreen(vm, landscape)
                        Screen.PLAN -> PlanScreen(vm, landscape)
                        Screen.INVITE -> InviteScreen(vm, landscape)
                        Screen.CONSENT -> ConsentScreen(vm)
                        Screen.TRIP -> TripScreen(vm, landscape)
                        Screen.SUMMARY -> SummaryScreen(vm, landscape)
                    }
                }
                ParkedOnlyOverlay(vm.needsParked)
                DialogHost(vm.dialog, { vm.lim(it) }, vm::onDialogPrimary, vm::onDialogSecondary, vm::setProfileTo)
                if (BuildConfig.DEBUG) {
                    Box(Modifier.align(Alignment.BottomStart)) { DebugPanel(vm) }
                }
            }
        }
    }
}
