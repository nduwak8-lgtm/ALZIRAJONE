package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.AppScreen

@Composable
fun AlziraBottomNavBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("alzira_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        val isToday = currentScreen == AppScreen.DASHBOARD
        val isCalendar = currentScreen == AppScreen.CALENDAR
        val isLog = currentScreen == AppScreen.DAILY_LOG
        val isStats = currentScreen == AppScreen.STATISTICS
        val isMore = currentScreen in listOf(
            AppScreen.MEDICATIONS,
            AppScreen.REPORTS,
            AppScreen.PARTNER,
            AppScreen.SETTINGS_SECURITY,
            AppScreen.ABOUT
        )

        NavigationBarItem(
            selected = isToday,
            onClick = { onNavigate(AppScreen.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = if (isToday) Icons.Filled.Spa else Icons.Outlined.Spa,
                    contentDescription = "Hoje",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Hoje",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_item_today")
        )

        NavigationBarItem(
            selected = isCalendar,
            onClick = { onNavigate(AppScreen.CALENDAR) },
            icon = {
                Icon(
                    imageVector = if (isCalendar) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = "Calendário",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Calendário",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isCalendar) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_item_calendar")
        )

        NavigationBarItem(
            selected = isLog,
            onClick = { onNavigate(AppScreen.DAILY_LOG) },
            icon = {
                Icon(
                    imageVector = if (isLog) Icons.Filled.AddCircle else Icons.Outlined.AddCircleOutline,
                    contentDescription = "Registrar",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Registrar",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isLog) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_item_log")
        )

        NavigationBarItem(
            selected = isStats,
            onClick = { onNavigate(AppScreen.STATISTICS) },
            icon = {
                Icon(
                    imageVector = if (isStats) Icons.Filled.Insights else Icons.Outlined.Insights,
                    contentDescription = "Análises",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Análises",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isStats) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_item_stats")
        )

        NavigationBarItem(
            selected = isMore,
            onClick = { onNavigate(AppScreen.MEDICATIONS) },
            icon = {
                Icon(
                    imageVector = if (isMore) Icons.Filled.Widgets else Icons.Outlined.Widgets,
                    contentDescription = "Menu",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isMore) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_item_menu")
        )
    }
}
