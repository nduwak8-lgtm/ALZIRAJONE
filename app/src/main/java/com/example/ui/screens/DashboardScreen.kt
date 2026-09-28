package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyRecord
import com.example.data.local.entity.FlowLevel
import com.example.domain.engine.CurrentCycleStatus
import com.example.domain.engine.DayPrediction
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.util.CokweCalendarHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    status: CurrentCycleStatus,
    todayRecord: DailyRecord?,
    outlook: List<DayPrediction>,
    tips: List<String>,
    viewMode: String,
    onNavigate: (AppScreen) -> Unit,
    onQuickFlow: (FlowLevel) -> Unit,
    onQuickSymptom: (String) -> Unit,
    onQuickMood: (String) -> Unit,
    onQuickSexualActivity: (Boolean, Boolean, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showExplanationDialog by remember { mutableStateOf(false) }
    var showSexualDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Health / Delay Alert Banner if applicable
        if (status.dailyHealthAlert != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().testTag("health_alert_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = "Alerta",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = status.dailyHealthAlert,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        if (status.pregnancyPossibilityNote != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldContainer),
                    modifier = Modifier.fillMaxWidth().testTag("pregnancy_note_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Nota de atraso",
                            tint = GoldAccent
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = status.pregnancyPossibilityNote,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SlateDark,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // Hero Cycle Card
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_cycle_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    EmeraldPrimary.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Confidence Level Tag
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (status.confidenceLevel) {
                                "Alta" -> EmeraldLight
                                "Moderada" -> GoldContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier
                                .clickable { showExplanationDialog = true }
                                .testTag("confidence_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = when (status.confidenceLevel) {
                                        "Alta" -> EmeraldPrimary
                                        "Moderada" -> GoldAccent
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Confiança: ${status.confidenceLevel}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when (status.confidenceLevel) {
                                            "Alta" -> EmeraldDark
                                            "Moderada" -> GoldDark
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = "Ver motivo",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cycle Day Circle Wheel
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            EmeraldLight,
                                            EmeraldPrimary.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                                .border(3.dp, EmeraldPrimary.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (status.hasCycleData) {
                                    Text(
                                        text = "DIA",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = EmeraldDark,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp
                                        )
                                    )
                                    Text(
                                        text = "${status.currentDayOfCycle}",
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                    Text(
                                        text = "do ciclo",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.AddCircleOutline,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Iniciar Ciclo",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Registrar hoje",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phase & Fertility Subtitle
                        Text(
                            text = if (status.hasCycleData) status.currentPhase else "Ainda não existem registros",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (status.hasCycleData) "Fertilidade: ${status.fertilityLevel}" else "Comece registrando o primeiro dia da sua menstruação.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (status.fertilityLevel.contains("Alta") || status.fertilityLevel.contains("Ovulação"))
                                    EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Next Event Indicator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Event,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val eventText = when {
                                    !status.hasCycleData || status.estimatedNextPeriodDate == null ->
                                        "Nenhum ciclo registrado ainda."
                                    status.isLate ->
                                        "Atraso de ${status.daysLate} dias da data esperada"
                                    else -> {
                                        val nextDate = status.estimatedNextPeriodDate
                                        val monthInfo = CokweCalendarHelper.getMonthInfo(nextDate)
                                        "Próxima menstruação prevista: ${nextDate.dayOfMonth} de ${monthInfo.cokweName.uppercase()} (${monthInfo.portugueseName})"
                                    }
                                }
                                Text(
                                    text = eventText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Logging Actions
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Registre Hoje",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(onClick = { onNavigate(AppScreen.DAILY_LOG) }) {
                            Text("Abrir completo", color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Action Buttons Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionButton(
                            icon = "🩸",
                            label = "Fluxo",
                            isSelected = todayRecord?.flow != null && todayRecord.flow != FlowLevel.NONE,
                            onClick = { onNavigate(AppScreen.DAILY_LOG) }
                        )
                        QuickActionButton(
                            icon = "🌡",
                            label = "Temp",
                            isSelected = todayRecord?.basalTemperature != null,
                            onClick = { onNavigate(AppScreen.DAILY_LOG) }
                        )
                        QuickActionButton(
                            icon = "🌸",
                            label = "Muco",
                            isSelected = todayRecord?.cervicalMucus != null && todayRecord.cervicalMucus.name != "NONE",
                            onClick = { onNavigate(AppScreen.DAILY_LOG) }
                        )
                        QuickActionButton(
                            icon = "🧪",
                            label = "LH",
                            isSelected = todayRecord?.lhTest != null && todayRecord.lhTest.name != "NOT_TAKEN",
                            onClick = { onNavigate(AppScreen.DAILY_LOG) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionButton(
                            icon = "😊",
                            label = "Humor",
                            isSelected = !todayRecord?.moods.isNullOrEmpty(),
                            onClick = { onNavigate(AppScreen.DAILY_LOG) }
                        )
                        QuickActionButton(
                            icon = "⚠️",
                            label = "Sintomas",
                            isSelected = !todayRecord?.symptoms.isNullOrEmpty(),
                            onClick = { onNavigate(AppScreen.DAILY_LOG) }
                        )
                        QuickActionButton(
                            icon = "❤️",
                            label = "Atividade",
                            isSelected = todayRecord?.sexualActivity == true,
                            onClick = { showSexualDialog = true }
                        )
                        QuickActionButton(
                            icon = "📝",
                            label = "Nota",
                            isSelected = !todayRecord?.notes.isNullOrBlank(),
                            onClick = { onNavigate(AppScreen.DAILY_LOG) }
                        )
                    }
                }
            }
        }

        // Section 14: Prognóstico dos Próximos Dias (+1, +2, +3, +4, +5, +7 dias)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("outlook_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Prognóstico dos Próximos Dias",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Estimativa baseada nos seus padrões e registros anteriores",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(outlook) { day ->
                            DayOutlookChip(day = day)
                        }
                    }
                }
            }
        }

        // Section 16: Tendência Hormonal Educativa
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("hormone_trend_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tendência Hormonal Estimada",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(
                            Icons.Default.Science,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    HormoneBar(label = "Estrogênio", level = status.hormoneTrend.estrogenLevel, color = EmeraldPrimary)
                    HormoneBar(label = "LH (Luteinizante)", level = status.hormoneTrend.lhLevel, color = GoldAccent)
                    HormoneBar(label = "Progesterona", level = status.hormoneTrend.progesteroneLevel, color = RosePrimary)
                    HormoneBar(label = "FSH", level = status.hormoneTrend.fshLevel, color = SlateMedium)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = status.hormoneTrend.disclaimer,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Section 17 & 18: Como posso me sentir hoje? & Dicas Diárias
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("daily_tips_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dicas do seu ciclo para hoje",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    tips.forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Explanation Dialog
    if (showExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showExplanationDialog = false },
            title = {
                Text(
                    text = "Confiança da Estimativa: ${status.confidenceLevel}",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = status.confidenceReason,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Lembre-se: O aplicativo ALZIRA JONÉ utiliza apenas dados fornecidos por você. As estimativas melhoram progressivamente a cada novo ciclo registrado.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showExplanationDialog = false }) {
                    Text("Entendido", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Modal Rápido de Atividade Sexual
    if (showSexualDialog) {
        var hadSex by remember { mutableStateOf(todayRecord?.sexualActivity ?: true) }
        var isProtected by remember { mutableStateOf(todayRecord?.protectionUsed ?: true) }
        var method by remember { mutableStateOf(todayRecord?.contraceptiveMethodUsed ?: "Preservativo") }

        AlertDialog(
            onDismissRequest = { showSexualDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("❤️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Atividade Sexual de Hoje", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Relação sexual ocorrida hoje?", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = hadSex,
                            onCheckedChange = { hadSex = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }

                    if (hadSex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Text("Proteção / Barreira:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = isProtected,
                                onClick = { isProtected = true },
                                label = { Text("Com Proteção") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldLight, selectedLabelColor = EmeraldDark)
                            )
                            FilterChip(
                                selected = !isProtected,
                                onClick = { isProtected = false },
                                label = { Text("Sem Proteção") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.errorContainer, selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer)
                            )
                        }

                        Text("Método principal:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        val quickMethods = listOf("Preservativo", "Pílula", "DIU", "Coito Interrompido", "Outro")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickMethods.forEach { m ->
                                FilterChip(
                                    selected = method == m,
                                    onClick = { method = m },
                                    label = { Text(m) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldLight, selectedLabelColor = EmeraldDark)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onQuickSexualActivity(hadSex, isProtected, method)
                        showSexualDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Salvar Atividade", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSexualDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun QuickActionButton(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isSelected) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) EmeraldPrimary else Color.Transparent,
                    shape = CircleShape
                )
        ) {
            Text(text = icon, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

@Composable
fun DayOutlookChip(day: DayPrediction) {
    val monthInfo = CokweCalendarHelper.getMonthInfo(day.date)
    val dateStr = "${day.date.dayOfMonth} ${monthInfo.cokweName}"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (day.isProbableOvulation) EmeraldLight
            else if (day.isEstimatedPeriod) RoseContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.width(135.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dia ${day.dayOfCycle}",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = day.estimatedPhase,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (day.isProbableOvulation) EmeraldDark else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
            Text(
                text = day.fertilityStage,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun HormoneBar(label: String, level: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Text(
                text = level,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            )
        }
    }
}
