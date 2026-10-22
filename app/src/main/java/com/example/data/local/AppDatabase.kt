package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BookingRequest
import com.example.data.model.DateStatus
import com.example.data.model.SlotType
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [StudioProfile::class, StudioDateSlot::class, BookingRequest::class],
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
                    "studio_expo_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.studioDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: StudioDao) {
                // Initial Studio Profile
                dao.insertProfile(
                    StudioProfile(
                        id = 1,
                        studioName = "Studio Expo",
                        photographerName = "Rajesh Maisuriya",
                        phone = "9876543210",
                        whatsapp = "9876543210",
                        city = "Ahmedabad, Gujarat",
                        ownerPin = "1234",
                        bio = "Luxury Wedding, Pre-Wedding & Cinematic Fashion Photography",
                        instagramHandle = "@studioexpo.official",
                        cameraGearDetails = "Sony FX3 / A7 IV • DJI Drone • 4K Cinematic Video • Gimbal & Pro Lights",
                        customWhatsAppNote = "Latest 4K Cinematic Camera & Drone Coverage Available. Special packages available! / नवीनतम 4K सिनेमैटिक कैमरा और ड्रोन कवरेज उपलब्ध है।",
                        customHeaderGreeting = "Hello! Here are our available (FREE) dates for upcoming wedding & events / नमस्ते! आगामी शादी और इवेंट्स के लिए हमारी उपलब्ध (FREE) तारीखें:",
                        customFooterNote = "Reserve your special date today! Contact us on WhatsApp for bookings & details / अपनी तारीख आज ही बुक करें!",
                        perDayRate = 25000.0
                    )
                )

                // Add starter dates for demo
                val dates = listOf<StudioDateSlot>(
                    StudioDateSlot(dateStr = "2026-10-20", status = DateStatus.FREE, note = "Available for Wedding/Shoot", slotType = SlotType.FULL_DAY),
                    StudioDateSlot(dateStr = "2026-10-22", status = DateStatus.FREE, note = "Available", slotType = SlotType.FULL_DAY),
                    StudioDateSlot(dateStr = "2026-10-24", status = DateStatus.BOOKED, note = "Wedding in Ahmedabad", slotType = SlotType.FULL_DAY, bookedCustomerName = "Amit Patel", bookedCustomerPhone = "9898012345", workType = "Wedding Ceremony", amount = 50000.0),
                    StudioDateSlot(dateStr = "2026-10-25", status = DateStatus.BOOKED, note = "Reception Shoot", slotType = SlotType.FULL_DAY, bookedCustomerName = "Amit Patel", bookedCustomerPhone = "9898012345", workType = "Reception Night", amount = 25000.0),
                    StudioDateSlot(dateStr = "2026-10-28", status = DateStatus.FREE, note = "Available for Booking", slotType = SlotType.FULL_DAY),
                    StudioDateSlot(dateStr = "2026-11-05", status = DateStatus.FREE, note = "Available for Pre-Wedding", slotType = SlotType.FULL_DAY),
                    StudioDateSlot(dateStr = "2026-11-12", status = DateStatus.FREE, note = "Available", slotType = SlotType.FULL_DAY)
                )
                dates.forEach { dao.insertOrUpdateDateSlot(it) }
            }
        }
    }
}
