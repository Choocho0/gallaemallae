package com.gallaemallae.app.platform

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

object Privacy {
    /** 차량·사용자 식별자는 해시로만 서버에 보내요 */
    fun anonId(raw: String): String =
        MessageDigest.getInstance("SHA-256").digest(raw.toByteArray()).take(6).joinToString("") { "%02x".format(it) }
}

/** 로그에 좌표·토큰·실명이 남지 않도록 가려요 */
object SafeLog {
    private val coord = Regex("-?\\d{1,3}\\.\\d{3,}")
    private val token = Regex("(token|auth)=\\S+", RegexOption.IGNORE_CASE)

    fun mask(msg: String): String = msg.replace(coord, "**.***").replace(token, "$1=***")

    fun d(tag: String, msg: String) = Log.d(tag, mask(msg))
    fun w(tag: String, msg: String, t: Throwable? = null) = Log.w(tag, mask(msg) + (t?.let { " · ${it.javaClass.simpleName}" } ?: ""))
}

/** 로그인 토큰은 암호화 저장소에만 둬요 */
class SecureStore(context: Context) {
    private val prefs: SharedPreferences = runCatching {
        val key = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            context, "gallae_secure", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }.getOrElse {
        SafeLog.w("SecureStore", "암호화 저장소를 열지 못해 메모리에만 보관해요", it)
        context.getSharedPreferences("gallae_volatile", Context.MODE_PRIVATE).also { p -> p.edit().clear().apply() }
    }

    var token: String?
        get() = prefs.getString("token", null)
        set(v) = prefs.edit().apply { if (v == null) remove("token") else putString("token", v) }.apply()

    var profile: String
        get() = prefs.getString("profile", "민준") ?: "민준"
        set(v) = prefs.edit().putString("profile", v).apply()

    fun clear() = prefs.edit().clear().apply()
}
