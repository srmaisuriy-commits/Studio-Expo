package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StudioAmber
import com.example.ui.viewmodel.OwnerScreen

@Composable
fun OwnerBottomNavBar(
    currentScreen: OwnerScreen,
    pendingCount: Int,
    onScreenSelected: (OwnerScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("owner_bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == OwnerScreen.DASHBOARD,
            onClick = { onScreenSelected(OwnerScreen.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = "Dashboard",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = StudioAmber,
                indicatorColor = StudioAmber,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_dashboard")
        )

        NavigationBarItem(
            selected = currentScreen == OwnerScreen.CALENDAR,
            onClick = { onScreenSelected(OwnerScreen.CALENDAR) },
            icon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendar",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Calendar", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = StudioAmber,
                indicatorColor = StudioAmber,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_calendar")
        )

        NavigationBarItem(
            selected = currentScreen == OwnerScreen.REQUESTS,
            onClick = { onScreenSelected(OwnerScreen.REQUESTS) },
            icon = {
                if (pendingCount > 0) {
                    BadgedBox(badge = {
                        Badge(containerColor = StatusBookedRed) {
                            Text("$pendingCount", color = Color.White)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Requests",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Requests",
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            label = { Text("Requests", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = StudioAmber,
                indicatorColor = StudioAmber,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_requests")
        )

        NavigationBarItem(
            selected = currentScreen == OwnerScreen.PROFILE,
            onClick = { onScreenSelected(OwnerScreen.PROFILE) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = StudioAmber,
                indicatorColor = StudioAmber,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_profile")
        )
    }
}
