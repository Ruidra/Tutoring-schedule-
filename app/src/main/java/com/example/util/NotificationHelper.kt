package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "tuition_class_reminders"
    const val CHANNEL_NAME = "Loud Class Alarms"
    const val CHANNEL_DESC = "Loud wake-up notifications for upcoming tuition classes and schedules"

    const val ACTION_STOP_ALARM = "com.example.ACTION_STOP_ALARM"
    const val ACTION_SNOOZE_ALARM = "com.example.ACTION_SNOOZE_ALARM"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 400, 800)
                setSound(soundUri, audioAttributes)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun showClassReminder(
        context: Context,
        subject: String,
        startTime: String,
        teacherName: String?,
        address: String?,
        minutesUntilStart: Int,
        notificationId: Int = subject.hashCode()
    ) {
        initNotificationChannel(context)

        // Intent to open app
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop action
        val stopIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_STOP_ALARM
            putExtra("notification_id", notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val stopPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 100000,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_SNOOZE_ALARM
            putExtra("notification_id", notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val snoozePendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 200000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (minutesUntilStart > 0) {
            "⏰ WAKE UP: $subject starts in $minutesUntilStart min!"
        } else {
            "⏰ WAKE UP: $subject is starting now!"
        }

        val details = buildString {
            append("Starts at $startTime")
            if (!teacherName.isNullOrBlank()) {
                append(" with $teacherName")
            }
            if (!address.isNullOrBlank()) {
                append("\n📍 $address")
            }
            append("\n(Alarm will repeat after 5 min if not stopped)")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(details)
            .setStyle(NotificationCompat.BigTextStyle().bigText(details))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(true) // Keeps it visible until stopped
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP ALARM", stopPendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "Snooze 5m", snoozePendingIntent)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 400, 800))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(notificationId, notification)

        triggerVibration(context)
    }

    fun showTestNotification(context: Context) {
        initNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⏰ TEST ALARM: Tuition Wake-Up"
        val body = "Loud alarm is ringing! If not stopped, it will repeat after 5 minutes."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 400, 800))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(9999, notification)

        triggerVibration(context)
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(notificationId)
    }

    private fun triggerVibration(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400, 200, 400), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400, 200, 400), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 400, 200, 400, 200, 400), -1)
                }
            }
        } catch (_: Exception) {}
    }
}
