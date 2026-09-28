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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserProfile
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary

@Composable
fun SecurityScreen(
    profile: UserProfile,
    onSetPin: (String) -> Unit,
    onRemovePin: () -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onSetTimeout: (Int) -> Unit,
    onSetViewMode: (String) -> Unit,
    onLockNow: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var showDeleteStep1Dialog by remember { mutableStateOf(false) }
    var showDeleteStep2Dialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("security_screen"),
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
                        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Segurança & Privacidade",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Proteja seus dados íntimos com criptografia local, PIN e bloqueio inteligente.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Section: Modos de Visualização
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Modo de Visualização",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Alterne rapidamente para proteger o que é exibido na tela.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        Triple("NORMAL", "Modo Normal", "Exibe todas as informações e registros completos."),
                        Triple("PRIVATE", "Modo Privado", "Oculta automaticamente atividade sexual e anotações íntimas."),
                        Triple("DISCREET", "Modo Discreto", "Visualização neutra para uso em locais públicos.")
                    ).forEach { (modeKey, title, desc) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSetViewMode(modeKey) }
                        ) {
                            RadioButton(
                                selected = profile.currentViewMode == modeKey,
                                onClick = { onSetViewMode(modeKey) },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                        }
                    }
                }
            }
        }

        // Section: PIN & Biometria
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bloqueio do Aplicativo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Código PIN de 4 Dígitos", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = if (!profile.pinHash.isNullOrBlank()) "PIN configurado e ativo" else "Nenhum PIN configurado",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        if (profile.pinHash.isNullOrBlank()) {
                            Button(
                                onClick = { showPinDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Criar PIN")
                            }
                        } else {
                            Row {
                                OutlinedButton(
                                    onClick = { showPinDialog = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Trocar")
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                TextButton(onClick = onRemovePin) {
                                    Text("Remover", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Desbloqueio por Biometria", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "Impressão digital compatível do Android",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = profile.isBiometricEnabled,
                            onCheckedChange = onToggleBiometric,
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }

                    if (!profile.pinHash.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onLockNow,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bloquear Aplicativo Agora", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Tempo de Bloqueio Automático
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bloqueio Automático ao Sair",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        0 to "Imediatamente",
                        30 to "Após 30 segundos",
                        60 to "Após 1 minuto",
                        300 to "Após 5 minutos",
                        -1 to "Nunca bloquear automaticamente"
                    ).forEach { (timeout, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSetTimeout(timeout) }
                        ) {
                            RadioButton(
                                selected = profile.lockTimeoutSeconds == timeout,
                                onClick = { onSetTimeout(timeout) },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // Section: Exclusão de Dados (Section 47 & 48)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth().testTag("delete_data_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacidade & Exclusão de Dados",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Exclua todos os dados íntimos, ciclos, sintomas e registros pessoais armazenados localmente.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showDeleteStep1Dialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("delete_my_data_button")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apagar Meus Dados", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Step 1: Initial Deletion Confirmation Dialog
    if (showDeleteStep1Dialog) {
        AlertDialog(
            onDismissRequest = { showDeleteStep1Dialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Apagar todos os dados?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Todos os ciclos, registros, sintomas e dados pessoais serão excluídos definitivamente deste aparelho.\n\nEsta operação não pode ser desfeita.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteStep1Dialog = false
                        showDeleteStep2Dialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Continuar exclusão")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteStep1Dialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Step 2: Final Double Confirmation Dialog
    if (showDeleteStep2Dialog) {
        AlertDialog(
            onDismissRequest = { showDeleteStep2Dialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Confirmação Final", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = {
                Text(
                    "Tem certeza absoluta? O banco de dados será completamente zerado e o aplicativo voltará à tela inicial como se estivesse acabado de ser instalado.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteStep2Dialog = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sim, excluir tudo agora", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteStep2Dialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Definir PIN de 4 Dígitos", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        label = { Text("Novo PIN (4 números)") }
                    )
                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4) confirmPinInput = it },
                        label = { Text("Confirmar PIN") }
                    )
                    if (errorMsg != null) {
                        Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length != 4) {
                            errorMsg = "O PIN deve conter exatamente 4 dígitos."
                        } else if (pinInput != confirmPinInput) {
                            errorMsg = "Os PINs digitados não coincidem."
                        } else {
                            onSetPin(pinInput)
                            showPinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Salvar PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
