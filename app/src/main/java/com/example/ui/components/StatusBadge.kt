package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusBookedRedLight
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StatusFreeGreenLight
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingAmberLight

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor, label) = when (status.uppercase()) {
        "FREE" -> Quad(StatusFreeGreenLight, Color(0xFF065F46), StatusFreeGreen, "🟢 FREE")
        "BOOKED" -> Quad(StatusBookedRedLight, Color(0xFF991B1B), StatusBookedRed, "🔴 BOOKED")
        "REQUEST" -> Quad(StatusPendingAmberLight, Color(0xFF92400E), StatusPendingAmber, "🟡 REQUEST")
        "CONFIRMED" -> Quad(StatusFreeGreenLight, Color(0xFF065F46), StatusFreeGreen, "✅ CONFIRMED")
        "PENDING" -> Quad(StatusPendingAmberLight, Color(0xFF92400E), StatusPendingAmber, "🟡 PENDING")
        "REJECTED" -> Quad(Color(0xFFF3F4F6), Color(0xFF4B5563), Color(0xFF9CA3AF), "❌ REJECTED")
        "NOT_SET", "NONE", "CLEAR" -> Quad(Color(0xFFF3F4F6), Color(0xFF4B5563), Color(0xFF9CA3AF), "⚪ CLEAR")
        else -> Quad(Color(0xFFE5E7EB), Color(0xFF374151), Color(0xFF6B7280), status)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
