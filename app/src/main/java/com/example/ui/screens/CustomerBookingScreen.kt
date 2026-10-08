package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.DateUtils
import com.example.data.model.BookingRequest
import com.example.data.model.DateStatus
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import com.example.data.model.WorkTypeCatalog
import com.example.ui.components.StatusBadge
import com.example.ui.components.StudioCalendarView
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StatusFreeGreenLight
import com.example.ui.theme.StudioAmber

@Composable
fun CustomerBookingScreen(
    profile: StudioProfile?,
    year: Int,
    monthZeroIndexed: Int,
    dateSlots: List<StudioDateSlot>,
    myRequests: List<BookingRequest>,
    selectedDate: String?,
    showSuccessDialog: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (String) -> Unit,
    onSubmitBooking: (dates: List<String>, name: String, phone: String, workType: String, amount: Double, location: String, notes: String) -> Unit,
    onDismissSuccessDialog: () -> Unit,
    onWhatsAppStudio: (phone: String, message: String) -> Unit,
    onCallStudio: (phone: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Booking Form, 1: My Requests

    // Multi-date selection state (Allows selecting multiple dates together)
    var selectedDates by remember {
        mutableStateOf<Set<String>>(if (selectedDate != null) setOf(selectedDate) else emptySet())
    }

    // Keep synced when external selectedDate changes
    androidx.compose.runtime.LaunchedEffect(selectedDate) {
        if (selectedDate != null && !selectedDates.contains(selectedDate)) {
            selectedDates = selectedDates + selectedDate
        }
    }

    // Form inputs state
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedWorkType by remember { mutableStateOf(WorkTypeCatalog.allTypes.first()) }
    var estimatedAmount by remember { mutableStateOf(selectedWorkType.defaultEstimate.toInt().toString()) }
    var venueLocation by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    val bookedDatesSummary = remember(selectedDates) {
        if (selectedDates.isEmpty()) selectedDate ?: ""
        else selectedDates.sorted().joinToString(", ") { DateUtils.formatForDisplay(it) }
    }

    // Success dialog
    if (showSuccessDialog) {
        CustomerSuccessDialog(
            studioName = profile?.studioName ?: "Studio Expo",
            photographerPhone = profile?.whatsapp ?: profile?.phone ?: "",
            customerName = customerName,
            bookedDatesText = bookedDatesSummary,
            onDismiss = onDismissSuccessDialog,
            onChatWhatsApp = {
                val msg = "Hello ${profile?.studioName ?: "Studio Expo"}, I have submitted a booking request for $customerName for shoot dates: $bookedDatesSummary (${selectedWorkType.englishName}). Please review and confirm!"
                onWhatsAppStudio(profile?.whatsapp ?: profile?.phone ?: "", msg)
                onDismissSuccessDialog()
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("customer_booking_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Public Studio Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "Studio Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profile?.studioName ?: "Studio Expo",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "By ${profile?.photographerName ?: "Photographer"} • 📍 ${profile?.city ?: "Gujarat"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = profile?.bio ?: "Professional Wedding, Pre-Wedding & Fashion Shoot",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contact studio buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val msg = "Hello ${profile?.studioName}, I am viewing your calendar and inquiring about shoot booking."
                                onWhatsAppStudio(profile?.whatsapp ?: profile?.phone ?: "", msg)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WhatsApp Studio",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = { onCallStudio(profile?.phone ?: "") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Studio", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Tabs: 0: Book Date, 1: My Requests
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📅 Select Date & Book", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "📋 My Requests (${myRequests.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // Instruction Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = StudioAmber.copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = StudioAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "🟢 Free તેમજ ખાલી (Open) તારીખો બુક કરી શકાય છે. તમે એક સાથે એક કરતાં વધુ તારીખો પણ પસંદ કરી શકો છો! (🔴 લાલ Booked તારીખો બુક નહીં થાય)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Public Calendar
            item {
                StudioCalendarView(
                    year = year,
                    monthZeroIndexed = monthZeroIndexed,
                    dateSlots = dateSlots,
                    bookings = emptyList(), // Customers don't see others' private bookings
                    selectedDate = if (selectedDates.isEmpty()) selectedDate else null,
                    selectedDates = selectedDates,
                    isOwnerMode = false,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onDateClick = { isoDate, slot, isFree ->
                        val isBooked = slot?.status == DateStatus.BOOKED.name
                        if (isBooked) {
                            Toast.makeText(
                                context,
                                "🔴 આ તારીખ પહેલાંથી બુક થયેલી છે (BOOKED)! આ તારીખ બુક નહીં થાય. ફક્ત 🟢 Free અથવા ખાલી (Open) તારીખો જ પસંદ કરી શકાય છે.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            // BOTH FREE (🟢) AND KHALI / OPEN (⚪) DATES CAN BE SELECTED & BOOKED!
                            val currentSet = selectedDates.toMutableSet()
                            if (currentSet.contains(isoDate)) {
                                currentSet.remove(isoDate)
                                selectedDates = currentSet
                                if (currentSet.isEmpty()) {
                                    onSelectDate("")
                                } else {
                                    onSelectDate(currentSet.last())
                                }
                                Toast.makeText(
                                    context,
                                    "તારીખ દૂર કરી: ${DateUtils.formatForDisplay(isoDate)}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                currentSet.add(isoDate)
                                selectedDates = currentSet
                                onSelectDate(isoDate)
                                val msg = if (isFree) {
                                    "🟢 Free તારીખ ઉમેરી: ${DateUtils.formatForDisplay(isoDate)}"
                                } else {
                                    "⚪ ખાલી તારીખ ઉમેરી: ${DateUtils.formatForDisplay(isoDate)}"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            // Selected Dates Summary Bar & Chips (When 1 or more dates are selected)
            if (selectedDates.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(StudioAmber)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "તમે પસંદ કરેલી તારીખો (${selectedDates.size} Selected):",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        selectedDates = emptySet()
                                        onSelectDate("")
                                        Toast.makeText(context, "બધી પસંદગી સાફ કરી", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Clear / સાફ કરો",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Horizontal chips of all selected dates
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(selectedDates.toList().sorted()) { dateIso ->
                                    val slot = dateSlots.find { it.dateStr == dateIso }
                                    val isFree = slot?.status == DateStatus.FREE.name
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isFree) StatusFreeGreen.copy(alpha = 0.18f)
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                1.dp,
                                                if (isFree) StatusFreeGreen else StudioAmber,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${if (isFree) "🟢" else "⚪"} ${DateUtils.formatForDisplay(dateIso)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove date",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable {
                                                    val newSet = selectedDates - dateIso
                                                    selectedDates = newSet
                                                    if (newSet.isEmpty()) onSelectDate("") else onSelectDate(newSet.last())
                                                    Toast.makeText(context, "તારીખ કાઢી: ${DateUtils.formatForDisplay(dateIso)}", Toast.LENGTH_SHORT).show()
                                                }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💡 કેલેન્ડર પર ફરીથી Tap કરીને કોઈપણ તારીખ ઉમેરી અથવા કાઢી શકો છો.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Booking Form (Visible when at least 1 date is selected - both Free and Open/Khali dates)
            if (selectedDates.isNotEmpty()) {
                item {
                    CustomerBookingFormCard(
                        selectedDates = selectedDates,
                        customerName = customerName,
                        customerPhone = customerPhone,
                        selectedWorkType = selectedWorkType,
                        estimatedAmount = estimatedAmount,
                        venueLocation = venueLocation,
                        notesText = notesText,
                        onNameChange = { customerName = it },
                        onPhoneChange = { customerPhone = it },
                        onWorkTypeSelect = { workType ->
                            selectedWorkType = workType
                            val count = selectedDates.size
                            val base = workType.defaultEstimate
                            estimatedAmount = if (count > 1) (base * count).toInt().toString() else base.toInt().toString()
                        },
                        onAmountChange = { estimatedAmount = it },
                        onLocationChange = { venueLocation = it },
                        onNotesChange = { notesText = it },
                        onSubmit = {
                            val count = selectedDates.size
                            val amount = estimatedAmount.toDoubleOrNull() ?: (selectedWorkType.defaultEstimate * count)
                            onSubmitBooking(
                                selectedDates.toList().sorted(),
                                customerName,
                                customerPhone,
                                "${selectedWorkType.englishName} (${selectedWorkType.gujaratiName})",
                                amount,
                                venueLocation,
                                notesText
                            )
                        }
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📅 કેલેન્ડરમાંથી 🟢 Free અથવા ખાલી (Open) તારીખો પસંદ કરો",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "તમે એક સાથે એક કરતાં વધુ તારીખો પણ પસંદ કરી શકો છો. તારીખ પસંદ કરતાં જ બુકિંગ ફોર્મ ખૂલશે.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Tab 1: Customer My Requests List
            item {
                Text(
                    text = "મારી બુકિંગ વિનંતીઓ (Submitted Requests)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (myRequests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "હજી સુધી કોઈ વિનંતી સબમિટ કરેલી નથી.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { selectedTab = 0 },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("તારીખ બુક કરો")
                            }
                        }
                    }
                }
            } else {
                items(myRequests) { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📅 ${DateUtils.formatFullForDisplay(req.dateStr)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                StatusBadge(status = req.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "📸 ${req.workType} • ₹${req.amount.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = StudioAmber
                            )

                            if (req.location.isNotBlank()) {
                                Text(
                                    text = "📍 ${req.location}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Status Explanation for Customer
                            val statusExplanation = when (req.status) {
                                "PENDING" -> "⏳ વિનંતી ઓનર સમક્ષ પેન્ડિંગ છે. ઓનર તરફથી Confirm થયા પછી તારીખ બુક થશે."
                                "CONFIRMED" -> "🎉 અભિનંદન! ઓનર દ્વારા તમારું બુકિંગ Confirm કરવામાં આવ્યું છે!"
                                "REJECTED" -> "❌ આ તારીખ માટે વિનંતી સ્વીકારાઈ નથી. કૃપા કરીને અન્ય તારીખ પસંદ કરો."
                                else -> ""
                            }

                            Text(
                                text = statusExplanation,
                                fontSize = 11.sp,
                                color = if (req.status == "CONFIRMED") StatusFreeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerBookingFormCard(
    selectedDates: Set<String>,
    customerName: String,
    customerPhone: String,
    selectedWorkType: com.example.data.model.WorkType,
    estimatedAmount: String,
    venueLocation: String,
    notesText: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onWorkTypeSelect: (com.example.data.model.WorkType) -> Unit,
    onAmountChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_booking_form_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Selected Date Banner (Single or Multi-Date)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StatusFreeGreen.copy(alpha = 0.15f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = StatusFreeGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (selectedDates.size > 1) {
                            "Selected Dates / ${selectedDates.size} તારીખો પસંદ કરેલ:"
                        } else {
                            "Selected Date / પસંદ કરેલી તારીખ:"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (selectedDates.size > 1) {
                            selectedDates.sorted().joinToString(", ") { DateUtils.formatForDisplay(it) }
                        } else {
                            DateUtils.formatFullForDisplay(selectedDates.firstOrNull() ?: "")
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusFreeGreen
                    )
                }
            }

            // Customer Name
            OutlinedTextField(
                value = customerName,
                onValueChange = onNameChange,
                label = { Text("તમારું પૂરું નામ (Your Full Name) *") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = StudioAmber)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_customer_name"),
                singleLine = true
            )

            // Mobile Number
            OutlinedTextField(
                value = customerPhone,
                onValueChange = onPhoneChange,
                label = { Text("મોબાઇલ / WhatsApp નંબર *") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = StudioAmber)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_customer_phone"),
                singleLine = true
            )

            // Work / Exposure Type Selector
            Text(
                text = "📸 કાર્યક્રમ / શૂટનો પ્રકાર (Work Type):",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(WorkTypeCatalog.allTypes) { type ->
                    val isSelected = selectedWorkType.id == type.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) StudioAmber else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onWorkTypeSelect(type) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${type.iconEmoji} ${type.englishName}",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Amount / Budget
            OutlinedTextField(
                value = estimatedAmount,
                onValueChange = onAmountChange,
                label = {
                    Text(
                        if (selectedDates.size > 1) "કુલ અંદાજિત પેકેજ રકમ (${selectedDates.size} દિવસ) (₹)"
                        else "અંદાજિત બજેટ / પેકેજ રકમ (₹)"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_customer_amount"),
                singleLine = true
            )

            // Venue Location
            OutlinedTextField(
                value = venueLocation,
                onValueChange = onLocationChange,
                label = { Text("સ્થળ / શહેર (Venue Location)") },
                placeholder = { Text("e.g. Shubh Banquet, Ahmedabad") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Notes
            OutlinedTextField(
                value = notesText,
                onValueChange = onNotesChange,
                label = { Text("ખાસ વિગત / નોંધ (Notes / Requirements)") },
                placeholder = { Text("e.g. Drone photography required, 2 days shoot") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Submit Button
            Button(
                onClick = onSubmit,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("customer_book_date_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedDates.size > 1) {
                        "BOOK ${selectedDates.size} DATES / તારીખો બુક કરો 🚀"
                    } else {
                        "BOOK DATE / તારીખ બુક કરો 🚀"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CustomerSuccessDialog(
    studioName: String,
    photographerPhone: String,
    customerName: String,
    bookedDatesText: String,
    onDismiss: () -> Unit,
    onChatWhatsApp: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("customer_success_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(StatusFreeGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = StatusFreeGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "✅ Booking Request Sent!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "બુકિંગ વિનંતી સફળતાપૂર્વક મોકલાઈ ગઈ છે",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (bookedDatesText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusFreeGreen.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📅 Dates: $bookedDatesText",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusFreeGreen,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "તમારી વિનંતી $studioName પાસે પહોંચી ગઈ છે. ઓનર Confirm/Done કરશે ત્યારે તારીખ કન્ફર્મ થશે.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // WhatsApp direct confirmation button
                Button(
                    onClick = onChatWhatsApp,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_whatsapp_owner_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WhatsApp પર ઓનરને મેસેજ કરો 📲",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK / બંધ કરો", fontSize = 13.sp)
                }
            }
        }
    }
}
