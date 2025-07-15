package com.lkuprashvili.chat.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Long.toTimeFormat(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)

    return when {
        mins < 60 -> "$mins min ago"
        hours < 24 -> "$hours h ago"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(this))
    }
}

fun generateChatId(user1Id: String, user2Id: String): String {
    return if (user1Id < user2Id) "$user1Id-$user2Id" else "$user2Id-$user1Id"
}
fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
