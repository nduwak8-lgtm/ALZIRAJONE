package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DAILY_CYCLE_CHECK = "com.example.ACTION_DAILY_CYCLE_CHECK"
        const val ACTION_SCHEDULED_REMINDER = "com.example.ACTION_SCHEDULED_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // Disparado pelo AlarmManager mesmo com o aplicativo fechado
        NotificationHelper.checkAndFireCycleNotifications(context)
        // Re-garantir o agendamento contínuo para o próximo dia
        NotificationHelper.scheduleDailyAlarm(context)
    }
}
