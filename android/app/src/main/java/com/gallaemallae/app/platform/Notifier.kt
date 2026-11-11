package com.gallaemallae.app.platform

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gallaemallae.app.MainActivity
import com.gallaemallae.app.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/** 시스템 알림의 버튼을 눌렀을 때 앱으로 전달되는 이벤트 */
data class NotiAction(val feedId: Int, val actionId: String)

object AppEvents {
    private val _actions = MutableSharedFlow<NotiAction>(extraBufferCapacity = 8)
    val actions: SharedFlow<NotiAction> = _actions
    fun emit(a: NotiAction) = _actions.tryEmit(a)
}

class NotiActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val feedId = intent.getIntExtra(EXTRA_FEED, -1)
        val action = intent.getStringExtra(EXTRA_ACTION) ?: return
        AppEvents.emit(NotiAction(feedId, action))
    }

    companion object {
        const val ACTION = "com.gallaemallae.app.NOTI_ACTION"
        const val EXTRA_FEED = "feed"
        const val EXTRA_ACTION = "action"
    }
}

/**
 * 시스템 Notification(헤드업)과 Toast로 알려요.
 * 앱 화면 안에 자체 배너를 띄우지 않고 Pleos 시스템 UI가 그리게 둬요.
 */
class Notifier(private val context: Context) {
    private val nm = NotificationManagerCompat.from(context)

    init {
        val ch = NotificationChannel(CHANNEL, "여정 알림", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "정차 요청, 메세지, 도착 알림"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    private fun allowed(): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    data class Action(val label: String, val id: String)

    /** @param id 같은 id로 다시 부르면 기존 알림을 바꿔요 */
    fun notify(
        id: Int,
        title: String,
        text: String,
        actions: List<Action> = emptyList(),
        feedId: Int = -1,
        timeoutMs: Long = 4500,
    ) {
        if (!allowed()) return
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val b = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setCategory(if (actions.isEmpty()) NotificationCompat.CATEGORY_STATUS else NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(open)
            .setAutoCancel(true)
            .setTimeoutAfter(timeoutMs)
        actions.forEachIndexed { i, a ->
            val pi = PendingIntent.getBroadcast(
                context, id * 10 + i,
                Intent(context, NotiActionReceiver::class.java).setAction(NotiActionReceiver.ACTION)
                    .putExtra(NotiActionReceiver.EXTRA_FEED, feedId)
                    .putExtra(NotiActionReceiver.EXTRA_ACTION, a.id),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            b.addAction(0, a.label, pi)
        }
        try {
            nm.notify(id, b.build())
        } catch (e: SecurityException) {
            SafeLog.w("Notifier", "알림 권한 없음", e)
        }
    }

    fun cancel(id: Int) = nm.cancel(id)

    fun cancelAll() = nm.cancelAll()

    fun toast(text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    companion object {
        const val CHANNEL = "trip_events"
        const val ID_STOP = 100
        const val ID_MSG = 101
        const val ID_INFO = 102
    }
}
