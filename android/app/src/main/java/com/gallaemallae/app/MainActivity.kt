package com.gallaemallae.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.gallaemallae.app.model.VoiceIntent
import com.gallaemallae.app.ui.GallaeApp

class MainActivity : ComponentActivity() {
    private val vm: TripViewModel by viewModels()

    private val askNotifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 창이 가려지면 저장하고, 다시 보이면 최신 상태로 갱신
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = vm.onAppStart()
            override fun onStop(owner: LifecycleOwner) = vm.onAppStop()
        })
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        handleVoice(intent)
        setContent { GallaeApp(vm) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleVoice(intent)
    }

    /**
     * Gleo AI 음성 명령 진입점.
     * 예) adb shell am start -a com.gallaemallae.app.action.VOICE --es intent yes
     */
    private fun handleVoice(intent: Intent?) {
        if (intent?.action != ACTION_VOICE) return
        val v = when (intent.getStringExtra("intent")?.lowercase()) {
            "yes" -> VoiceIntent.YES
            "no" -> VoiceIntent.NO
            "next" -> VoiceIntent.NEXT
            "late" -> VoiceIntent.LATE
            else -> return
        }
        vm.onVoice(v)
    }

    companion object {
        const val ACTION_VOICE = "com.gallaemallae.app.action.VOICE"
    }
}
