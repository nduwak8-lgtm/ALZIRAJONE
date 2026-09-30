package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun OnboardingScreen(
    onComplete: (
        name: String,
        age: Int,
        habitualCycle: Int,
        habitualPeriod: Int,
        isRegular: Boolean,
        lastPeriodDate: LocalDate?,
        goal: String,
        pin: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }

    var name by remember { mutableStateOf("") }
    var age by remember { mutableIntStateOf(26) }
    var goal by remember { mutableStateOf("Acompanhar ciclo e saúde") }
    var knowsLastPeriod by remember { mutableStateOf(false) }
    var lastPeriodDate by remember { mutableStateOf<LocalDate?>(LocalDate.now().minusDays(10)) }
    var habitualPeriodDuration by remember { mutableIntStateOf(5) }
    var habitualCycleLength by remember { mutableIntStateOf(28) }
    var isRegularCycle by remember { mutableStateOf(true) }
    var pin by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("onboarding_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Progress Indicator
            LinearProgressIndicator(
                progress = { step / 7f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = EmeraldPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (step) {
                    1 -> {
                        // Welcome screen with official portrait
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, GoldAccent, CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.official_app_identity_image),
                                    contentDescription = "Alzira Joné - Identidade Oficial",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "ALZIRA JONÉ",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    color = EmeraldPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Calendário Menstrual, Fertilidade e Saúde Feminina",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldLight
                            ) {
                                Text(
                                    text = "“Seu ciclo. Seus dados. Seu ritmo.”",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = EmeraldDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                    2 -> {
                        // Goal selection
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Qual o seu objetivo principal?",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            val goals = listOf(
                                "Acompanhar ciclo e saúde",
                                "Planejar gravidez (engravidar)",
                                "Evitar gravidez de forma consciente",
                                "Compreender sintomas e TPM",
                                "Pré-concepção e autoconhecimento"
                            )
                            goals.forEach { g ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (goal == g) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = if (goal == g) androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable { goal = g }
                                ) {
                                    Text(
                                        text = g,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (goal == g) FontWeight.Bold else FontWeight.Normal,
                                            color = if (goal == g) EmeraldDark else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                    3 -> {
                        // Name and Age
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Como devemos te chamar?",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Seu Nome ou Apelido") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sua Idade: $age anos",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Slider(
                                value = age.toFloat(),
                                onValueChange = { age = it.toInt() },
                                valueRange = 14f..55f,
                                colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                            )
                        }
                    }
                    4 -> {
                        // Last period start date
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Você se lembra de quando começou sua última menstruação?",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                FilterChip(
                                    selected = knowsLastPeriod,
                                    onClick = { knowsLastPeriod = true },
                                    label = { Text("Sim, sei a data") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldLight,
                                        selectedLabelColor = EmeraldDark
                                    )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                FilterChip(
                                    selected = !knowsLastPeriod,
                                    onClick = { knowsLastPeriod = false },
                                    label = { Text("Não lembro / Começar do zero") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldLight,
                                        selectedLabelColor = EmeraldDark
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (knowsLastPeriod) {
                                val date = lastPeriodDate ?: LocalDate.now().minusDays(10)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = EmeraldLight,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = EmeraldDark)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { lastPeriodDate = date.minusDays(1) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Text("-1 dia", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Button(
                                        onClick = { lastPeriodDate = date.plusDays(1) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Text("+1 dia", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            } else {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Tudo bem! Você começará sem histórico de ciclos.",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Assim que sua menstruação descer, basta registrar o primeiro dia para iniciar seu acompanhamento personalizado.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                    5 -> {
                        // Habitual period duration
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Quantos dias dura sua menstruação habitualmente?",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "$habitualPeriodDuration dias",
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Slider(
                                value = habitualPeriodDuration.toFloat(),
                                onValueChange = { habitualPeriodDuration = it.toInt() },
                                valueRange = 2f..10f,
                                steps = 7,
                                colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                            )
                        }
                    }
                    6 -> {
                        // Habitual cycle length and regularity
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Duração habitual do seu ciclo menstrual?",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "$habitualCycleLength dias",
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Slider(
                                value = habitualCycleLength.toFloat(),
                                onValueChange = { habitualCycleLength = it.toInt() },
                                valueRange = 20f..45f,
                                steps = 24,
                                colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            Text(text = "Seus ciclos costumam ser regulares?", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FilterChip(
                                    selected = isRegularCycle,
                                    onClick = { isRegularCycle = true },
                                    label = { Text("Sim, regulares") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldLight,
                                        selectedLabelColor = EmeraldDark
                                    )
                                )
                                FilterChip(
                                    selected = !isRegularCycle,
                                    onClick = { isRegularCycle = false },
                                    label = { Text("Variáveis / Irregulares") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldLight,
                                        selectedLabelColor = EmeraldDark
                                    )
                                )
                            }
                        }
                    }
                    7 -> {
                        // Security Setup (Optional PIN)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Deseja proteger seus dados com PIN?",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Você pode definir um PIN de 4 dígitos agora ou deixar para depois.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedTextField(
                                value = pin,
                                onValueChange = { if (it.length <= 4) pin = it },
                                label = { Text("PIN de 4 dígitos (opcional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    TextButton(onClick = { step-- }) {
                        Text("Voltar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Spacer(modifier = Modifier.width(60.dp))
                }

                Button(
                    onClick = {
                        if (step < 7) {
                            step++
                        } else {
                            onComplete(
                                name,
                                age,
                                habitualCycleLength,
                                habitualPeriodDuration,
                                isRegularCycle,
                                if (knowsLastPeriod) lastPeriodDate else null,
                                goal,
                                pin.ifBlank { null }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(
                        text = if (step == 7) "Concluir e Iniciar" else "Avançar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(if (step == 7) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}
