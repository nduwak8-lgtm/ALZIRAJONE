package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.local.entity.DailyRecord
import com.example.data.local.entity.MenstrualCycle
import com.example.data.local.entity.UserProfile
import com.example.domain.engine.CycleStats
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.util.PdfReportGenerator
import java.io.File

@Composable
fun ReportsScreen(
    profile: UserProfile,
    allCycles: List<MenstrualCycle>,
    allRecords: List<DailyRecord>,
    stats: CycleStats,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("Últimos 3 meses") }
    val periods = listOf("Último ciclo", "Últimos 3 meses", "Últimos 6 meses", "Últimos 12 meses", "Todos os registros")

    var showConsentDialog by remember { mutableStateOf(false) }
    var generatedFile by remember { mutableStateOf<File?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("reports_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Relatório de Saúde em PDF",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gere um documento organizado para consultas com sua médica ou ginecologista.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Period Selection
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Selecione o Período",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    periods.forEach { period ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedPeriod == period,
                                onClick = { selectedPeriod = period },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = period, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // Privacy Warning Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldDark)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Privacidade: Este relatório contém informações pessoais e íntimas de saúde. O arquivo é gerado no seu dispositivo e nunca é compartilhado automaticamente.",
                        style = MaterialTheme.typography.bodySmall.copy(color = EmeraldDark, fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        // Export Action Button
        item {
            Button(
                onClick = { showConsentDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("export_pdf_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar Relatório PDF", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            if (generatedFile != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "✓ Relatório PDF gerado com sucesso!",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = generatedFile?.name ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val file = generatedFile ?: return@Button
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório ALZIRA JONÉ"))
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Compartilhar / Visualizar PDF")
                        }
                    }
                }
            }
        }
    }

    // Consent Confirmation Dialog (Section 31)
    if (showConsentDialog) {
        AlertDialog(
            onDismissRequest = { showConsentDialog = false },
            title = {
                Text(
                    text = "Confirmar Exportação",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            },
            text = {
                Text(
                    text = "Este relatório contém informações pessoais de saúde inseridas por você (ciclos, sintomas, temperatura, medicamentos).\n\nDeseja gerar o documento PDF agora?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConsentDialog = false
                        isGenerating = true
                        val file = PdfReportGenerator.generateHealthReport(
                            context = context,
                            profile = profile,
                            cycles = allCycles,
                            records = allRecords,
                            stats = stats,
                            periodFilterLabel = selectedPeriod
                        )
                        generatedFile = file
                        isGenerating = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Exportar PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConsentDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
