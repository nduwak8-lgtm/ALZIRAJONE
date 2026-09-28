package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.domain.engine.CyclePredictionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

object NotificationHelper {

    const val CHANNEL_CYCLE_ID = "alzira_cycle_alerts"
    const val CHANNEL_MEDICATIONS_ID = "alzira_medications_reminders"
    const val CHANNEL_ACTIVITIES_ID = "alzira_scheduled_activities"

    const val NOTIF_PERIOD_START_ID = 1001
    const val NOTIF_FERTILE_WINDOW_ID = 1002
    const val NOTIF_OVULATION_ID = 1003
    const val NOTIF_DELAY_ALERT_ID = 1004
    const val NOTIF_MEDICATION_ID = 1005
    const val NOTIF_ACTIVITY_ID = 1006
    const val NOTIF_DAILY_CHECKIN_ID = 1007

    fun init(context: Context) {
        createNotificationChannels(context)
        scheduleDailyAlarm(context)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelCycle = NotificationChannel(
                CHANNEL_CYCLE_ID,
                "Início de Período e Fertilidade",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de início de menstruação, período fértil e ovulação"
                enableVibration(true)
                enableLights(true)
            }

            val channelMeds = NotificationChannel(
                CHANNEL_MEDICATIONS_ID,
                "Medicamentos e Contraceptivos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lembretes pontuais para tomar medicamentos e pílula"
                enableVibration(true)
            }

            val channelActivities = NotificationChannel(
                CHANNEL_ACTIVITIES_ID,
                "Actividades Agendadas e Calendário",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Lembretes de compromissos e atividades no calendário"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(channelCycle)
            notificationManager.createNotificationChannel(channelMeds)
            notificationManager.createNotificationChannel(channelActivities)
        }
    }

    fun scheduleDailyAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DAILY_CYCLE_CHECK
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Agendamento diário para as 08:30 da manhã
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (_: Exception) {
            // Em caso de restrição de segurança
        }
    }

    /**
     * Executa a verificação completa em segundo plano mesmo sem abrir o app:
     * - Início do período menstrual
     * - Atraso menstrual
     * - Início do período fértil e dia de ovulação
     * - Atividades agendadas
     * - Medicamentos do dia
     */
    fun checkAndFireCycleNotifications(context: Context) {
        if (!hasNotificationPermission(context)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val dao = db.appDao()

                val profile: com.example.data.local.entity.UserProfile = dao.getUserProfile() ?: return@launch
                val cycles: List<com.example.data.local.entity.MenstrualCycle> = dao.getAllCycles()
                val recentRecords: List<com.example.data.local.entity.DailyRecord> = dao.getAllDailyRecords()
                val medications: List<com.example.data.local.entity.Medication> = dao.getAllMedications()
                val reminders: List<com.example.data.local.entity.AppReminder> = dao.getAllReminders()

                val today = LocalDate.now()
                val latestCycle = cycles.maxByOrNull { it.startDate }

                val status = CyclePredictionEngine.evaluateCurrentStatus(
                    today = today,
                    latestCycle = latestCycle,
                    allCycles = cycles,
                    recentRecords = recentRecords,
                    profile = profile
                )

                // 1. Notificação de início do período menstrual previsto
                if (status.estimatedNextPeriodDate != null) {
                    val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, status.estimatedNextPeriodDate)
                    if (daysUntil == 0L) {
                        sendNotification(
                            context = context,
                            title = "🩸 Início do Período Menstrual",
                            message = "A sua menstruação está prevista para hoje (${status.estimatedNextPeriodDate.dayOfMonth} de ${CokweCalendarHelper.getMonthInfo(status.estimatedNextPeriodDate).portugueseName}). Tenha os seus cuidados preparados.",
                            channelId = CHANNEL_CYCLE_ID,
                            notificationId = NOTIF_PERIOD_START_ID
                        )
                    } else if (daysUntil == 1L) {
                        sendNotification(
                            context = context,
                            title = "🩸 Menstruação Prevista para Amanhã",
                            message = "O seu novo ciclo menstrual deve iniciar amanhã. Mantenha os seus produtos de higiene à mão.",
                            channelId = CHANNEL_CYCLE_ID,
                            notificationId = NOTIF_PERIOD_START_ID
                        )
                    }
                }

                // 2. Notificação de atraso menstrual
                if (status.isLate && status.daysLate >= 1) {
                    sendNotification(
                        context = context,
                        title = "⚠️ Aviso de Atraso Menstrual",
                        message = "O seu ciclo está com ${status.daysLate} dia(s) de atraso em relação à média habitual de ${profile.habitualCycleLength} dias.",
                        channelId = CHANNEL_CYCLE_ID,
                        notificationId = NOTIF_DELAY_ALERT_ID
                    )
                }

                // 3. Notificação de início da janela fértil
                if (status.fertileWindowStart != null && today.isEqual(status.fertileWindowStart)) {
                    sendNotification(
                        context = context,
                        title = "🌸 Início da Janela Fértil",
                        message = "Inicia hoje a sua janela fértil (${status.fertilityLevel}). Probabilidade de ovulação nos próximos dias.",
                        channelId = CHANNEL_CYCLE_ID,
                        notificationId = NOTIF_FERTILE_WINDOW_ID
                    )
                }

                // 4. Notificação de dia de ovulação
                if (status.estimatedOvulationDate != null && today.isEqual(status.estimatedOvulationDate)) {
                    sendNotification(
                        context = context,
                        title = "🥚 Dia Provável de Ovulação",
                        message = "Hoje é o dia de pico de fertilidade e provável ovulação estimado pelo seu calendário.",
                        channelId = CHANNEL_CYCLE_ID,
                        notificationId = NOTIF_OVULATION_ID
                    )
                }

                // 5. Atividades Agendadas no Calendário
                val upcomingEvents = CalendarHelper.getUpcomingEvents(context)
                if (upcomingEvents.isNotEmpty()) {
                    val firstEvent = upcomingEvents.first()
                    sendNotification(
                        context = context,
                        title = "📅 Actividade Agendada no Calendário",
                        message = "Compromisso de hoje: ${firstEvent.title}. Verifique a sua agenda.",
                        channelId = CHANNEL_ACTIVITIES_ID,
                        notificationId = NOTIF_ACTIVITY_ID
                    )
                }

                // 6. Medicamentos / Contraceptivos
                val medsToday = medications.filter { !it.isTakenToday }
                if (medsToday.isNotEmpty()) {
                    val medNames = medsToday.joinToString(", ") { it.name }
                    sendNotification(
                        context = context,
                        title = "💊 Lembrete de Medicamento / Contraceptivo",
                        message = "Lembre-se de tomar: $medNames.",
                        channelId = CHANNEL_MEDICATIONS_ID,
                        notificationId = NOTIF_MEDICATION_ID
                    )
                }

                // 7. Lembretes gerais ativos
                reminders.filter { it.isEnabled }.forEachIndexed { index, rem ->
                    val text = if (rem.isSecret) rem.secretMessage else rem.message
                    sendNotification(
                        context = context,
                        title = rem.title,
                        message = text,
                        channelId = CHANNEL_MEDICATIONS_ID,
                        notificationId = 3000 + index
                    )
                }

            } catch (_: Exception) {
                // Tratamento seguro em background
            }
        }
    }

    fun sendNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String,
        notificationId: Int
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Falha de permissão não esperada
        }
    }

    /**
     * Permite testar imediatamente qualquer categoria de notificação a partir da tela
     */
    fun testNotification(context: Context, type: String) {
        when (type) {
            "PERIOD_START" -> {
                sendNotification(
                    context = context,
                    title = "🩸 Início do Período Menstrual",
                    message = "Aviso do Calendário: Sua menstruação está prevista para hoje. Tenha produtos de higiene por perto.",
                    channelId = CHANNEL_CYCLE_ID,
                    notificationId = NOTIF_PERIOD_START_ID
                )
            }
            "DELAY" -> {
                sendNotification(
                    context = context,
                    title = "⚠️ Aviso de Atraso Menstrual",
                    message = "O seu período menstrual está com 3 dias de atraso em relação à previsão média do seu ciclo.",
                    channelId = CHANNEL_CYCLE_ID,
                    notificationId = NOTIF_DELAY_ALERT_ID
                )
            }
            "FERTILE" -> {
                sendNotification(
                    context = context,
                    title = "🌸 Início da Janela Fértil",
                    message = "Início da sua janela fértil. Maior probabilidade de concepção nos próximos 5 dias.",
                    channelId = CHANNEL_CYCLE_ID,
                    notificationId = NOTIF_FERTILE_WINDOW_ID
                )
            }
            "OVULATION" -> {
                sendNotification(
                    context = context,
                    title = "🥚 Dia Provável de Ovulação",
                    message = "Pico fértil estimado pelo calendário Alzira Joné para o dia de hoje.",
                    channelId = CHANNEL_CYCLE_ID,
                    notificationId = NOTIF_OVULATION_ID
                )
            }
            "MEDICATION" -> {
                sendNotification(
                    context = context,
                    title = "💊 Lembrete de Medicamento / Contraceptivo",
                    message = "Hora de tomar o seu medicamento ou pílula anticoncepcional.",
                    channelId = CHANNEL_MEDICATIONS_ID,
                    notificationId = NOTIF_MEDICATION_ID
                )
            }
            "ACTIVITY" -> {
                sendNotification(
                    context = context,
                    title = "📅 Actividade Agendada no Calendário",
                    message = "Lembrete de compromisso agendado para o dia de hoje no seu calendário.",
                    channelId = CHANNEL_ACTIVITIES_ID,
                    notificationId = NOTIF_ACTIVITY_ID
                )
            }
        }
    }
}
