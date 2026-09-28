package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuSheet(
    onDismiss: () -> Unit,
    onNavigate: (AppScreen) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("more_menu_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Mais Opções & Recursos",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            )
            Text(
                text = "ALZIRA JONÉ • Gestão Completa de Saúde",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuItemRow(
                icon = Icons.Outlined.Medication,
                title = "Medicamentos & Lembretes",
                subtitle = "Contraceptivos, dosagens e notificações discretas",
                onClick = {
                    onNavigate(AppScreen.MEDICATIONS)
                    onDismiss()
                }
            )

            MenuItemRow(
                icon = Icons.Outlined.PictureAsPdf,
                title = "Relatório Médico em PDF",
                subtitle = "Exportação confidencial para consultas ginecológicas",
                onClick = {
                    onNavigate(AppScreen.REPORTS)
                    onDismiss()
                }
            )

            MenuItemRow(
                icon = Icons.Outlined.Group,
                title = "Modo Parceiro",
                subtitle = "Compartilhamento granular e seguro com código de acesso",
                onClick = {
                    onNavigate(AppScreen.PARTNER)
                    onDismiss()
                }
            )

            MenuItemRow(
                icon = Icons.Outlined.Security,
                title = "Segurança, PIN & Privacidade",
                subtitle = "Modos de visualização, biometria e tempo de bloqueio",
                onClick = {
                    onNavigate(AppScreen.SETTINGS_SECURITY)
                    onDismiss()
                }
            )

            MenuItemRow(
                icon = Icons.Outlined.Info,
                title = "Sobre Alzira Joné",
                subtitle = "Identidade visual, ícone personalizado e desenvolvedor",
                onClick = {
                    onNavigate(AppScreen.ABOUT)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun MenuItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = EmeraldLight,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
