package com.example.quizappproject

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val builder = NotificationCompat.Builder(context, "quiz_reminder_channel")
            .setSmallIcon(R.drawable.baseline_info_24)
            .setContentTitle("Ready for another round?")
            .setContentText("Take today’s quiz and keep your streak alive!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Take today’s quiz and keep your streak alive!"))


        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1001, builder.build())
    }
}
