package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StudioAmber

@Composable
fun SubscriptionPaywallOverlay(
    onSubscribe: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Non-dismissable: user must subscribe */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("subscription_paywall_screen"),
            color = Color(0xFF131516)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF231B0E),
                                Color(0xFF17191C),
                                Color(0xFF131516)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // Golden Crown Icon
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(StudioAmber, Color(0xFF8C5C0D))
                                ),
                                shape = CircleShape
                            )
                            .border(2.dp, Color(0xFFFFD180), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👑",
                            fontSize = 38.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Limited Time Offer Badge (User requested: limited time offer in Hindi/English)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE65100).copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(text = "⚡ ", fontSize = 12.sp)
                            Text(
                                text = "LIMITED TIME OFFER • सीमित समय का ऑफर",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFB74D),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Main Title
                    Text(
                        text = "Studio Expo VIP Membership",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Expiration Notice
                    Text(
                        text = "Your 30-Day Free Trial Has Ended\nआपका 30 दिनों का फ्री ट्रायल पूरा हो चुका है",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF8A80),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Continue uninterrupted access to your studio calendar, client web link & bookings.\nअपने स्टूडियो का कैलेंडर और बुकिंग पोर्टल चालू रखने के लिए सब्सक्राइब करें।",
                        fontSize = 12.sp,
                        color = Color(0xFFB0B7C3),
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Price & Plan Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                Brush.horizontalGradient(
                                    listOf(StudioAmber, Color(0xFFFFD180), StudioAmber)
                                ),
                                RoundedCornerShape(18.dp)
                            ),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2228))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1 YEAR VIP PLAN / 1 साल",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioAmber
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = StatusFreeGreen.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusFreeGreen)
                                ) {
                                    Text(
                                        text = "85% OFF",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = StatusFreeGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "₹149",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4ADE80)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "/ 1 Year",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "₹999",
                                    fontSize = 15.sp,
                                    color = Color(0xFF757575),
                                    textDecoration = TextDecoration.LineThrough,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Text(
                                text = "Only ₹12.4/month • केवल ₹12 प्रति माह",
                                fontSize = 11.sp,
                                color = Color(0xFFFFD180),
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Features list
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PaywallFeatureRow("Unlimited Event Bookings / अनलिमिटेड बुकिंग")
                                PaywallFeatureRow("24/7 Client Booking Web Link / लाइव वेब पोर्टल")
                                PaywallFeatureRow("Direct WhatsApp Quotation / व्हाट्सएप कोटेशन")
                                PaywallFeatureRow("Date-wise Crew Management / क्रू मैनेजमेंट")
                                PaywallFeatureRow("Cloud Calendar Backup / सुरक्षित क्लाउड सिंक")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Big Action Button (User requested: subcripsion leva matenu button)
                    Button(
                        onClick = onSubscribe,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("btn_activate_subscription"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioAmber,
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "💳 ", fontSize = 18.sp)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Get 1 Year VIP Access @ ₹149",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "अभी सब्सक्राइब करें (Limited Time Offer)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF332005)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Safe & Instant activation guarantee
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🔒 ", fontSize = 12.sp)
                        Text(
                            text = "100% Safe & Instant Activation • तुरंत एक्टिवेशन",
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaywallFeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = StatusFreeGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFFE0E0E0),
            fontWeight = FontWeight.Medium
        )
    }
}
