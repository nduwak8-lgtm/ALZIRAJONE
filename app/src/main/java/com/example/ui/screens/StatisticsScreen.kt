package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyRecord
import com.example.data.local.entity.MenstrualCycle
import com.example.domain.engine.CycleStats
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@Composable
fun StatisticsScreen(
    stats: CycleStats,
    allCycles: List<MenstrualCycle>,
    allRecords: List<DailyRecord>,
    modifier: Modifier = Modifier
) {
    val bbtRecords = allRecords.filter { it.basalTemperature != null }.takeLast(14)
    val allSymptoms = allRecords.flatMap { it.symptoms }
    val symptomCounts = allSymptoms.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }
    val allMoods = allRecords.flatMap { it.moods }
    val moodCounts = allMoods.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("statistics_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Header Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoGraph, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Minhas Estatísticas & Padrões",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatBox(label = "Ciclo Médio", value = "${stats.averageCycleLength}d")
                        StatBox(label = "Mediana", value = "${stats.medianCycleLength}d")
                        StatBox(label = "Menor / Maior", value = "${stats.minCycleLength}-${stats.maxCycleLength}d")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatBox(label = "Menstruação", value = "${stats.averagePeriodDuration}d")
                        StatBox(label = "Regularidade", value = if (stats.isRegular) "Regular" else "Variação Alta")
                        StatBox(label = "Ciclos Salvos", value = "${stats.cycleCount}")
                    }
                }
            }
        }

        // Section 20: Análise Pessoal (Baseada em dados reais)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldLight),
                modifier = Modifier.fillMaxWidth().testTag("personal_analysis_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Análise Pessoal do Seu Ciclo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (stats.cycleCount < 1) {
                        Text(
                            text = "Dados insuficientes para uma análise confiável. Continue registrando seus ciclos e sintomas para desbloquear seus padrões personalizados.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = EmeraldDark)
                        )
                    } else {
                        Text(
                            text = "• Seu ciclo médio nos registros é de ${stats.averageCycleLength} dias.\n" +
                                    "• Variação registrada entre ${stats.minCycleLength} e ${stats.maxCycleLength} dias (Desvio: ${stats.standardDeviation} dias).\n" +
                                    (if (stats.isRegular) "• Seus ciclos mantêm uma consistência saudável e previsível."
                                    else "• Seus ciclos apresentam variação considerável. Sinais corporais como muco cervical e temperatura basal são os melhores guias.") +
                                    "\n• Duração média da menstruação: ${stats.averagePeriodDuration} dias.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = EmeraldDark, lineHeight = 22.sp)
                        )
                    }
                }
            }
        }

        // Section: Gráfico de Temperatura Basal (BBT)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("bbt_chart_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Curva de Temperatura Basal (BBT)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Confirmação retrospectiva de ovulação",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (bbtRecords.isEmpty()) {
                        Text(
                            text = "Nenhuma temperatura basal registrada nos últimos dias. Anote sua temperatura ao acordar na tela 'Registrar'.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    } else {
                        // Visual bar representation of BBT
                        bbtRecords.forEach { rec ->
                            val temp = rec.basalTemperature ?: 36.5
                            val normalized = ((temp - 36.0) / 1.0).coerceIn(0.1, 1.0)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rec.date.takeLast(5),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(50.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(normalized.toFloat())
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(if (temp >= 36.6) GoldAccent else EmeraldPrimary)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$temp°C",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Sintomas Mais Frequentes
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("symptoms_frequency_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sintomas Mais Frequentes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (symptomCounts.isEmpty()) {
                        Text(
                            text = "Nenhum sintoma frequente registrado ainda.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    } else {
                        symptomCounts.take(6).forEach { (symptom, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = symptom, style = MaterialTheme.typography.bodyMedium)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldLight
                                ) {
                                    Text(
                                        text = "$count registro(s)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = EmeraldDark,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Humores Mais Frequentes
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Humores Registrados",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (moodCounts.isEmpty()) {
                        Text(
                            text = "Nenhum humor registrado ainda.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    } else {
                        moodCounts.take(5).forEach { (mood, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = mood, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "$count vez(es)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        )
    }
}
