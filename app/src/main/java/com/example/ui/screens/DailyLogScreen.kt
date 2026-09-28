package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.*
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.util.CokweCalendarHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyLogScreen(
    currentDate: LocalDate,
    existingRecord: DailyRecord?,
    viewMode: String,
    onDateChange: (LocalDate) -> Unit,
    onSaveRecord: (DailyRecord) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateString = remember(currentDate) { currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE) }

    var flow by remember(existingRecord) { mutableStateOf(existingRecord?.flow ?: FlowLevel.NONE) }
    var symptoms by remember(existingRecord) { mutableStateOf(existingRecord?.symptoms ?: emptyList()) }
    var moods by remember(existingRecord) { mutableStateOf(existingRecord?.moods ?: emptyList()) }
    var energy by remember(existingRecord) { mutableIntStateOf(existingRecord?.energyLevel ?: 7) }
    var pain by remember(existingRecord) { mutableIntStateOf(existingRecord?.painLevel ?: 0) }
    var sleepHours by remember(existingRecord) { mutableDoubleStateOf(existingRecord?.sleepHours ?: 7.5) }
    var sleepQuality by remember(existingRecord) { mutableStateOf(existingRecord?.sleepQuality ?: "Boa") }
    var bbtInput by remember(existingRecord) { mutableStateOf(existingRecord?.basalTemperature?.toString() ?: "") }
    var weightInput by remember(existingRecord) { mutableStateOf(existingRecord?.weightKg?.toString() ?: "") }
    var mucus by remember(existingRecord) { mutableStateOf(existingRecord?.cervicalMucus ?: CervicalMucusType.NONE) }
    var lhTest by remember(existingRecord) { mutableStateOf(existingRecord?.lhTest ?: LhTestResult.NOT_TAKEN) }
    var pregnancyTest by remember(existingRecord) { mutableStateOf(existingRecord?.pregnancyTest ?: PregnancyTestResult.NOT_TAKEN) }
    var sexualActivity by remember(existingRecord) { mutableStateOf(existingRecord?.sexualActivity ?: false) }
    var protectionUsed by remember(existingRecord) { mutableStateOf(existingRecord?.protectionUsed ?: true) }
    var contraceptiveMethod by remember(existingRecord) { mutableStateOf(existingRecord?.contraceptiveMethodUsed ?: "Preservativo") }
    var sexualNotesPrivate by remember(existingRecord) { mutableStateOf(existingRecord?.sexualNotesPrivate ?: "") }
    var libido by remember(existingRecord) { mutableStateOf("Moderado") }
    var orgasm by remember(existingRecord) { mutableStateOf("Sim") }
    var notes by remember(existingRecord) { mutableStateOf(existingRecord?.notes ?: "") }

    var saveFeedback by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("daily_log_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Selector Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onDateChange(currentDate.minusDays(1)) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Dia anterior")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val monthInfo = CokweCalendarHelper.getMonthInfo(currentDate)
                        Text(
                            text = "${currentDate.dayOfMonth} de ${monthInfo.cokweName.uppercase()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = EmeraldPrimary
                        )
                        Text(
                            text = "(${monthInfo.portugueseName}) • ${currentDate.year}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = if (currentDate.isEqual(LocalDate.now())) "Hoje" else dateString,
                            style = MaterialTheme.typography.labelSmall.copy(color = EmeraldDark)
                        )
                    }

                    IconButton(onClick = { onDateChange(currentDate.plusDays(1)) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Próximo dia")
                    }
                }
            }
        }

        // Section: Menstruação / Fluxo
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🩸 Menstruação / Fluxo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FlowLevel.entries.forEach { level ->
                            FilterChip(
                                selected = flow == level,
                                onClick = { flow = level },
                                label = { Text(level.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldLight,
                                    selectedLabelColor = EmeraldDark
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: Sintomas (Multiple Selection)
        item {
            val allSymptoms = listOf(
                "Cólicas", "Dor abdominal", "Dor lombar", "Dor de cabeça", "Enxaqueca",
                "Náusea", "Tontura", "Sensibilidade mamária", "Inchaço", "Acne",
                "Cansaço", "Sono", "Alterações intestinais", "Alterações urinárias",
                "Corrimento", "Dor pélvica", "Outros"
            )
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ Sintomas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allSymptoms.forEach { s ->
                            val isSelected = symptoms.contains(s)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    symptoms = if (isSelected) symptoms - s else symptoms + s
                                },
                                label = { Text(s) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldLight,
                                    selectedLabelColor = EmeraldDark
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: Humor
        item {
            val allMoods = listOf(
                "Feliz", "Tranquila", "Normal", "Triste", "Ansiosa",
                "Irritada", "Sensível", "Energética", "Cansada", "Emocional", "Outro"
            )
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "😊 Humor",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allMoods.forEach { m ->
                            val isSelected = moods.contains(m)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    moods = if (isSelected) moods - m else moods + m
                                },
                                label = { Text(m) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldLight,
                                    selectedLabelColor = EmeraldDark
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: Escalas de Dor e Energia (0 a 10)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚡ Energia: $energy / 10",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Slider(
                        value = energy.toFloat(),
                        onValueChange = { energy = it.toInt() },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🩺 Nível de Dor: $pain / 10",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Slider(
                        value = pain.toFloat(),
                        onValueChange = { pain = it.toInt() },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.error, activeTrackColor = MaterialTheme.colorScheme.error)
                    )
                }
            }
        }

        // Section: Temperatura Basal & Peso
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🌡 Temperatura Basal & Peso",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = bbtInput,
                            onValueChange = { bbtInput = it },
                            label = { Text("Temp (°C)") },
                            placeholder = { Text("Ex: 36.35") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Peso (kg)") },
                            placeholder = { Text("Ex: 58.0") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "A temperatura basal serve para confirmar retrospectivamente que a ovulação provavelmente ocorreu.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Section: Muco Cervical
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🌸 Muco Cervical",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "A presença de muco transparente, escorregadio e elástico está frequentemente associada à aproximação da ovulação.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CervicalMucusType.entries.forEach { m ->
                            FilterChip(
                                selected = mucus == m,
                                onClick = { mucus = m },
                                label = { Text(m.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldLight,
                                    selectedLabelColor = EmeraldDark
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: Teste de LH
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🧪 Teste de LH (Ovulação)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LhTestResult.entries.forEach { result ->
                            FilterChip(
                                selected = lhTest == result,
                                onClick = { lhTest = result },
                                label = { Text(result.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldLight,
                                    selectedLabelColor = EmeraldDark
                                )
                            )
                        }
                    }

                    if (lhTest == LhTestResult.POSITIVE || lhTest == LhTestResult.PEAK) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Foi detectado um aumento de LH. Isso pode indicar aproximação da ovulação, mas não garante que ela ocorrerá exatamente em determinado horário.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = EmeraldDark,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // Section: Atividade Sexual (Habilitada com suporte completo)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("sexual_activity_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "❤️ Atividade Sexual & Íntima",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Switch(
                            checked = sexualActivity,
                            onCheckedChange = { sexualActivity = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }

                    Text(
                        text = if (sexualActivity) "Atividade sexual registrada para este dia" else "Toque para registrar relação sexual",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    if (sexualActivity) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Proteção
                        Text(
                            text = "🛡️ Uso de Proteção",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = protectionUsed,
                                onClick = { protectionUsed = true },
                                label = { Text("Com Proteção") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldLight,
                                    selectedLabelColor = EmeraldDark
                                )
                            )
                            FilterChip(
                                selected = !protectionUsed,
                                onClick = { protectionUsed = false },
                                label = { Text("Sem Proteção") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Método contraceptivo
                        Text(
                            text = "💊 Método Contraceptivo / Barreira",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val methods = listOf("Preservativo", "Pílula / Anticoncepcional", "DIU", "Coito Interrompido", "Preservativo Feminino", "Outro")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            methods.forEach { method ->
                                FilterChip(
                                    selected = contraceptiveMethod == method,
                                    onClick = { contraceptiveMethod = method },
                                    label = { Text(method) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldLight,
                                        selectedLabelColor = EmeraldDark
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Libido / Desejo sexual
                        Text(
                            text = "✨ Desejo Sexual / Libido",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val libidos = listOf("Alto 🔥", "Moderado 😊", "Baixo 🍃")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            libidos.forEach { l ->
                                FilterChip(
                                    selected = libido == l,
                                    onClick = { libido = l },
                                    label = { Text(l) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldLight,
                                        selectedLabelColor = EmeraldDark
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Orgasmo
                        Text(
                            text = "💫 Orgasmo",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val orgasms = listOf("Sim ✨", "Não", "Múltiplo 🌟")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            orgasms.forEach { o ->
                                FilterChip(
                                    selected = orgasm == o,
                                    onClick = { orgasm = o },
                                    label = { Text(o) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldLight,
                                        selectedLabelColor = EmeraldDark
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Observações íntimas privadas
                        OutlinedTextField(
                            value = sexualNotesPrivate,
                            onValueChange = { sexualNotesPrivate = it },
                            label = { Text("🔒 Observações Íntimas Privadas") },
                            placeholder = { Text("Ex: sensações, detalhes ou anotações particulares...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            }
        }

        // Section: Notas
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📝 Notas Pessoais",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Adicione qualquer observação sobre o dia...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val finalSexualNotes = buildString {
                        if (sexualNotesPrivate.isNotBlank()) append(sexualNotesPrivate)
                        if (sexualActivity) {
                            if (isNotEmpty()) append(" | ")
                            append("Libido: $libido | Orgasmo: $orgasm")
                        }
                    }
                    val record = DailyRecord(
                        date = dateString,
                        flow = flow,
                        symptoms = symptoms,
                        moods = moods,
                        energyLevel = energy,
                        painLevel = pain,
                        sleepHours = sleepHours,
                        sleepQuality = sleepQuality,
                        basalTemperature = bbtInput.toDoubleOrNull(),
                        weightKg = weightInput.toDoubleOrNull(),
                        cervicalMucus = mucus,
                        lhTest = lhTest,
                        pregnancyTest = pregnancyTest,
                        sexualActivity = sexualActivity,
                        protectionUsed = protectionUsed,
                        contraceptiveMethodUsed = contraceptiveMethod,
                        sexualNotesPrivate = finalSexualNotes,
                        notes = notes
                    )
                    onSaveRecord(record)
                    saveFeedback = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_daily_log_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar Registro Diário", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            if (saveFeedback) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ Registro salvo com sucesso! Os cálculos e previsões foram atualizados.",
                    color = EmeraldPrimary,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
