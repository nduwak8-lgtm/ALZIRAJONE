package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyRecord
import com.example.data.local.entity.FlowLevel
import com.example.domain.engine.CurrentCycleStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.util.BilingualMonthHeader
import com.example.util.CokweCalendarHelper
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun CalendarScreen(
    status: CurrentCycleStatus,
    allRecords: List<DailyRecord>,
    selectedDate: LocalDate,
    viewMode: String,
    onSelectDate: (LocalDate) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var calendarViewType by remember { mutableStateOf("MÊS") } // MÊS, SEMANA, DIA
    var showMonthPicker by remember { mutableStateOf(false) }

    // Manter o mês exibido sincronizado quando a data selecionada mudar fora do mês atual
    LaunchedEffect(selectedDate) {
        val selectedMonth = YearMonth.from(selectedDate)
        if (selectedMonth != displayedMonth) {
            displayedMonth = selectedMonth
        }
    }

    val daysInMonth = remember(displayedMonth) {
        val firstDay = displayedMonth.atDay(1)
        val dayOfWeekOffset = (firstDay.dayOfWeek.value % 7) // Sunday = 0
        val length = displayedMonth.lengthOfMonth()
        val list = mutableListOf<LocalDate?>()
        for (i in 0 until dayOfWeekOffset) {
            list.add(null)
        }
        for (d in 1..length) {
            list.add(displayedMonth.atDay(d))
        }
        list
    }

    val recordsByDate = remember(allRecords) {
        allRecords.associateBy { it.date }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("calendar_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Selector de Modo: MÊS, SEMANA, DIA
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Segmented Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        listOf("MÊS", "SEMANA", "DIA").forEach { type ->
                            FilterChip(
                                selected = calendarViewType == type,
                                onClick = { calendarViewType = type },
                                label = {
                                    Text(
                                        text = type,
                                        fontWeight = if (calendarViewType == type) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                },
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .testTag("chip_view_$type"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldLight,
                                    selectedLabelColor = EmeraldDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Navegação de Período Bilíngue dependendo do tipo selecionado
                    when (calendarViewType) {
                        "MÊS" -> {
                            BilingualMonthHeader(
                                yearMonth = displayedMonth,
                                onPrevious = { displayedMonth = displayedMonth.minusMonths(1) },
                                onNext = { displayedMonth = displayedMonth.plusMonths(1) },
                                onMonthClick = { showMonthPicker = true }
                            )
                        }
                        "SEMANA" -> {
                            val startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                            val endOfWeek = startOfWeek.plusDays(6)
                            val startMonthInfo = CokweCalendarHelper.getMonthInfo(startOfWeek)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { onSelectDate(selectedDate.minusWeeks(1)) }) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Semana anterior", tint = EmeraldPrimary)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = startMonthInfo.cokweName.uppercase(),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            letterSpacing = 1.sp
                                        ),
                                        color = EmeraldPrimary
                                    )
                                    Text(
                                        text = "(${startMonthInfo.portugueseName}) • ${startOfWeek.dayOfMonth} a ${endOfWeek.dayOfMonth}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                IconButton(onClick = { onSelectDate(selectedDate.plusWeeks(1)) }) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Próxima semana", tint = EmeraldPrimary)
                                }
                            }
                        }
                        "DIA" -> {
                            val dayMonthInfo = CokweCalendarHelper.getMonthInfo(selectedDate)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { onSelectDate(selectedDate.minusDays(1)) }) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Dia anterior", tint = EmeraldPrimary)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${selectedDate.dayOfMonth} de ${dayMonthInfo.cokweName.uppercase()}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 19.sp
                                        ),
                                        color = EmeraldPrimary
                                    )
                                    Text(
                                        text = "(${dayMonthInfo.portugueseName}) • ${selectedDate.year}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                IconButton(onClick = { onSelectDate(selectedDate.plusDays(1)) }) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Próximo dia", tint = EmeraldPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. CONTEÚDO BASEADO NO TIPO: MÊS / SEMANA / DIA
        when (calendarViewType) {
            "MÊS" -> {
                // Header dos dias da semana
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb").forEach { day ->
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Grid dos dias do mês
                            val rows = daysInMonth.chunked(7)
                            rows.forEach { week ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                    week.forEach { date ->
                                        if (date == null) {
                                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                        } else {
                                            val isSelected = date.isEqual(selectedDate)
                                            val isToday = date.isEqual(LocalDate.now())
                                            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                            val record = recordsByDate[dateStr]

                                            val isPeriodDay = (record?.flow != null && record.flow != FlowLevel.NONE) ||
                                                    (status.estimatedNextPeriodDate != null && (date.isEqual(status.estimatedNextPeriodDate) ||
                                                            (date.isAfter(status.estimatedNextPeriodDate) && date.isBefore(status.estimatedNextPeriodDate.plusDays(4)))))
                                            val isOvulation = status.estimatedOvulationDate != null && date.isEqual(status.estimatedOvulationDate)
                                            val isFertile = status.fertileWindowStart != null && status.fertileWindowEnd != null &&
                                                    !date.isBefore(status.fertileWindowStart) && !date.isAfter(status.fertileWindowEnd)

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        when {
                                                            isSelected -> EmeraldPrimary
                                                            isPeriodDay -> RoseContainer
                                                            isOvulation -> GoldContainer
                                                            isFertile -> EmeraldLight
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .border(
                                                        width = if (isToday) 1.5.dp else 0.dp,
                                                        color = if (isToday) EmeraldPrimary else Color.Transparent,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { onSelectDate(date) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = "${date.dayOfMonth}",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    )

                                                    // Mini Marcadores (incluindo ❤️ atividade sexual)
                                                    Row(horizontalArrangement = Arrangement.Center) {
                                                        if (record?.flow != null && record.flow != FlowLevel.NONE) {
                                                            Text("🩸", fontSize = 8.sp)
                                                        } else if (isFertile) {
                                                            Text(if (isOvulation) "🥚" else "🌸", fontSize = 8.sp)
                                                        }
                                                        if (record?.basalTemperature != null) {
                                                            Text("🌡", fontSize = 8.sp)
                                                        }
                                                        if (record?.sexualActivity == true) {
                                                            Text("❤️", fontSize = 8.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Card do Dia Selecionado na visão mensal
                item {
                    val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val record = recordsByDate[dateStr]
                    SelectedDayDetailCard(
                        selectedDate = selectedDate,
                        record = record,
                        onEdit = { onNavigate(AppScreen.DAILY_LOG) }
                    )
                }
            }

            "SEMANA" -> {
                // Visão semanal detalhada dia a dia
                val startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                val daysOfWeek = (0..6).map { startOfWeek.plusDays(it.toLong()) }

                items(daysOfWeek) { date ->
                    val isSelected = date.isEqual(selectedDate)
                    val isToday = date.isEqual(LocalDate.now())
                    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val record = recordsByDate[dateStr]

                    val isPeriodDay = (record?.flow != null && record.flow != FlowLevel.NONE) ||
                            (status.estimatedNextPeriodDate != null && (date.isEqual(status.estimatedNextPeriodDate) ||
                                    (date.isAfter(status.estimatedNextPeriodDate) && date.isBefore(status.estimatedNextPeriodDate.plusDays(4)))))
                    val isOvulation = status.estimatedOvulationDate != null && date.isEqual(status.estimatedOvulationDate)
                    val isFertile = status.fertileWindowStart != null && status.fertileWindowEnd != null &&
                            !date.isBefore(status.fertileWindowStart) && !date.isAfter(status.fertileWindowEnd)

                    val monthInfo = CokweCalendarHelper.getMonthInfo(date)
                    val weekDayName = when (date.dayOfWeek) {
                        DayOfWeek.SUNDAY -> "Domingo"
                        DayOfWeek.MONDAY -> "Segunda-feira"
                        DayOfWeek.TUESDAY -> "Terça-feira"
                        DayOfWeek.WEDNESDAY -> "Quarta-feira"
                        DayOfWeek.THURSDAY -> "Quinta-feira"
                        DayOfWeek.FRIDAY -> "Sexta-feira"
                        DayOfWeek.SATURDAY -> "Sábado"
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) EmeraldLight.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectDate(date) }
                            .border(
                                width = if (isToday) 1.5.dp else if (isSelected) 1.5.dp else 0.dp,
                                color = if (isToday) EmeraldPrimary else if (isSelected) EmeraldPrimary else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> EmeraldPrimary
                                                    isPeriodDay -> RoseContainer
                                                    isOvulation -> GoldContainer
                                                    isFertile -> EmeraldLight
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${date.dayOfMonth}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = weekDayName,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${date.dayOfMonth} de ${monthInfo.cokweName} (${monthInfo.portugueseName})",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }

                                if (isToday) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = EmeraldLight) {
                                        Text(
                                            text = "Hoje",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = EmeraldDark,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Badges do dia na semana
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isPeriodDay) {
                                    BadgePill(text = "🩸 Menstruação", containerColor = RoseContainer, textColor = RosePrimary)
                                }
                                if (isOvulation) {
                                    BadgePill(text = "🥚 Ovulação", containerColor = GoldContainer, textColor = GoldDark)
                                } else if (isFertile) {
                                    BadgePill(text = "🌸 Fértil", containerColor = EmeraldLight, textColor = EmeraldDark)
                                }
                                if (record?.sexualActivity == true) {
                                    BadgePill(
                                        text = if (record.protectionUsed) "❤️ Com proteção" else "❤️ Sem proteção",
                                        containerColor = if (record.protectionUsed) EmeraldLight else MaterialTheme.colorScheme.errorContainer,
                                        textColor = if (record.protectionUsed) EmeraldDark else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (record?.basalTemperature != null) {
                                    BadgePill(text = "🌡 ${record.basalTemperature}°C", containerColor = MaterialTheme.colorScheme.surfaceVariant, textColor = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            if (record != null && (record.symptoms.isNotEmpty() || record.moods.isNotEmpty())) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val summary = (record.symptoms + record.moods).take(3).joinToString(", ")
                                Text(
                                    text = summary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            "DIA" -> {
                // Visão focada exclusiva do Dia selecionado
                item {
                    val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val record = recordsByDate[dateStr]
                    val monthInfo = CokweCalendarHelper.getMonthInfo(selectedDate)

                    val isPeriodDay = (record?.flow != null && record.flow != FlowLevel.NONE) ||
                            (status.estimatedNextPeriodDate != null && (selectedDate.isEqual(status.estimatedNextPeriodDate) ||
                                    (selectedDate.isAfter(status.estimatedNextPeriodDate) && selectedDate.isBefore(status.estimatedNextPeriodDate.plusDays(4)))))
                    val isOvulation = status.estimatedOvulationDate != null && selectedDate.isEqual(status.estimatedOvulationDate)
                    val isFertile = status.fertileWindowStart != null && status.fertileWindowEnd != null &&
                            !selectedDate.isBefore(status.fertileWindowStart) && !selectedDate.isAfter(status.fertileWindowEnd)

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Card Principal do Dia
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().testTag("day_view_hero_card")
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = monthInfo.cokweName.uppercase(),
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 24.sp
                                            ),
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = "(${monthInfo.portugueseName})",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                    Button(
                                        onClick = { onNavigate(AppScreen.DAILY_LOG) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Registrar", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(14.dp))

                                // Fases e Estimativas do Dia
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isPeriodDay) RoseContainer else if (isOvulation) GoldContainer else if (isFertile) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Fase do Ciclo",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                            Text(
                                                text = when {
                                                    isPeriodDay -> "Menstruação"
                                                    isOvulation -> "Ovulação Provável"
                                                    isFertile -> "Janela Fértil"
                                                    else -> "Fase Lútea / Folicular"
                                                },
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Fertilidade",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                            Text(
                                                text = if (isOvulation) "Pico Muito Alto" else if (isFertile) "Alta Probabilidade" else "Baixa Probabilidade",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Seção Destaque: Atividade Sexual
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (record?.sexualActivity == true) EmeraldLight.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("❤️", fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Atividade Sexual",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }

                                            if (record?.sexualActivity == true) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (record.protectionUsed) EmeraldLight else MaterialTheme.colorScheme.errorContainer
                                                ) {
                                                    Text(
                                                        text = if (record.protectionUsed) "Com proteção" else "Sem proteção",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = if (record.protectionUsed) EmeraldDark else MaterialTheme.colorScheme.onErrorContainer,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        if (record?.sexualActivity == true) {
                                            Text(
                                                text = "• Método utilizado: ${record.contraceptiveMethodUsed}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            if (record.sexualNotesPrivate.isNotBlank()) {
                                                Text(
                                                    text = "• Detalhes: ${record.sexualNotesPrivate}",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "Nenhuma relação sexual registrada para este dia.",
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Outros registros do dia
                                if (record != null) {
                                    if (record.flow != FlowLevel.NONE) {
                                        DetailItem(icon = "🩸", label = "Fluxo Menstrual", value = record.flow.label)
                                    }
                                    if (record.basalTemperature != null) {
                                        DetailItem(icon = "🌡", label = "Temperatura Basal", value = "${record.basalTemperature} °C")
                                    }
                                    if (record.weightKg != null) {
                                        DetailItem(icon = "⚖️", label = "Peso", value = "${record.weightKg} kg")
                                    }
                                    if (record.cervicalMucus.name != "NONE") {
                                        DetailItem(icon = "🌸", label = "Muco Cervical", value = record.cervicalMucus.label)
                                    }
                                    if (record.lhTest.name != "NOT_TAKEN") {
                                        DetailItem(icon = "🧪", label = "Teste de LH", value = record.lhTest.label)
                                    }
                                    if (record.symptoms.isNotEmpty()) {
                                        DetailItem(icon = "⚠️", label = "Sintomas", value = record.symptoms.joinToString(", "))
                                    }
                                    if (record.moods.isNotEmpty()) {
                                        DetailItem(icon = "😊", label = "Humor", value = record.moods.joinToString(", "))
                                    }
                                    if (record.notes.isNotBlank()) {
                                        DetailItem(icon = "📝", label = "Notas Pessoais", value = record.notes)
                                    }
                                } else {
                                    Text(
                                        text = "Nenhum outro sintoma ou dado anotado neste dia.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Legenda de Marcadores
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Legenda de Marcadores",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🩸 Menstruação  |  🌸 Janela Fértil  |  🥚 Ovulação Provável  |  🌡 Temp Basal  |  ❤️ Atividade Sexual  |  📝 Notas  |  ⚠️ Sintomas",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }

    // Modal de Escolha Rápida de Mês com os 12 meses em Côkwe e Português
    if (showMonthPicker) {
        AlertDialog(
            onDismissRequest = { showMonthPicker = false },
            title = {
                Text(
                    text = "Meses em Côkwe & Português",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CokweCalendarHelper.MONTHS) { item ->
                        val isCurrent = displayedMonth.monthValue == item.monthNumber
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    displayedMonth = YearMonth.of(displayedMonth.year, item.monthNumber)
                                    showMonthPicker = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.cokweName.uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp
                                        ),
                                        color = if (isCurrent) EmeraldDark else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.traditionalNumber,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                if (isCurrent) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMonthPicker = false }) {
                    Text("Fechar", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun SelectedDayDetailCard(
    selectedDate: LocalDate,
    record: DailyRecord?,
    onEdit: () -> Unit
) {
    val monthInfo = CokweCalendarHelper.getMonthInfo(selectedDate)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("day_detail_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${selectedDate.dayOfMonth} de ${monthInfo.cokweName.uppercase()}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "(${monthInfo.portugueseName}) • ${selectedDate.year}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Editar", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (record == null) {
                Text(
                    text = "Nenhum registro para este dia. Toque em 'Editar' para adicionar menstruação, atividade sexual, temperatura ou sintomas.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            } else {
                if (record.flow != FlowLevel.NONE) {
                    DetailItem(icon = "🩸", label = "Fluxo Menstrual", value = record.flow.label)
                }
                if (record.basalTemperature != null) {
                    DetailItem(icon = "🌡", label = "Temperatura Basal", value = "${record.basalTemperature} °C")
                }
                if (record.cervicalMucus.name != "NONE") {
                    DetailItem(icon = "🌸", label = "Muco Cervical", value = record.cervicalMucus.label)
                }
                if (record.lhTest.name != "NOT_TAKEN") {
                    DetailItem(icon = "🧪", label = "Teste de LH", value = record.lhTest.label)
                }
                if (record.symptoms.isNotEmpty()) {
                    DetailItem(icon = "⚠️", label = "Sintomas", value = record.symptoms.joinToString(", "))
                }
                if (record.moods.isNotEmpty()) {
                    DetailItem(icon = "😊", label = "Humor", value = record.moods.joinToString(", "))
                }
                if (record.sexualActivity) {
                    DetailItem(
                        icon = "❤️",
                        label = "Atividade Sexual",
                        value = if (record.protectionUsed) "Com proteção (${record.contraceptiveMethodUsed})" else "Sem proteção (${record.contraceptiveMethodUsed})"
                    )
                    if (record.sexualNotesPrivate.isNotBlank()) {
                        DetailItem(icon = "🔒", label = "Notas Íntimas", value = record.sexualNotesPrivate)
                    }
                }
                if (record.notes.isNotBlank()) {
                    DetailItem(icon = "📝", label = "Notas", value = record.notes)
                }
            }
        }
    }
}

@Composable
fun BadgePill(text: String, containerColor: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
fun DetailItem(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
