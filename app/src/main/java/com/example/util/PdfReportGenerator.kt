package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.local.entity.DailyRecord
import com.example.data.local.entity.MenstrualCycle
import com.example.data.local.entity.UserProfile
import com.example.domain.engine.CycleStats
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PdfReportGenerator {

    fun generateHealthReport(
        context: Context,
        profile: UserProfile,
        cycles: List<MenstrualCycle>,
        records: List<DailyRecord>,
        stats: CycleStats,
        periodFilterLabel: String
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(30, 95, 59) // EmeraldPrimary
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(100, 115, 105)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val sectionPaint = Paint().apply {
            color = Color.rgb(30, 95, 59)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(40, 45, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val boldBodyPaint = Paint().apply {
            color = Color.rgb(30, 40, 35)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val footerPaint = Paint().apply {
            color = Color.rgb(130, 140, 135)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }

        val linePaint = Paint().apply {
            color = Color.rgb(205, 218, 210)
            strokeWidth = 1f
        }

        var y = 50f

        // Header
        canvas.drawText("ALZIRA JONÉ", 40f, y, titlePaint)
        y += 16f
        canvas.drawText("Calendário Menstrual, Fertilidade e Saúde Feminina", 40f, y, subtitlePaint)
        y += 14f
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        canvas.drawText("Relatório Confidencial de Saúde | Gerado em: $currentDate | Período: $periodFilterLabel", 40f, y, footerPaint)
        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 24f

        // Patient / Profile Info
        canvas.drawText("1. DADOS DA UTILIZADORA", 40f, y, sectionPaint)
        y += 16f
        canvas.drawText("Nome: ${profile.name}    Idade: ${profile.age} anos    Objetivo: ${profile.primaryGoal}", 40f, y, bodyPaint)
        y += 14f
        canvas.drawText("Ciclo habitual: ${profile.habitualCycleLength} dias    Menstruação habitual: ${profile.habitualPeriodDuration} dias    Regularidade: ${if (profile.isRegularCycle) "Regular" else "Irregular"}", 40f, y, bodyPaint)
        y += 24f

        // Cycle Statistics
        canvas.drawText("2. ESTATÍSTICAS DOS CICLOS REGISTRADOS", 40f, y, sectionPaint)
        y += 16f
        canvas.drawText("Total de ciclos analisados: ${stats.cycleCount}", 40f, y, boldBodyPaint)
        y += 14f
        canvas.drawText("• Duração média do ciclo: ${stats.averageCycleLength} dias (Mediana: ${stats.medianCycleLength} dias)", 40f, y, bodyPaint)
        y += 14f
        canvas.drawText("• Variação dos ciclos: Mínimo ${stats.minCycleLength} dias | Máximo ${stats.maxCycleLength} dias | Desvio Padrão: ${stats.standardDeviation} dias", 40f, y, bodyPaint)
        y += 14f
        canvas.drawText("• Duração média do fluxo menstrual: ${stats.averagePeriodDuration} dias", 40f, y, bodyPaint)
        y += 24f

        // Recent Cycles Table
        canvas.drawText("3. CRONOLOGIA DOS CICLOS RECENTES", 40f, y, sectionPaint)
        y += 16f
        canvas.drawText("Início", 40f, y, boldBodyPaint)
        canvas.drawText("Término", 150f, y, boldBodyPaint)
        canvas.drawText("Duração", 270f, y, boldBodyPaint)
        canvas.drawText("Menstruação", 380f, y, boldBodyPaint)
        canvas.drawText("Tipo", 480f, y, boldBodyPaint)
        y += 6f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 14f

        val displayCycles = cycles.take(8)
        if (displayCycles.isEmpty()) {
            canvas.drawText("Nenhum ciclo completo registrado ainda.", 40f, y, bodyPaint)
            y += 16f
        } else {
            for (cycle in displayCycles) {
                canvas.drawText(cycle.startDate, 40f, y, bodyPaint)
                canvas.drawText(cycle.endDate ?: "Em curso", 150f, y, bodyPaint)
                canvas.drawText("${cycle.cycleLength ?: "-"} dias", 270f, y, bodyPaint)
                canvas.drawText("${cycle.periodDuration ?: "-"} dias", 380f, y, bodyPaint)
                canvas.drawText(if (cycle.isEstimated) "Estimado" else "Confirmado", 480f, y, bodyPaint)
                y += 14f
            }
        }
        y += 16f

        // Symptom & Health Overview
        canvas.drawText("4. REGISTROS DE SINTOMAS & SINAIS CORPORAIS", 40f, y, sectionPaint)
        y += 16f
        val allSymptoms = records.flatMap { it.symptoms }
        val symptomFrequencies = allSymptoms.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }
        if (symptomFrequencies.isNotEmpty()) {
            val topSymptoms = symptomFrequencies.take(6).joinToString(", ") { "${it.first} (${it.second}x)" }
            canvas.drawText("Sintomas mais frequentes: $topSymptoms", 40f, y, bodyPaint)
            y += 14f
        } else {
            canvas.drawText("Nenhum sintoma recorrente anotado no período.", 40f, y, bodyPaint)
            y += 14f
        }

        val bbtCount = records.count { it.basalTemperature != null }
        val lhCount = records.count { it.lhTest.name != "NOT_TAKEN" }
        canvas.drawText("Registros de Temperatura Basal: $bbtCount dias    Registros de Teste LH: $lhCount testes", 40f, y, bodyPaint)
        y += 30f

        // Legal & Medical Disclaimer
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 14f
        canvas.drawText("AVISO MÉDICO & CONFIDENCIALIDADE:", 40f, y, boldBodyPaint)
        y += 12f
        canvas.drawText("Este relatório reúne dados inseridos exclusivamente pela própria utilizadora no aplicativo móvel Alzira Joné.", 40f, y, footerPaint)
        y += 10f
        canvas.drawText("As previsões e estimativas possuem finalidade educativa e de acompanhamento pessoal e NÃO substituem", 40f, y, footerPaint)
        y += 10f
        canvas.drawText("diagnósticos médicos, consultas clínicas nem exames laboratoriais.", 40f, y, footerPaint)
        y += 14f
        canvas.drawText("Desenvolvido por Nduwa Kayongo | Contato: 942022933 | nduwak8@gmail.com", 40f, y, footerPaint)

        pdfDocument.finishPage(page)

        // Save PDF to cache dir
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val pdfFile = File(reportsDir, "relatorio_saude_alzirajone_${System.currentTimeMillis()}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return pdfFile
    }
}
