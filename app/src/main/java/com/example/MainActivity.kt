package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.TuitionViewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.TuitionRoutineTheme
import com.example.util.AlarmSoundPlayer
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {

    private val viewModel: TuitionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.initNotificationChannel(this)
        handleAlarmIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val systemInDark = isSystemInDarkTheme()

            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> systemInDark
            }

            TuitionRoutineTheme(darkTheme = isDarkTheme) {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    private fun handleAlarmIntent(intent: Intent?) {
        if (intent == null) return
        val notifId = intent.getIntExtra("notification_id", -1)

        when (intent.action) {
            NotificationHelper.ACTION_STOP_ALARM -> {
                AlarmSoundPlayer.stopAlarm(this)
                val currentAlarms = viewModel.activeAlarms.value
                currentAlarms.forEach { viewModel.dismissAlarm(it.key) }
                if (notifId != -1) {
                    NotificationHelper.cancelNotification(this, notifId)
                }
            }
            NotificationHelper.ACTION_SNOOZE_ALARM -> {
                AlarmSoundPlayer.stopAlarm(this)
                val currentAlarms = viewModel.activeAlarms.value
                currentAlarms.forEach { viewModel.snoozeAlarm(it.key) }
                if (notifId != -1) {
                    NotificationHelper.cancelNotification(this, notifId)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmSoundPlayer.stopAlarm(this)
    }
}
