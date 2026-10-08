package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.R
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StudioAmber
import com.example.ui.viewmodel.AppRole

@Composable
fun StudioTopBar(
    currentRole: AppRole,
    studioName: String,
    onRoleSelected: (AppRole) -> Unit,
    onShareClick: (() -> Unit)? = null,
    onMessageTemplateClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Main Branding Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Studio Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = studioName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (currentRole == AppRole.OWNER) "Studio Owner Dashboard 🔐" else "Customer Booking Portal 🌐",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (currentRole == AppRole.OWNER) StudioAmber else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (currentRole == AppRole.OWNER) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onMessageTemplateClick != null) {
                            IconButton(
                                onClick = onMessageTemplateClick,
                                modifier = Modifier.testTag("topbar_template_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = "Set WhatsApp Message Template (મેસેજ સેટ કરો)",
                                    tint = StudioAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        if (onShareClick != null) {
                            IconButton(
                                onClick = onShareClick,
                                modifier = Modifier.testTag("topbar_share_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share WhatsApp",
                                    tint = StatusFreeGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual Access Mode Switcher (Owner vs Customer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RoleToggleButton(
                    title = "👤 Owner Mode",
                    subtitle = "Manage & Confirm",
                    isSelected = currentRole == AppRole.OWNER,
                    icon = Icons.Default.Lock,
                    onClick = { onRoleSelected(AppRole.OWNER) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_owner_mode_btn")
                )

                Spacer(modifier = Modifier.width(4.dp))

                RoleToggleButton(
                    title = "👥 Customer View",
                    subtitle = "Public Link Flow",
                    isSelected = currentRole == AppRole.CUSTOMER,
                    icon = Icons.Default.Person,
                    onClick = { onRoleSelected(AppRole.CUSTOMER) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_customer_mode_btn")
                )
            }
        }
    }
}

@Composable
private fun RoleToggleButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    fontSize = 9.sp,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
