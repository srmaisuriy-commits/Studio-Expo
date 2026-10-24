package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.DateUtils
import com.example.data.model.BookedCustomerGroup
import com.example.data.model.StudioProfile
import com.example.data.model.WorkTypeCatalog
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextLightPrimary
import com.example.ui.theme.TextLightSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditBookingDialog(
    group: BookedCustomerGroup,
    profile: StudioProfile?,
    onDismiss: () -> Unit,
    onSave: (
        originalDates: List<String>,
        newDates: List<String>,
        customerName: String,
        customerPhone: String,
        workTypeSummary: String,
        dateCrewMap: Map<String, String>,
        totalAmount: Double
    ) -> Unit,
    onCancelBooking: (List<String>) -> Unit
) {
    val catalogTypes = remember(profile) { WorkTypeCatalog.getTypesForProfile(profile) }

    var customerName by remember { mutableStateOf(group.customerName) }
    var customerPhone by remember { mutableStateOf(group.customerPhone) }
    var activeDates by remember { mutableStateOf(group.dates) }

    // Date-crew map: initialize from existing
    var dateSpecificCrewMap by remember {
        val initialMap = group.dates.associateWith { d ->
            group.dateCrewMap[d] ?: group.workTypeSummary
        }.toMutableMap()
        mutableStateOf(initialMap)
    }

    // Whether multi-dates have different crew per date
    var differentCrewPerDate by remember {
        mutableStateOf(activeDates.size > 1 && (group.dateCrewMap.size > 1 || group.workTypeSummary.contains("|")))
    }

    var selectedGlobalCrew by remember {
        mutableStateOf(group.workTypeSummary.takeIf { it.isNotBlank() } ?: catalogTypes.first().englishName)
    }

    var customAmount by remember { mutableDoubleStateOf(group.totalAmount) }
    var amountText by remember { mutableStateOf(group.totalAmount.toLong().toString()) }

    fun recalculateAmount(dates: List<String>, isDateWise: Boolean, crewMap: Map<String, String>, globalCrew: String) {
        if (dates.isEmpty()) {
            customAmount = 0.0
            amountText = "0"
            return
        }
        val calculatedSum = if (dates.size > 1 && isDateWise) {
            dates.sumOf { d ->
                val role = crewMap[d] ?: globalCrew
                catalogTypes.find { it.englishName == role }?.defaultEstimate ?: 10000.0
            }
        } else {
            val role = catalogTypes.find { it.englishName == globalCrew } ?: catalogTypes.first()
            dates.size * role.defaultEstimate
        }
        customAmount = calculatedSum
        amountText = calculatedSum.toLong().toString()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("edit_booking_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "✏️ Edit Booking & Crew",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioAmber
                        )
                        Text(
                            text = "बुकिंग और क्रू लिस्ट अपडेट करें",
                            fontSize = 11.sp,
                            color = TextLightSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextLightSecondary)
                    }
                }

                // Customer Name
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name / ग्राहक का नाम") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = StudioAmber) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_customer_name"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioAmber,
                        cursorColor = StudioAmber
                    )
                )

                // Customer Phone
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Customer Mobile / मोबाइल नंबर") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = StudioAmber) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("edit_customer_phone"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioAmber,
                        cursorColor = StudioAmber
                    )
                )

                // Booked Dates Section
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 Booked Dates (${activeDates.size}) / बुक की गई तारीखें",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLightPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    if (activeDates.isEmpty()) {
                        Text(
                            text = "No dates remaining in this booking.",
                            fontSize = 12.sp,
                            color = StatusBookedRed
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeDates.forEach { dateIso ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = StudioSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioAmber.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = DateUtils.formatForDisplay(dateIso),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextLightPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove date",
                                            tint = StatusBookedRed,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable {
                                                    val updatedDates = activeDates.filterNot { it == dateIso }
                                                    activeDates = updatedDates
                                                    recalculateAmount(updatedDates, differentCrewPerDate, dateSpecificCrewMap, selectedGlobalCrew)
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 7 CREW SELECTION SECTION (as explicitly requested: "yes but edit opsion ma crow list api dejo")
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👥 Select Crew Roles / क्रू लिस्ट",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioAmber
                        )
                        if (activeDates.size > 1) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (differentCrewPerDate) StudioAmber else StudioSurface,
                                modifier = Modifier.clickable {
                                    differentCrewPerDate = !differentCrewPerDate
                                    recalculateAmount(activeDates, differentCrewPerDate, dateSpecificCrewMap, selectedGlobalCrew)
                                }
                            ) {
                                Text(
                                    text = if (differentCrewPerDate) "Date-wise ON" else "Same Crew",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (differentCrewPerDate) Color.Black else TextLightPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (activeDates.size > 1 && differentCrewPerDate) {
                        // Date-wise crew assignment for each date
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activeDates.forEach { dateIso ->
                                val selectedForThisDate = dateSpecificCrewMap[dateIso] ?: selectedGlobalCrew
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = StudioSurface),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "📅 ${DateUtils.formatForDisplay(dateIso)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = StudioAmber
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            catalogTypes.forEach { workType ->
                                                val isSelected = selectedForThisDate.contains(workType.englishName, ignoreCase = true)
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) StudioAmber else StudioCard,
                                                    modifier = Modifier.clickable {
                                                        val updatedMap = dateSpecificCrewMap.toMutableMap()
                                                        updatedMap[dateIso] = workType.englishName
                                                        dateSpecificCrewMap = updatedMap
                                                        recalculateAmount(activeDates, differentCrewPerDate, updatedMap, selectedGlobalCrew)
                                                    }
                                                ) {
                                                    Text(
                                                        text = "${workType.iconEmoji} ${workType.englishName} • ₹${workType.defaultEstimate.toInt()}",
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color.Black else TextLightPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Single / Same Crew Selection Chips across all 7 types
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            catalogTypes.forEach { workType ->
                                val isSelected = selectedGlobalCrew.contains(workType.englishName, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) StudioAmber else StudioSurface,
                                    modifier = Modifier.clickable {
                                        selectedGlobalCrew = workType.englishName
                                        recalculateAmount(activeDates, differentCrewPerDate, dateSpecificCrewMap, workType.englishName)
                                    }
                                ) {
                                    Text(
                                        text = "${workType.iconEmoji} ${workType.englishName} • ₹${workType.defaultEstimate.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else TextLightPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Total Package Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        customAmount = it.toDoubleOrNull() ?: 0.0
                    },
                    label = { Text("Total Package Amount (₹) / कुल पैकेज राशि") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("edit_package_amount"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioAmber,
                        cursorColor = StudioAmber
                    )
                )

                // Save Changes Button
                Button(
                    onClick = {
                        val finalWorkSummary = if (activeDates.size > 1 && differentCrewPerDate) {
                            activeDates.joinToString(" | ") { d ->
                                "$d: ${dateSpecificCrewMap[d] ?: selectedGlobalCrew}"
                            }
                        } else {
                            selectedGlobalCrew
                        }

                        onSave(
                            group.dates,
                            activeDates,
                            customerName,
                            customerPhone,
                            finalWorkSummary,
                            dateSpecificCrewMap,
                            customAmount
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_edited_booking"),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save Changes / बदलाव सहेजें",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Unbook / Cancel & Dismiss Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onCancelBooking(group.dates)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusBookedRed.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = StatusBookedRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unbook Dates", color = StatusBookedRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel / बंद करें", fontSize = 11.sp, color = TextLightSecondary)
                    }
                }
            }
        }
    }
}
