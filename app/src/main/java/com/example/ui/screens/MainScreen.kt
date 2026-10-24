package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BookingRequest
import com.example.data.model.StudioProfile
import com.example.ui.components.OwnerBottomNavBar
import com.example.ui.components.StudioTopBar
import com.example.ui.viewmodel.AppRole
import com.example.ui.viewmodel.OwnerScreen
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun MainScreen(
    viewModel: StudioViewModel,
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

    val calendarYear by viewModel.calendarYear.collectAsStateWithLifecycle()
    val calendarMonth by viewModel.calendarMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val ownerSelectedDates by viewModel.ownerSelectedDates.collectAsStateWithLifecycle()

    val showAddDateDialog by viewModel.showAddDateDialog.collectAsStateWithLifecycle()
    val showShareDialog by viewModel.showShareDialog.collectAsStateWithLifecycle()
    val showMessageTemplateDialog by viewModel.showMessageTemplateDialog.collectAsStateWithLifecycle()
    val activeBookingDetail by viewModel.activeBookingDetail.collectAsStateWithLifecycle()
    val bookingSuccessDialog by viewModel.bookingSuccessDialog.collectAsStateWithLifecycle()

    // Back button handling: In owner mode, navigate back to Dashboard before closing
    BackHandler(enabled = currentRole == AppRole.OWNER && isOwnerAuthenticated && currentOwnerScreen != OwnerScreen.DASHBOARD) {
        viewModel.setOwnerScreen(OwnerScreen.DASHBOARD)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (currentRole == AppRole.CUSTOMER || isOwnerAuthenticated) {
                StudioTopBar(
                    currentRole = currentRole,
                    studioName = profile?.studioName ?: "Studio Expo",
                    onRoleSelected = { role ->
                        if (role == AppRole.OWNER) {
                            viewModel.switchToOwnerView()
                        } else {
                            viewModel.switchToCustomerView()
                        }
                    },
                    onShareClick = if (currentRole == AppRole.OWNER) {
                        { viewModel.openShareDialog() }
                    } else null,
                    onMessageTemplateClick = if (currentRole == AppRole.OWNER) {
                        { viewModel.openMessageTemplateDialog() }
                    } else null
                )
            }
        },
        bottomBar = {
            if (currentRole == AppRole.OWNER && isOwnerAuthenticated) {
                OwnerBottomNavBar(
                    currentScreen = currentOwnerScreen,
                    pendingCount = pendingBookings.size,
                    onScreenSelected = { screen ->
                        viewModel.setOwnerScreen(screen)
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentRole) {
                AppRole.CUSTOMER -> {
                    CustomerBookingScreen(
                        profile = profile,
                        year = calendarYear,
                        monthZeroIndexed = calendarMonth,
                        dateSlots = dateSlots,
                        bookings = allBookings,
                        selectedDate = selectedDate,
                        bookingSuccessDialog = bookingSuccessDialog,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onDateSelected = { dateIso -> viewModel.selectDate(dateIso) },
                        onSubmitBooking = { dates, name, phone, workType, amount, location, notes ->
                            viewModel.submitCustomerBooking(dates, name, phone, workType, amount, location, notes)
                        },
                        onDismissSuccess = { viewModel.dismissBookingSuccessDialog() },
                        onWhatsAppDirect = { phone, msg ->
                            viewModel.openWhatsAppChatWithCustomer(context, phone, msg)
                        },
                        onCallDirect = { phone ->
                            viewModel.callCustomer(context, phone)
                        }
                    )
                }

                AppRole.OWNER -> {
                    if (!isOwnerAuthenticated) {
                        OwnerAuthScreen(
                            studioName = profile?.studioName ?: "Studio Expo",
                            photographerName = profile?.photographerName ?: "Owner",
                            pinError = ownerPinError,
                            onPinEntered = { pin -> viewModel.verifyOwnerPin(pin) },
                            onEmailLogin = { email, pass -> viewModel.loginOwnerWithEmail(email, pass) },
                            onEmailSignUp = { email, pass -> viewModel.registerOwnerWithEmail(email, pass) },
                            onGoogleLogin = { idToken -> viewModel.loginOwnerWithGoogle(idToken) },
                            onDirectLogin = { user -> viewModel.authenticateOwner(user) },
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
                                    onAddFreeDateClick = { viewModel.openAddDateDialog() },
                                    onShareWhatsAppClick = { viewModel.openShareDialog() },
                                    onSetMessageTemplateClick = { viewModel.openMessageTemplateDialog() },
                                    onConfirmBooking = { booking -> viewModel.confirmBooking(booking) },
                                    onOpenBookingDetail = { booking -> viewModel.openBookingDetail(booking) },
                                    onSaveEditedBooking = { oldDates, newDates, cName, cPhone, workSummary, crewMap, totalAmt ->
                                        viewModel.updateBookingDetails(oldDates, newDates, cName, cPhone, workSummary, crewMap, totalAmt)
                                    },
                                    onCancelBookingDates = { dates ->
                                        viewModel.cancelBookingForDates(dates)
                                    },
                                    onCallCustomer = { phone ->
                                        viewModel.callCustomer(context, phone)
                                    },
                                    onWhatsAppCustomer = { phone, msg ->
                                        viewModel.openWhatsAppChatWithCustomer(context, phone, msg)
                                    }
                                )
                            }

                            OwnerScreen.CALENDAR -> {
                                OwnerCalendarScreen(
                                    year = calendarYear,
                                    monthZeroIndexed = calendarMonth,
                                    dateSlots = dateSlots,
                                    bookings = allBookings,
                                    selectedDates = ownerSelectedDates,
                                    profile = profile,
                                    onPreviousMonth = { viewModel.previousMonth() },
                                    onNextMonth = { viewModel.nextMonth() },
                                    onDateToggle = { dateIso -> viewModel.toggleOwnerSelectedDate(dateIso) },
                                    onClearAllSelectedDates = { viewModel.clearOwnerSelectedDates() },
                                    onSetFreeDates = { dates -> viewModel.markDatesAsFree(dates) },
                                    onSetBookedDates = { dates -> viewModel.markDatesAsBooked(dates) },
                                    onSetRequestDates = { dates -> viewModel.markDatesAsPending(dates) },
                                    onClearDates = { dates -> viewModel.clearDatesStatus(dates) },
                                    onDirectBookingDone = { dates, name, phone, workType, amount ->
                                        viewModel.createDirectOwnerBooking(dates, name, phone, workType, amount)
                                    },
                                    onCallCustomer = { phone -> viewModel.callCustomer(context, phone) },
                                    onWhatsAppCustomer = { phone, msg -> viewModel.openWhatsAppChatWithCustomer(context, phone, msg) },
                                    onShareWhatsAppClick = { viewModel.openShareDialog() },
                                    onMessageTemplateClick = { viewModel.openMessageTemplateDialog() },
                                    onCloseDatePanel = { viewModel.clearOwnerSelectedDates() }
                                )
                            }

                            OwnerScreen.REQUESTS -> {
                                OwnerBookingRequestsScreen(
                                    bookings = allBookings,
                                    onConfirmBooking = { booking -> viewModel.confirmBooking(booking) },
                                    onRejectBooking = { booking -> viewModel.rejectBooking(booking) },
                                    onOpenBookingDetail = { booking -> viewModel.openBookingDetail(booking) }
                                )
                            }

                            OwnerScreen.PROFILE -> {
                                OwnerProfileScreen(
                                    profile = profile,
                                    onSaveProfile = { updated -> viewModel.saveProfile(updated) },
                                    onLogout = { viewModel.logoutOwner() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showShareDialog) {
        val freeSlots = dateSlots.filter { it.status == com.example.data.model.DateStatus.FREE }
        ShareWhatsAppDialog(
            profile = profile,
            freeSlots = freeSlots,
            onDismiss = { viewModel.closeShareDialog() },
            onShareClick = { msg ->
                viewModel.shareFormattedMessage(context, msg)
                viewModel.closeShareDialog()
            },
            onSaveCameraDetails = { gear, note ->
                viewModel.updateEquipmentAndNote(gear, note)
            },
            generateMessage = { gear, note ->
                viewModel.buildBroadcastMessage(gear, note, freeSlots)
            }
        )
    }

    if (showMessageTemplateDialog) {
        MessageTemplateDialog(
            profile = profile,
            onDismiss = { viewModel.closeMessageTemplateDialog() },
            onSave = { gear, note, greeting, footer ->
                viewModel.saveCustomTemplates(gear, note, greeting, footer)
                viewModel.closeMessageTemplateDialog()
            }
        )
    }

    if (showAddDateDialog) {
        AddFreeDateDialog(
            initialDateIso = selectedDate,
            onDismiss = { viewModel.closeAddDateDialog() },
            onConfirm = { dateIso, note, slotType ->
                viewModel.addFreeDate(dateIso, note, slotType)
                viewModel.closeAddDateDialog()
            }
        )
    }

    if (activeBookingDetail != null) {
        val booking = activeBookingDetail!!
        BookingDetailDialog(
            booking = booking,
            onDismiss = { viewModel.closeBookingDetail() },
            onConfirm = { b -> viewModel.confirmBooking(b) },
            onReject = { b -> viewModel.rejectBooking(b) }
        )
    }
}
