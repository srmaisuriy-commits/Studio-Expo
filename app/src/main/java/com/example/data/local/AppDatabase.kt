package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BookingRequest
import com.example.data.model.BookingStatus
import com.example.data.model.DateStatus
import com.example.data.model.SlotType
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudioProfile::class,
        StudioDateSlot::class,
        BookingRequest::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studioDao(): StudioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studio_expo_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                INSTANCE = instance

                // Guarantee that initial profile and sample dates are seeded immediately on first run
                scope.launch(Dispatchers.IO) {
                    val dao = instance.studioDao()
                    if (dao.getProfileSync() == null) {
                        populateInitialData(dao)
                    }
                }

                instance
            }
        }

        private suspend fun populateInitialData(dao: StudioDao) {
            // 1. Initial Studio Profile
            dao.insertOrUpdateProfile(
                StudioProfile(
                    id = 1,
                    studioName = "Studio Expo",
                    photographerName = "Rajesh Maisuriya",
                    phone = "9876543210",
                    whatsapp = "9876543210",
                    city = "Ahmedabad, Gujarat",
                    ownerPin = "1234",
                    bio = "Luxury Wedding & Cinematic Photography Studio. Capturing timeless moments across Gujarat & India.",
                    cameraGearDetails = "Sony FX3 / A7 IV • DJI Drone • 4K Cinematic Video • Gimbal & Pro Lights",
                    customWhatsAppNote = "લેટેસ્ટ સિનેમેટિક કેમેરા અને ડ્રોન કવરેજ ઉપલબ્ધ છે. તમારા શુભ પ્રસંગના શૂટ માટે સંપર્ક કરો!"
                )
            )

            // 2. Pre-seed sample dates in the upcoming days/weeks:
            // Some FREE dates (🟢) and some BOOKED dates (🔴)
            val slots = listOf(
                StudioDateSlot(
                    dateStr = DateUtils.addDaysToToday(2),
                    status = DateStatus.FREE.name,
                    slotType = SlotType.FULL_DAY.name,
                    basePrice = 35000.0,
                    note = "Available for Pre-Wedding or Event"
                ),
                StudioDateSlot(
                    dateStr = DateUtils.addDaysToToday(4),
                    status = DateStatus.FREE.name,
                    slotType = SlotType.FULL_DAY.name,
                    basePrice = 45000.0,
                    note = "Wedding / Lagan Slot Open"
                ),
                StudioDateSlot(
                    dateStr = DateUtils.addDaysToToday(6),
                    status = DateStatus.BOOKED.name,
                    slotType = SlotType.FULL_DAY.name,
                    basePrice = 55000.0,
                    note = "Confirmed Lagan Shoot"
                ),
                StudioDateSlot(
                    dateStr = DateUtils.addDaysToToday(9),
                    status = DateStatus.FREE.name,
                    slotType = SlotType.MORNING.name,
                    basePrice = 20000.0,
                    note = "Morning Baby Shower / Sangeet"
                ),
                StudioDateSlot(
                    dateStr = DateUtils.addDaysToToday(11),
                    status = DateStatus.FREE.name,
                    slotType = SlotType.FULL_DAY.name,
                    basePrice = 50000.0,
                    note = "Available for Destination Shoot"
                ),
                StudioDateSlot(
                    dateStr = DateUtils.addDaysToToday(14),
                    status = DateStatus.BOOKED.name,
                    slotType = SlotType.FULL_DAY.name,
                    basePrice = 60000.0,
                    note = "Grand Reception Night"
                ),
                StudioDateSlot(
                    dateStr = DateUtils.addDaysToToday(18),
                    status = DateStatus.FREE.name,
                    slotType = SlotType.FULL_DAY.name,
                    basePrice = 40000.0,
                    note = "Weekend Available"
                )
            )
            dao.insertDateSlots(slots)

            // 3. Pre-seed a sample pending booking request and confirmed booking
            val booking1 = BookingRequest(
                dateStr = DateUtils.addDaysToToday(4),
                customerName = "Bhavin Shah",
                customerPhone = "9825012345",
                workType = "Wedding / Lagan (લગ્ન)",
                amount = 45000.0,
                location = "Shubh Banquet, SG Highway, Ahmedabad",
                notes = "Morning Muhurat & Evening Reception. Drone required.",
                status = BookingStatus.PENDING.name,
                isSubmittedByCurrentCustomer = false
            )
            dao.insertBooking(booking1)

            val booking2 = BookingRequest(
                dateStr = DateUtils.addDaysToToday(6),
                customerName = "Pooja Patel",
                customerPhone = "9898056789",
                workType = "Pre-Wedding Shoot (પ્રી-વેડિંગ)",
                amount = 55000.0,
                location = "Polo Forest & Heritage City",
                notes = "2 Days Shoot with Traditional Attire",
                status = BookingStatus.CONFIRMED.name,
                isSubmittedByCurrentCustomer = false
            )
            val id2 = dao.insertBooking(booking2)
            dao.updateDateSlotStatus(DateUtils.addDaysToToday(6), DateStatus.BOOKED.name, id2)
        }
    }
}
