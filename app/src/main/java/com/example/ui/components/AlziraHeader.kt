package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@Composable
fun AlziraHeader(
    userName: String,
    avatarUri: String?,
    viewMode: String,
    isPinProtected: Boolean,
    onAvatarClick: () -> Unit,
    onModeClick: () -> Unit,
    onLockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("alzira_header"),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onAvatarClick() }
                    .testTag("avatar_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, GoldAccent, CircleShape)
                ) {
                    if (!avatarUri.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "Foto de perfil de Alzira",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Official identity honoring Alzira Joné's exact visual identity
                        Image(
                            painter = painterResource(id = R.drawable.official_app_identity_image),
                            contentDescription = "Alzira Joné - Identidade Oficial",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ALZIRA JONÉ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                color = EmeraldPrimary
                            )
                        )
                    }
                    Text(
                        text = "Seu ciclo. Seus dados. Seu ritmo.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Quick Actions: Mode & Lock
            Row(verticalAlignment = Alignment.CenterVertically) {
                // View Mode Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (viewMode) {
                        "PRIVATE" -> MaterialTheme.colorScheme.secondaryContainer
                        "DISCREET" -> MaterialTheme.colorScheme.surfaceVariant
                        else -> EmeraldLight
                    },
                    modifier = Modifier
                        .clickable { onModeClick() }
                        .testTag("view_mode_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (viewMode) {
                                "PRIVATE" -> Icons.Default.VisibilityOff
                                "DISCREET" -> Icons.AutoMirrored.Filled.Notes
                                else -> Icons.Default.Shield
                            },
                            contentDescription = "Modo de visualização",
                            tint = when (viewMode) {
                                "PRIVATE" -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> EmeraldDark
                            },
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (viewMode) {
                                "PRIVATE" -> "Privado"
                                "DISCREET" -> "Discreto"
                                else -> "Normal"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = when (viewMode) {
                                    "PRIVATE" -> MaterialTheme.colorScheme.onSecondaryContainer
                                    else -> EmeraldDark
                                }
                            )
                        )
                    }
                }

                if (isPinProtected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onLockClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("quick_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloquear aplicativo agora",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
