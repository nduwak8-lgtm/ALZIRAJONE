package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AppReminder
import com.example.data.local.entity.Medication
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicationsScreen(
    medications: List<Medication>,
    reminders: List<AppReminder>,
    onToggleTaken: (Medication) -> Unit,
    onDeleteMedication: (Medication) -> Unit,
    onAddMedication: (name: String, dosage: String, time: String, isContraceptive: Boolean, type: String, notes: String) -> Unit,
    onToggleReminder: (AppReminder) -> Unit,
    onDeleteReminder: (AppReminder) -> Unit,
    onAddReminder: (title: String, message: String, time: String, isSecret: Boolean, type: String) -> Unit,
    onTestNotification: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddMedDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var testFeedback by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("medications_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Medicamentos & Lembretes",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary
                            )
                            Text(
                                text = "Notificações automáticas em segundo plano e lembretes",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }

        // Section: Notificações Automáticas em Segundo Plano (Mesmo com app fechado)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("background_notifications_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Notificações Automáticas Ativas",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = EmeraldLight) {
                            Text(
                                text = "Ativo 24/7",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = EmeraldDark, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "O calendário envia alertas automáticos mesmo que você não abra o aplicativo:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("• 🩸 Início do período menstrual (aviso prévio e no dia)", style = MaterialTheme.typography.bodySmall)
                        Text("• ⚠️ Aviso de atraso do ciclo menstrual", style = MaterialTheme.typography.bodySmall)
                        Text("• 🌸 Início da janela fértil e dia provável de ovulação", style = MaterialTheme.typography.bodySmall)
                        Text("• 📅 Actividades agendadas no calendário", style = MaterialTheme.typography.bodySmall)
                        Text("• 💊 Lembretes de tomar medicamentos e pílula", style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Disparar notificação de teste agora:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                onTestNotification("PERIOD_START")
                                testFeedback = "Notificação de Início de Período disparada!"
                            },
                            label = { Text("🩸 Início Menstruação") }
                        )
                        SuggestionChip(
                            onClick = {
                                onTestNotification("DELAY")
                                testFeedback = "Alerta de Atraso Menstrual disparado!"
                            },
                            label = { Text("⚠️ Atraso Menstrual") }
                        )
                        SuggestionChip(
                            onClick = {
                                onTestNotification("FERTILE")
                                testFeedback = "Notificação de Janela Fértil disparada!"
                            },
                            label = { Text("🌸 Janela Fértil") }
                        )
                        SuggestionChip(
                            onClick = {
                                onTestNotification("OVULATION")
                                testFeedback = "Notificação de Dia de Ovulação disparada!"
                            },
                            label = { Text("🥚 Ovulação") }
                        )
                        SuggestionChip(
                            onClick = {
                                onTestNotification("MEDICATION")
                                testFeedback = "Lembrete de Medicamento disparado!"
                            },
                            label = { Text("💊 Medicamento") }
                        )
                        SuggestionChip(
                            onClick = {
                                onTestNotification("ACTIVITY")
                                testFeedback = "Lembrete de Actividade disparado!"
                            },
                            label = { Text("📅 Actividade") }
                        )
                    }

                    if (testFeedback.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ $testFeedback",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        // Section: Medicamentos e Contraceptivos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Medicamentos & Contraceptivos",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = { showAddMedDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (medications.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nenhum medicamento ou contraceptivo cadastrado. Toque em 'Adicionar' para cadastrar.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        } else {
            items(medications) { med ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            IconButton(onClick = { onToggleTaken(med) }) {
                                Icon(
                                    imageVector = if (med.isTakenToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Marcar tomado",
                                    tint = if (med.isTakenToday) EmeraldPrimary else MaterialTheme.colorScheme.outline
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = med.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${med.dosage} • ${med.timeOfDay} • ${if (med.isContraceptive) "Contraceptivo" else "Medicamento"}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                if (med.notes.isNotBlank()) {
                                    Text(
                                        text = med.notes,
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { onDeleteMedication(med) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Section: Lembretes e Notificações Secretas
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lembretes & Notificações",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "🔒 Modo privado: notificações discretas",
                        style = MaterialTheme.typography.labelSmall.copy(color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                    )
                }
                Button(
                    onClick = { showAddReminderDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Criar", fontWeight = FontWeight.Bold)
                }
            }
        }

        items(reminders) { rem ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rem.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            if (rem.isSecret) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldLight
                                ) {
                                    Text(
                                        text = "🔒 Secreto",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = EmeraldDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Horário: ${rem.time} • ${if (rem.isSecret) rem.secretMessage else rem.message}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = rem.isEnabled,
                            onCheckedChange = { onToggleReminder(rem) },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                        IconButton(onClick = { onDeleteReminder(rem) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    // Add Medication Dialog
    if (showAddMedDialog) {
        var name by remember { mutableStateOf("") }
        var dosage by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("08:00") }
        var isContraceptive by remember { mutableStateOf(false) }
        var type by remember { mutableStateOf("Pílula") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddMedDialog = false },
            title = { Text("Novo Medicamento / Contraceptivo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome do medicamento") })
                    OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dose (ex: 1 comprimido, 50mg)") })
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Horário (HH:mm)") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isContraceptive, onCheckedChange = { isContraceptive = it })
                        Text("É método contraceptivo?")
                    }
                    if (isContraceptive) {
                        OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo (Pílula, Injeção, DIU, Implante)") })
                    }
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Observação") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddMedication(name, dosage, time, isContraceptive, type, notes)
                            showAddMedDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMedDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Add Reminder Dialog
    if (showAddReminderDialog) {
        var title by remember { mutableStateOf("Lembrete diário") }
        var message by remember { mutableStateOf("Hora do seu registro de saúde") }
        var time by remember { mutableStateOf("20:00") }
        var isSecret by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = { Text("Novo Lembrete", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") })
                    OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Mensagem interna") })
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Horário (HH:mm)") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isSecret, onCheckedChange = { isSecret = it })
                        Text("Lembrete privado / secreto (notificação neutra)")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddReminder(title, message, time, isSecret, "CUSTOM")
                        showAddReminderDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReminderDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
