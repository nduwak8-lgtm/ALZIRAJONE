package com.example.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import java.time.LocalDate
import java.time.YearMonth

/**
 * Mapeamento dos meses do calendário em língua Côkwe e Português:
 * 1. Kamoshi (setembro)
 * 2. Kwali (outubro)
 * 3. Kutatu (novembro)
 * Kuwana (Dezembro)
 * 4. Kutano (janeiro)
 * 5. Kusambano (fevereiro)
 * 6. Kushimbyali (Março)
 * 7. Kunake (Abril)
 * 8. Kuva (Maio)
 * 9. Kakumi Mukehe (junho)
 * 10. Kakumi Munene (Julho)
 * 11. Tchimbejimbeji (Agosto)
 */
data class CokweMonthInfo(
    val monthNumber: Int, // 1 = Janeiro, ..., 12 = Dezembro
    val cokweName: String,
    val portugueseName: String,
    val traditionalNumber: String
)

object CokweCalendarHelper {

    val MONTHS = listOf(
        CokweMonthInfo(1, "Kutano", "Janeiro", "4. Kutano (janeiro)"),
        CokweMonthInfo(2, "Kusambano", "Fevereiro", "5. Kusambano (fevereiro)"),
        CokweMonthInfo(3, "Kushimbyali", "Março", "6. Kushimbyali (Março)"),
        CokweMonthInfo(4, "Kunake", "Abril", "7. Kunake (Abril)"),
        CokweMonthInfo(5, "Kuva", "Maio", "8. Kuva (Maio)"),
        CokweMonthInfo(6, "Kakumi Mukehe", "Junho", "9. Kakumi Mukehe (junho)"),
        CokweMonthInfo(7, "Kakumi Munene", "Julho", "10. Kakumi Munene (Julho)"),
        CokweMonthInfo(8, "Tchimbejimbeji", "Agosto", "11. Tchimbejimbeji (Agosto)"),
        CokweMonthInfo(9, "Kamoshi", "Setembro", "1. Kamoshi (setembro)"),
        CokweMonthInfo(10, "Kwali", "Outubro", "2. Kwali (outubro)"),
        CokweMonthInfo(11, "Kutatu", "Novembro", "3. Kutatu (novembro)"),
        CokweMonthInfo(12, "Kuwana", "Dezembro", "Kuwana (Dezembro)")
    )

    fun getMonthInfo(monthNumber: Int): CokweMonthInfo {
        return MONTHS.firstOrNull { it.monthNumber == monthNumber }
            ?: CokweMonthInfo(monthNumber, "Kutano", "Janeiro", "4. Kutano (janeiro)")
    }

    fun getMonthInfo(yearMonth: YearMonth): CokweMonthInfo {
        return getMonthInfo(yearMonth.monthValue)
    }

    fun getMonthInfo(date: LocalDate): CokweMonthInfo {
        return getMonthInfo(date.monthValue)
    }

    /**
     * Retorna a descrição bilíngue completa com Côkwe em destaque:
     * Ex: "KAMOSHI (Setembro) 2026"
     */
    fun formatBilingualMonthWithYear(yearMonth: YearMonth): Pair<String, String> {
        val info = getMonthInfo(yearMonth)
        val cokweUpper = info.cokweName.uppercase()
        val ptWithYear = "(${info.portugueseName}) ${yearMonth.year}"
        return Pair(cokweUpper, ptWithYear)
    }

    /**
     * Formata uma data completa com o nome bilíngue do mês
     * Ex: "28 de Kamoshi (Setembro) de 2026"
     */
    fun formatBilingualDate(date: LocalDate): String {
        val info = getMonthInfo(date)
        return "${date.dayOfMonth} de ${info.cokweName} (${info.portugueseName}) de ${date.year}"
    }

    /**
     * Retorna o nome curto para cabeçalhos de semana
     */
    fun formatBilingualWeekRange(start: LocalDate, end: LocalDate): String {
        val startInfo = getMonthInfo(start)
        val endInfo = getMonthInfo(end)
        return if (start.monthValue == end.monthValue) {
            "${start.dayOfMonth} - ${end.dayOfMonth} de ${startInfo.cokweName} (${startInfo.portugueseName}) ${start.year}"
        } else {
            "${start.dayOfMonth} ${startInfo.cokweName} (${startInfo.portugueseName}) - ${end.dayOfMonth} ${endInfo.cokweName} (${endInfo.portugueseName}) ${end.year}"
        }
    }
}

/**
 * Cabeçalho de Navegação de Mês Bilíngue (Côkwe em letras grandes e português entre parênteses)
 */
@Composable
fun BilingualMonthHeader(
    yearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onMonthClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val monthInfo = CokweCalendarHelper.getMonthInfo(yearMonth)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Mês anterior", tint = EmeraldPrimary)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .then(if (onMonthClick != null) Modifier.clickable { onMonthClick() } else Modifier)
                .padding(vertical = 4.dp)
        ) {
            // Nome em Côkwe em LETRAS GRANDES
            Text(
                text = monthInfo.cokweName.uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp,
                    letterSpacing = 1.2.sp
                ),
                color = EmeraldPrimary,
                textAlign = TextAlign.Center
            )

            // Nome em português entre parênteses () e ano
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "(${monthInfo.portugueseName})",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${yearMonth.year}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                )
            }
        }

        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Próximo mês", tint = EmeraldPrimary)
        }
    }
}
