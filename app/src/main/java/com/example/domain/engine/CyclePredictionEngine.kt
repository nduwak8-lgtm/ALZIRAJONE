package com.example.domain.engine

import com.example.data.local.entity.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class CycleStats(
    val cycleCount: Int,
    val averageCycleLength: Double,
    val medianCycleLength: Int,
    val minCycleLength: Int,
    val maxCycleLength: Int,
    val standardDeviation: Double,
    val isRegular: Boolean,
    val averagePeriodDuration: Double,
    val lutealPhaseEstimatedDays: Int = 14,
    val hasSufficientData: Boolean = false
)

data class DayPrediction(
    val date: LocalDate,
    val dayOfCycle: Int,
    val estimatedPhase: String,
    val fertilityStage: String,
    val fertilityScore: Double, // 0.0 to 1.0
    val isProbableOvulation: Boolean,
    val isEstimatedPeriod: Boolean,
    val frequentHistoricalSymptoms: List<String>,
    val educationalNote: String
)

data class HormoneTrend(
    val estrogenLevel: String, // "Baixo", "Aumentando", "Pico provável", "Moderado"
    val lhLevel: String, // "Baixo", "Pico provável", "Diminuindo"
    val progesteroneLevel: String, // "Baixo", "Aumentando", "Elevado", "Diminuindo"
    val fshLevel: String, // "Baixo", "Leve elevação", "Estável"
    val disclaimer: String = "Estimativa educativa, não resultado laboratorial."
)

data class CurrentCycleStatus(
    val hasCycleData: Boolean,
    val currentDayOfCycle: Int,
    val cycleStartDate: LocalDate?,
    val estimatedNextPeriodDate: LocalDate?,
    val estimatedOvulationDate: LocalDate?,
    val fertileWindowStart: LocalDate?,
    val fertileWindowEnd: LocalDate?,
    val currentPhase: String,
    val fertilityLevel: String,
    val confidenceLevel: String, // "Alta", "Moderada", "Baixa", "Sem dados"
    val confidenceReason: String,
    val isLate: Boolean,
    val daysLate: Int,
    val hormoneTrend: HormoneTrend,
    val pregnancyPossibilityNote: String?,
    val dailyHealthAlert: String?
)

object CyclePredictionEngine {

    /**
     * Computes statistics from recorded cycles.
     */
    fun calculateCycleStats(
        cycles: List<MenstrualCycle>,
        profile: UserProfile
    ): CycleStats {
        val validLengths = cycles.mapNotNull { it.cycleLength }.filter { it in 15..60 }
        val validPeriods = cycles.mapNotNull { it.periodDuration }.filter { it in 1..15 }

        if (validLengths.isEmpty()) {
            return CycleStats(
                cycleCount = 0,
                averageCycleLength = 0.0,
                medianCycleLength = 0,
                minCycleLength = 0,
                maxCycleLength = 0,
                standardDeviation = 0.0,
                isRegular = true,
                averagePeriodDuration = 0.0,
                hasSufficientData = false
            )
        }

        val avgLength = validLengths.average()
        val sortedLengths = validLengths.sorted()
        val medianLength = sortedLengths[sortedLengths.size / 2]
        val minLength = sortedLengths.first()
        val maxLength = sortedLengths.last()

        val variance = validLengths.map { (it - avgLength) * (it - avgLength) }.average()
        val stdDev = sqrt(variance)
        val isRegular = stdDev <= 3.5

        val avgPeriod = if (validPeriods.isNotEmpty()) validPeriods.average() else profile.habitualPeriodDuration.toDouble()

        return CycleStats(
            cycleCount = validLengths.size,
            averageCycleLength = (avgLength * 10).roundToInt() / 10.0,
            medianCycleLength = medianLength,
            minCycleLength = minLength,
            maxCycleLength = maxLength,
            standardDeviation = (stdDev * 10).roundToInt() / 10.0,
            isRegular = isRegular,
            averagePeriodDuration = (avgPeriod * 10).roundToInt() / 10.0,
            hasSufficientData = validLengths.size >= 2
        )
    }

    /**
     * Determines current cycle status, fertile window and phase for a specific date (usually today).
     */
    fun evaluateCurrentStatus(
        today: LocalDate,
        latestCycle: MenstrualCycle?,
        allCycles: List<MenstrualCycle>,
        recentRecords: List<DailyRecord>,
        profile: UserProfile
    ): CurrentCycleStatus {
        if (latestCycle == null) {
            return CurrentCycleStatus(
                hasCycleData = false,
                currentDayOfCycle = 0,
                cycleStartDate = null,
                estimatedNextPeriodDate = null,
                estimatedOvulationDate = null,
                fertileWindowStart = null,
                fertileWindowEnd = null,
                currentPhase = "Ainda não existem registros",
                fertilityLevel = "Dados insuficientes",
                confidenceLevel = "Sem dados",
                confidenceReason = "Ainda não há dados suficientes para fazer uma estimativa confiável.",
                isLate = false,
                daysLate = 0,
                hormoneTrend = HormoneTrend(
                    estrogenLevel = "Não monitorado",
                    lhLevel = "Não monitorado",
                    progesteroneLevel = "Não monitorado",
                    fshLevel = "Não monitorado"
                ),
                pregnancyPossibilityNote = null,
                dailyHealthAlert = null
            )
        }

        val cycleStartDate = try {
            LocalDate.parse(latestCycle.startDate)
        } catch (_: Exception) {
            today
        }

        val stats = calculateCycleStats(allCycles, profile)
        val expectedCycleLength = if (stats.hasSufficientData) {
            stats.averageCycleLength.roundToInt().coerceIn(19, 45)
        } else {
            profile.habitualCycleLength.coerceIn(19, 45)
        }
        val lutealDays = stats.lutealPhaseEstimatedDays

        // Ovulation is calculated based on cycle length minus luteal phase, adjusted by LH/Mucus/BBT if recorded
        var estimatedOvulationDate = cycleStartDate.plusDays((expectedCycleLength - lutealDays).toLong())

        // Check recent LH test records
        val recentLhPositive = recentRecords.firstOrNull {
            it.lhTest == LhTestResult.POSITIVE || it.lhTest == LhTestResult.PEAK
        }
        if (recentLhPositive != null) {
            try {
                val lhDate = LocalDate.parse(recentLhPositive.date)
                // LH surge precedes ovulation by ~24-36 hours (1-2 days)
                estimatedOvulationDate = lhDate.plusDays(1)
            } catch (_: Exception) {}
        }

        val fertileStart = estimatedOvulationDate.minusDays(5)
        val fertileEnd = estimatedOvulationDate.plusDays(1)
        val estimatedNextPeriod = cycleStartDate.plusDays(expectedCycleLength.toLong())

        val dayOfCycle = (ChronoUnit.DAYS.between(cycleStartDate, today) + 1).toInt().coerceAtLeast(1)

        // Evaluate phase
        val periodDuration = if (stats.averagePeriodDuration > 0) stats.averagePeriodDuration.roundToInt().coerceIn(2, 10) else profile.habitualPeriodDuration.coerceIn(2, 10)
        val currentPhase = when {
            dayOfCycle <= periodDuration -> "Fase Menstrual"
            today.isBefore(fertileStart) -> "Fase Folicular"
            today.isBefore(estimatedOvulationDate) -> "Janela Fértil"
            today.isEqual(estimatedOvulationDate) -> "Ovulação Provável"
            today.isBefore(estimatedNextPeriod) -> "Fase Lútea"
            else -> "Atraso Menstrual"
        }

        // Evaluate fertility stage
        val fertilityLevel = when {
            dayOfCycle <= periodDuration -> "Baixa probabilidade"
            today.isBefore(fertileStart.minusDays(2)) -> "Baixa probabilidade"
            today.isBefore(fertileStart) -> "Probabilidade crescente"
            today.isEqual(estimatedOvulationDate) -> "Ovulação provável"
            today.isAfter(fertileStart) && today.isBefore(fertileEnd) -> "Alta probabilidade"
            today.isEqual(fertileEnd) -> "Pós-ovulação provável"
            else -> "Baixa probabilidade"
        }

        // Confidence calculation
        val hasLh = recentRecords.any { it.lhTest == LhTestResult.POSITIVE || it.lhTest == LhTestResult.PEAK }
        val hasMucus = recentRecords.any { it.cervicalMucus.fertileIndicator }
        val hasBbt = recentRecords.count { it.basalTemperature != null } >= 5
        val cycleCount = stats.cycleCount

        val (confidenceLevel, confidenceReason) = when {
            cycleCount >= 3 && (hasLh || (hasMucus && hasBbt)) -> {
                "Alta" to "Baseada em ${stats.cycleCount} ciclos registrados e sinais concordantes de fertilidade (muco/LH/temperatura)."
            }
            cycleCount >= 2 && stats.isRegular -> {
                "Moderada" to "Baseada em ciclos com boa regularidade (${stats.averageCycleLength} dias de média). Registre muco e LH para maior precisão."
            }
            stats.standardDeviation > 4.5 -> {
                "Baixa" to "Seu ciclo apresenta variação elevada (${stats.minCycleLength} a ${stats.maxCycleLength} dias). Os sinais corporais fornecem mais precisão que apenas o calendário."
            }
            cycleCount <= 1 -> {
                "Baixa" to "Dados históricos iniciais. À medida que você registrar mais ciclos, a estimativa se tornará cada vez mais personalizada."
            }
            else -> {
                "Moderada" to "Baseada nos seus registros históricos recentes."
            }
        }

        // Delay checking
        val isLate = today.isAfter(estimatedNextPeriod)
        val daysLate = if (isLate) ChronoUnit.DAYS.between(estimatedNextPeriod, today).toInt() else 0

        // Hormone trend (educational)
        val hormoneTrend = when (currentPhase) {
            "Fase Menstrual" -> HormoneTrend(
                estrogenLevel = "Baixo",
                lhLevel = "Baixo",
                progesteroneLevel = "Baixo",
                fshLevel = "Leve elevação inicial"
            )
            "Fase Folicular" -> HormoneTrend(
                estrogenLevel = "Aumentando progressivamente",
                lhLevel = "Baixo / Estável",
                progesteroneLevel = "Baixo",
                fshLevel = "Estável para seleção folicular"
            )
            "Janela Fértil" -> HormoneTrend(
                estrogenLevel = "Pico pré-ovulatório",
                lhLevel = "Iniciando aumento ou onda",
                progesteroneLevel = "Baixo",
                fshLevel = "Onda pré-ovulatória"
            )
            "Ovulação Provável" -> HormoneTrend(
                estrogenLevel = "Queda transitória pós-pico",
                lhLevel = "Pico máximo de LH",
                progesteroneLevel = "Início discreto de secreção",
                fshLevel = "Pico associado"
            )
            "Fase Lútea" -> HormoneTrend(
                estrogenLevel = "Segundo platô moderado",
                lhLevel = "Baixo",
                progesteroneLevel = "Elevado (fase do corpo lúteo)",
                fshLevel = "Baixo"
            )
            else -> HormoneTrend(
                estrogenLevel = "Variável",
                lhLevel = "Baixo",
                progesteroneLevel = "Em declínio ou sustentado",
                fshLevel = "Variável"
            )
        }

        // Pregnancy possibility evaluation
        val recentSexInFertileWindow = recentRecords.any {
            try {
                val d = LocalDate.parse(it.date)
                it.sexualActivity && !it.protectionUsed && !d.isBefore(fertileStart) && !d.isAfter(fertileEnd)
            } catch (_: Exception) { false }
        }

        val pregnancyNote = when {
            isLate && daysLate >= 3 && recentSexInFertileWindow ->
                "Possibilidade a considerar: Você registrou relação sem proteção no período fértil e seu período está atrasado há $daysLate dias. Pode haver motivo para considerar um teste de gravidez."
            isLate && daysLate >= 5 ->
                "Seu período está além da previsão em $daysLate dias. Fatores como estresse, rotina ou alterações naturais podem influenciar. Se desejar, considere realizar um teste de farmácia ou buscar orientação médica."
            else -> null
        }

        // Health alert based on latest record
        val latestRecord = recentRecords.firstOrNull()
        val healthAlert = when {
            latestRecord?.flow == FlowLevel.VERY_INTENSE ->
                "Alerta de saúde: Fluxo muito intenso registrado. Se você notar fraqueza, cólicas severas ou sangramento anormal prolongado, procure um profissional de saúde."
            (latestRecord?.painLevel ?: 0) >= 8 ->
                "Alerta de dor: Nível de dor elevado (${latestRecord?.painLevel}/10). Mantenha repouso e, caso a dor persista ou não responda aos cuidados habituais, busque orientação médica."
            else -> null
        }

        return CurrentCycleStatus(
            hasCycleData = true,
            currentDayOfCycle = dayOfCycle,
            cycleStartDate = cycleStartDate,
            estimatedNextPeriodDate = estimatedNextPeriod,
            estimatedOvulationDate = estimatedOvulationDate,
            fertileWindowStart = fertileStart,
            fertileWindowEnd = fertileEnd,
            currentPhase = currentPhase,
            fertilityLevel = fertilityLevel,
            confidenceLevel = confidenceLevel,
            confidenceReason = confidenceReason,
            isLate = isLate,
            daysLate = daysLate,
            hormoneTrend = hormoneTrend,
            pregnancyPossibilityNote = pregnancyNote,
            dailyHealthAlert = healthAlert
        )
    }

    /**
     * Generates a 7-day outlook (+1 to +7 days) with expected phases and frequent symptoms.
     */
    fun generateNextDaysOutlook(
        today: LocalDate,
        status: CurrentCycleStatus,
        historicalRecords: List<DailyRecord>
    ): List<DayPrediction> {
        if (!status.hasCycleData || status.fertileWindowStart == null || status.estimatedOvulationDate == null || status.estimatedNextPeriodDate == null) {
            return emptyList()
        }
        val daysAhead = listOf(1, 2, 3, 4, 5, 7)
        val results = mutableListOf<DayPrediction>()

        // Analyze which symptoms occurred at similar cycle days in the past
        val symptomMapByDay = mutableMapOf<Int, MutableMap<String, Int>>()
        // Populate historical frequency
        for (r in historicalRecords) {
            r.symptoms.forEach { symptom ->
                val countMap = symptomMapByDay.getOrPut(0) { mutableMapOf() }
                countMap[symptom] = (countMap[symptom] ?: 0) + 1
            }
        }

        for (offset in daysAhead) {
            val targetDate = today.plusDays(offset.toLong())
            val targetDay = status.currentDayOfCycle + offset

            val phase = when {
                targetDate.isBefore(status.fertileWindowStart) -> "Fase Folicular"
                targetDate.isBefore(status.estimatedOvulationDate) -> "Janela Fértil"
                targetDate.isEqual(status.estimatedOvulationDate) -> "Ovulação Provável"
                targetDate.isBefore(status.estimatedNextPeriodDate) -> "Fase Lútea"
                targetDate.isEqual(status.estimatedNextPeriodDate) -> "Menstruação Prevista"
                else -> "Fase Menstrual Prevista"
            }

            val fertility = when {
                targetDate.isBefore(status.fertileWindowStart) -> "Baixa probabilidade"
                targetDate.isEqual(status.estimatedOvulationDate) -> "Ovulação provável"
                targetDate.isAfter(status.fertileWindowStart) && targetDate.isBefore(status.fertileWindowEnd) -> "Alta probabilidade"
                targetDate.isEqual(status.fertileWindowEnd) -> "Pós-ovulação provável"
                else -> "Baixa probabilidade"
            }

            val isOvulation = targetDate.isEqual(status.estimatedOvulationDate)
            val isPeriod = !targetDate.isBefore(status.estimatedNextPeriodDate) &&
                    targetDate.isBefore(status.estimatedNextPeriodDate.plusDays(5))

            // Suggestions based on phase
            val suggestions = when (phase) {
                "Fase Folicular" -> listOf("Boa energia", "Pele favorável")
                "Janela Fértil" -> listOf("Sensibilidade aumentada", "Muco elástico provável")
                "Ovulação Provável" -> listOf("Leve pontada pélvica", "Aumento da libido")
                "Fase Lútea" -> listOf("Possível sensibilidade mamária", "Mudança leve de humor")
                else -> listOf("Possíveis cólicas leves", "Necessidade de repouso")
            }

            results.add(
                DayPrediction(
                    date = targetDate,
                    dayOfCycle = targetDay,
                    estimatedPhase = phase,
                    fertilityStage = fertility,
                    fertilityScore = if (isOvulation) 0.95 else if (phase == "Janela Fértil") 0.8 else 0.2,
                    isProbableOvulation = isOvulation,
                    isEstimatedPeriod = isPeriod,
                    frequentHistoricalSymptoms = suggestions,
                    educationalNote = "Estimativa baseada no seu padrão de ciclo."
                )
            )
        }

        return results
    }

    /**
     * Contextual daily wellness tips according to current cycle phase.
     */
    fun getDailyTips(phase: String): List<String> {
        return when (phase) {
            "Fase Menstrual" -> listOf(
                "Hidratação constante: Beba bastante água para auxiliar na circulação e alívio do inchaço.",
                "Descanso e conforto: Priorize noites reparadoras de sono e compressas mornas na região lombar ou pélvica se sentir cólicas.",
                "Acompanhe o fluxo: Registre a intensidade do sangramento para manter o histórico médico atualizado."
            )
            "Fase Folicular" -> listOf(
                "Energia em ascensão: O aumento natural do estrogênio costuma favorecer a disposição física e criativa.",
                "Nutrição balanceada: Aproveite para incluir folhas verdes, proteínas magras e fibras na rotina.",
                "Observe seu corpo: Comece a notar as sutis mudanças na textura do muco cervical."
            )
            "Janela Fértil", "Ovulação Provável" -> listOf(
                "Compreendendo a fertilidade: A janela fértil abrange os dias que antecedem a ovulação e o próprio dia do óvulo.",
                "Muco fértil: Muco transparente, escorregadio e elástico é um ótimo indicador natural de fertilidade.",
                "Teste de LH e BBT: Se estiver testando ovulação, faça o teste de LH no mesmo horário e continue registrando a temperatura."
            )
            "Fase Lútea" -> listOf(
                "Acolha suas sensações: A progesterona elevada pode trazer maior sensibilidade ou retenção de líquidos.",
                "Cuidados antiestresse: Práticas relaxantes, como chás suaves e caminhadas leves, são muito bem-vindas.",
                "Registro de sintomas: Anotar sintomas diários ajuda você e seu médico a identificarem padrões pré-menstruais."
            )
            else -> listOf(
                "Acompanhamento atento: Mantenha seus registros diários para que o algoritmo compreenda a extensão do ciclo.",
                "Orientação profissional: Lembre-se que atrasos ocasionais são comuns por estresse ou viagens, mas persistência deve ser avaliada por ginecologista."
            )
        }
    }
}
