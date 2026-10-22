package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StatusRequestYellow

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor, label) = when (status.uppercase()) {
        "FREE" -> Quad(
            Color(0xFF1B3826),
            StatusFreeGreen,
            StatusFreeGreen.copy(alpha = 0.5f),
            "🟢 AVAILABLE / FREE"
        )
        "BOOKED" -> Quad(
            Color(0xFF381B1B),
            StatusBookedRed,
            StatusBookedRed.copy(alpha = 0.5f),
            "🔴 BOOKED"
        )
        "PENDING", "REQUEST" -> Quad(
            Color(0xFF3A3317),
            StatusRequestYellow,
            StatusRequestYellow.copy(alpha = 0.5f),
            "🟡 REQUEST / PENDING"
        )
        "OFF", "BUSY" -> Quad(
            Color(0xFF262A33),
            Color(0xFF90A4AE),
            Color(0xFF455A64),
            "⚪ OFF / BUSY"
        )
        else -> Quad(
            Color(0xFF20242C),
            Color(0xFF78909C),
            Color(0xFF37474F),
            "⚪ NOT SET"
        )
    }

    Box(
        modifier = modifier
            .background(bgColor, shape = RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
