package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DateStatus
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StudioAmber
import com.example.ui.viewmodel.AppRole
import com.example.ui.viewmodel.OwnerScreen
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun MainScreen(
    viewModel: StudioViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val isOwnerAuthenticated by viewModel.isOwnerAuthenticated.collectAsStateWithLifecycle()
    val ownerPinError by viewModel.pinError.collectAsStateWithLifecycle()
    val currentOwnerScreen by viewModel.currentOwnerScreen.collectAsStateWithLifecycle()

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val dateSlots by viewModel.dateSlots.collectAsStateWithLifecycle()
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val pendingBookings by viewModel.pendingBookings.collectAsStateWithLifecycle()
    val customerMyRequests by viewModel.customerMyRequests.collectAsStateWithLifecycle()

    val calendarYear by viewModel.calendarYear.collectAsStateWithLifecycle()
    val calendarMonth by viewModel.calendarMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    val showAddDateDialog by viewModel.showAddDateDialog.collectAsStateWithLifecycle()
    val showShareDialog by viewModel.showShareDialog.collectAsStateWithLifecycle()
    val showMessageTemplateDialog by viewModel.showMessageTemplateDialog.collectAsStateWithLifecycle()
    val activeBookingDetail by viewModel.activeBookingDetail.collectAsStateWithLifecycle()
    val bookingSuccessDialog by viewModel.bookingSuccessDialog.collectAsStateWithLifecycle()

    // Handle Back Press
    BackHandler(enabled = currentRole == AppRole.OWNER && currentOwnerScreen != OwnerScreen.DASHBOARD) {
        viewModel.setOwnerScreen(OwnerScreen.DASHBOARD)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            StudioTopBar(
                currentRole = currentRole,
                studioName = profile?.studioName ?: "Studio Expo",
                onRoleSelected = { role ->
                    if (role == AppRole.CUSTOMER) {
                        viewModel.switchToCustomerView()
                    } else {
                        viewModel.switchToOwnerView()
                    }
                },
                onShareClick = {
                    viewModel.openShareDialog()
                },
                onMessageTemplateClick = {
                    viewModel.openMessageTemplateDialog()
                }
            )
        },
        bottomBar = {
            if (currentRole == AppRole.OWNER && isOwnerAuthenticated) {
                OwnerBottomNavBar(
                    currentScreen = currentOwnerScreen,
                    pendingCount = pendingBookings.size,
                    onScreenSelected = { viewModel.setOwnerScreen(it) },
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                AppRole.OWNER -> {
                    if (!isOwnerAuthenticated) {
                        OwnerAuthScreen(
                            studioName = profile?.studioName ?: "Studio Expo",
                            photographerName = profile?.photographerName ?: "Rajesh Maisuriya",
                            pinError = ownerPinError,
                            onPinEntered = { pin -> viewModel.verifyOwnerPin(pin) },
                            onSwitchToCustomer = { viewModel.switchToCustomerView() }
                        )
                    } else {
                        when (currentOwnerScreen) {
                            OwnerScreen.DASHBOARD -> {
                                OwnerDashboardScreen(
                                    profile = profile,
                                    dateSlots = dateSlots,
                                    pendingRequests = pendingBookings,
                                    allBookings = allBookings,
                                    onNavigateCalendar = { viewModel.setOwnerScreen(OwnerScreen.CALENDAR) },
                                    onNavigateRequests = { viewModel.setOwnerScreen(OwnerScreen.REQUESTS) },
                                    onAddFreeDateClick = { viewModel.setOwnerScreen(OwnerScreen.CALENDAR) },
                                    onShareWhatsAppClick = { viewModel.openShareDialog() },
                                    onSetMessageTemplateClick = { viewModel.openMessageTemplateDialog() },
                                    onConfirmBooking = { viewModel.confirmBooking(it) },
                                    onOpenBookingDetail = { viewModel.openBookingDetail(it) }
                                )
                            }
                            OwnerScreen.CALENDAR -> {
                                OwnerCalendarScreen(
                                    year = calendarYear,
                                    monthZeroIndexed = calendarMonth,
                                    dateSlots = dateSlots,
                                    bookings = allBookings,
                                    selectedDate = selectedDate,
                                    onPreviousMonth = { viewModel.previousMonth() },
                                    onNextMonth = { viewModel.nextMonth() },
                                    onDateSelect = { viewModel.selectDate(it) },
                                    onSetFreeDate = { viewModel.setDirectDateStatus(it, DateStatus.FREE) },
                                    onSetBookedDate = { viewModel.setDirectDateStatus(it, DateStatus.BOOKED) },
                                    onSetRequestDate = { viewModel.setDirectDateStatus(it, DateStatus.REQUEST) },
                                    onClearDate = { viewModel.clearDateStatus(it) },
                                    onDirectBookingDone = { date, name, phone, workType, amount ->
                                        viewModel.createDirectBooking(date, name, phone, workType, amount)
                                    },
                                    onCallCustomer = { phone -> viewModel.callCustomer(context, phone) },
                                    onWhatsAppCustomer = { phone, msg ->
                                        viewModel.openWhatsAppChatWithCustomer(context, phone, msg)
                                    },
                                    onShareWhatsAppClick = { viewModel.openShareDialog() },
                                    onSetMessageTemplateClick = { viewModel.openMessageTemplateDialog() },
                                    onCloseDatePanel = { viewModel.clearSelectedDate() }
                                )
                            }
                            OwnerScreen.REQUESTS -> {
                                OwnerBookingRequestsScreen(
                                    bookings = allBookings,
                                    onConfirmBooking = { viewModel.confirmBooking(it) },
                                    onRejectBooking = { viewModel.rejectBooking(it) },
                                    onCallCustomer = { phone -> viewModel.callCustomer(context, phone) },
                                    onWhatsAppCustomer = { phone, msg ->
                                        viewModel.openWhatsAppChatWithCustomer(context, phone, msg)
                                    },
                                    onOpenBookingDetail = { viewModel.openBookingDetail(it) }
                                )
                            }
                            OwnerScreen.PROFILE -> {
                                OwnerProfileScreen(
                                    profile = profile,
                                    onSaveProfile = { viewModel.updateProfile(it) },
                                    onLogout = { viewModel.logoutOwner() }
                                )
                            }
                        }
                    }
                }

                AppRole.CUSTOMER -> {
                    // Customer Public Booking Flow (NO LOGIN REQUIRED)
                    CustomerBookingScreen(
                        profile = profile,
                        year = calendarYear,
                        monthZeroIndexed = calendarMonth,
                        dateSlots = dateSlots,
                        myRequests = customerMyRequests,
                        selectedDate = selectedDate,
                        showSuccessDialog = bookingSuccessDialog,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onSelectDate = { viewModel.selectDate(it) },
                        onSubmitBooking = { dates, name, phone, workType, amount, location, notes ->
                            viewModel.submitCustomerBooking(dates, name, phone, workType, amount, location, notes)
                        },
                        onDismissSuccessDialog = { viewModel.dismissBookingSuccess() },
                        onWhatsAppStudio = { phone, msg ->
                            viewModel.openWhatsAppChatWithCustomer(context, phone, msg)
                        },
                        onCallStudio = { phone -> viewModel.callCustomer(context, phone) }
                    )
                }
            }
        }
    }

    // Direct Share WhatsApp Dialog with Auto-Updated Live Calendar Free Dates and Camera Gear Config
    if (showShareDialog) {
        ShareWhatsAppDialog(
            profile = profile,
            freeSlots = dateSlots,
            onDismiss = { viewModel.closeShareDialog() },
            onShareClick = { customMsg ->
                viewModel.shareFreeDatesOnWhatsApp(context, customMsg)
            },
            onSaveCameraDetails = { gear, note ->
                viewModel.updateCameraAndWhatsAppNote(gear, note)
            },
            generateMessage = { gear, note ->
                viewModel.getDynamicWhatsAppMessage(gear, note)
            }
        )
    }

    // Full Message Template Builder & Camera Gear Config Dialog
    if (showMessageTemplateDialog) {
        MessageTemplateDialog(
            profile = profile,
            onDismiss = { viewModel.closeMessageTemplateDialog() },
            onSaveTemplate = { gear, note, greeting, footer ->
                viewModel.updateFullMessageTemplate(gear, note, greeting, footer)
            },
            onShareClick = { customMsg ->
                viewModel.shareFreeDatesOnWhatsApp(context, customMsg)
            },
            generateMessage = { gear, note, greeting, footer ->
                viewModel.getDynamicWhatsAppMessage(
                    customGear = gear,
                    customNote = note,
                    customGreeting = greeting,
                    customFooter = footer
                )
            }
        )
    }

    if (activeBookingDetail != null) {
        BookingDetailDialog(
            booking = activeBookingDetail!!,
            onDismiss = { viewModel.closeBookingDetail() },
            onConfirm = { viewModel.confirmBooking(activeBookingDetail!!) },
            onReject = { viewModel.rejectBooking(activeBookingDetail!!) },
            onCallCustomer = { viewModel.callCustomer(context, activeBookingDetail!!.customerPhone) },
            onWhatsAppCustomer = {
                val msg = "Hello ${activeBookingDetail!!.customerName}, regarding your booking on ${activeBookingDetail!!.dateStr} for ${activeBookingDetail!!.workType}..."
                viewModel.openWhatsAppChatWithCustomer(context, activeBookingDetail!!.customerPhone, msg)
            }
        )
    }
}

@Composable
private fun OwnerBottomNavBar(
    currentScreen: OwnerScreen,
    pendingCount: Int,
    onScreenSelected: (OwnerScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("owner_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == OwnerScreen.DASHBOARD,
            onClick = { onScreenSelected(OwnerScreen.DASHBOARD) },
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = StudioAmber
            )
        )

        NavigationBarItem(
            selected = currentScreen == OwnerScreen.CALENDAR,
            onClick = { onScreenSelected(OwnerScreen.CALENDAR) },
            icon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar") },
            label = { Text("Calendar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = StudioAmber
            )
        )

        NavigationBarItem(
            selected = currentScreen == OwnerScreen.REQUESTS,
            onClick = { onScreenSelected(OwnerScreen.REQUESTS) },
            icon = {
                if (pendingCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = StatusPendingAmber) {
                                Text(
                                    text = pendingCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Requests")
                    }
                } else {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "Requests")
                }
            },
            label = { Text("Requests", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = StudioAmber
            )
        )

        NavigationBarItem(
            selected = currentScreen == OwnerScreen.PROFILE,
            onClick = { onScreenSelected(OwnerScreen.PROFILE) },
            icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = StudioAmber
            )
        )
    }
}
