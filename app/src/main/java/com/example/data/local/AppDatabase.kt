package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studioDao(): StudioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE studio_profile ADD COLUMN showRateTraditionalPhoto INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE studio_profile ADD COLUMN showRateTraditionalVideo INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE studio_profile ADD COLUMN showRateCandidPhoto INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE studio_profile ADD COLUMN showRateSemiCandidPhoto INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE studio_profile ADD COLUMN showRateCinematographer INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE studio_profile ADD COLUMN showRateDronePilot INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE studio_profile ADD COLUMN showRateSupportingCrew INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studio_expo_db"
                )
                    .addMigrations(MIGRATION_3_4)
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
                        phone = "+91 XXXXX XX520",
                        whatsapp = "+91 XXXXX XX520",
                        city = "Ahmedabad, Gujarat",
                        ownerPin = "1234",
                        bio = "Luxury Wedding, Pre-Wedding & Cinematic Fashion Photography",
                        instagramHandle = "@studioexpo.official",
                        cameraGearDetails = StudioProfile.DEFAULT_CAMERA_GEAR,
                        customWhatsAppNote = StudioProfile.DEFAULT_WHATSAPP_NOTE,
                        customHeaderGreeting = StudioProfile.DEFAULT_HEADER_GREETING,
                        customFooterNote = StudioProfile.DEFAULT_FOOTER_NOTE,
                        perDayRate = 25000.0
                    )
                )

                // Add starter dates for demo
                val dates = listOf<StudioDateSlot>(
                    StudioDateSlot(dateStr = "2026-10-20", status = DateStatus.FREE, note = "Available for Wedding/Shoot", slotType = SlotType.FULL_DAY),
                    StudioDateSlot(dateStr = "2026-10-22", status = DateStatus.FREE, note = "Available", slotType = SlotType.FULL_DAY),
                    StudioDateSlot(dateStr = "2026-10-24", status = DateStatus.BOOKED, note = "Shoot in Ahmedabad", slotType = SlotType.FULL_DAY, bookedCustomerName = "Amit Patel", bookedCustomerPhone = "+91 XXXXX XX520", workType = "Traditional Photographer + Cinematographer", amount = 35000.0),
                    StudioDateSlot(dateStr = "2026-10-25", status = DateStatus.BOOKED, note = "Candid & Drone Shoot", slotType = SlotType.FULL_DAY, bookedCustomerName = "Amit Patel", bookedCustomerPhone = "+91 XXXXX XX520", workType = "Candid Photographer + Drone Pilot", amount = 30000.0),
                    StudioDateSlot(dateStr = "2026-10-28", status = DateStatus.FREE, note = "Available for Booking", slotType = SlotType.FULL_DAY),
                    StudioDateSlot(dateStr = "2026-11-05", status = DateStatus.FREE, note = "Available for Shoot", slotType = SlotType.FULL_DAY),
                    StudioDateSlot(dateStr = "2026-11-12", status = DateStatus.FREE, note = "Available", slotType = SlotType.FULL_DAY)
                )
                dates.forEach { dao.insertOrUpdateDateSlot(it) }
            }
        }
    }
}
