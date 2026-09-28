package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PartnerSharing
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary

@Composable
fun PartnerScreen(
    sharing: PartnerSharing,
    onUpdateSharing: (PartnerSharing) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEnabled by remember(sharing) { mutableStateOf(sharing.isEnabled) }
    var shareNextPeriod by remember(sharing) { mutableStateOf(sharing.shareNextPeriod) }
    var shareFertileWindow by remember(sharing) { mutableStateOf(sharing.shareFertileWindow) }
    var shareReminders by remember(sharing) { mutableStateOf(sharing.shareReminders) }
    var shareMood by remember(sharing) { mutableStateOf(sharing.shareMood) }
    var shareNotes by remember(sharing) { mutableStateOf(sharing.shareNotes) }
    var shareIntimateSymptoms by remember(sharing) { mutableStateOf(sharing.shareIntimateSymptoms) }
    var shareSexualActivity by remember(sharing) { mutableStateOf(sharing.shareSexualActivity) }
    var shareMedications by remember(sharing) { mutableStateOf(sharing.shareMedications) }

    fun notifyUpdate() {
        onUpdateSharing(
            sharing.copy(
                isEnabled = isEnabled,
                shareNextPeriod = shareNextPeriod,
                shareFertileWindow = shareFertileWindow,
                shareReminders = shareReminders,
                shareMood = shareMood,
                shareNotes = shareNotes,
                shareIntimateSymptoms = shareIntimateSymptoms,
                shareSexualActivity = shareSexualActivity,
                shareMedications = shareMedications
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("partner_screen"),
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
                        Icon(Icons.Default.Group, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Modo Parceiro",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Compartilhamento consciente e granular. Você escolhe exatamente quais dados podem ser visualizados.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Connection Switch & Invite Code
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
                        Column {
                            Text(
                                text = "Ativar Compartilhamento",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isEnabled) "Parceiro pode acessar dados autorizados" else "Acesso bloqueado",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = {
                                isEnabled = it
                                notifyUpdate()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }

                    if (isEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Código de Convite do Parceiro",
                                        style = MaterialTheme.typography.labelSmall.copy(color = EmeraldDark)
                                    )
                                    Text(
                                        text = sharing.accessCode,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 2.sp,
                                            color = EmeraldDark
                                        )
                                    )
                                }
                                Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldDark)
                            }
                        }
                    }
                }
            }
        }

        // Granular Permissions
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Permissões de Compartilhamento",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Dados íntimos (como atividade sexual e notas privadas) permanecem desmarcados por padrão para sua proteção.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SharingCheckbox(
                        label = "Próxima menstruação",
                        checked = shareNextPeriod,
                        onCheckedChange = { shareNextPeriod = it; notifyUpdate() }
                    )
                    SharingCheckbox(
                        label = "Período fértil",
                        checked = shareFertileWindow,
                        onCheckedChange = { shareFertileWindow = it; notifyUpdate() }
                    )
                    SharingCheckbox(
                        label = "Lembretes e rotina",
                        checked = shareReminders,
                        onCheckedChange = { shareReminders = it; notifyUpdate() }
                    )
                    SharingCheckbox(
                        label = "Humor registrado",
                        checked = shareMood,
                        onCheckedChange = { shareMood = it; notifyUpdate() }
                    )
                    SharingCheckbox(
                        label = "Notas gerais",
                        checked = shareNotes,
                        onCheckedChange = { shareNotes = it; notifyUpdate() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    SharingCheckbox(
                        label = "Sintomas íntimos",
                        checked = shareIntimateSymptoms,
                        onCheckedChange = { shareIntimateSymptoms = it; notifyUpdate() }
                    )
                    SharingCheckbox(
                        label = "Atividade sexual",
                        checked = shareSexualActivity,
                        onCheckedChange = { shareSexualActivity = it; notifyUpdate() }
                    )
                    SharingCheckbox(
                        label = "Medicamentos",
                        checked = shareMedications,
                        onCheckedChange = { shareMedications = it; notifyUpdate() }
                    )
                }
            }
        }
    }
}

@Composable
fun SharingCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
