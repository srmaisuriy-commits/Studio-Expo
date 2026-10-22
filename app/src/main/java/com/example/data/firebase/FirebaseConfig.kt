package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseConfig {

    fun initialize(context: Context): FirebaseApp? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                FirebaseApp.getInstance()
            }
        } catch (e: Throwable) {
            Log.e("FirebaseConfig", "FirebaseApp init: ${e.message}")
            null
        }
    }

    val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.e("FirebaseConfig", "FirebaseAuth getInstance: ${e.message}")
            null
        }

    val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.e("FirebaseConfig", "FirebaseFirestore getInstance: ${e.message}")
            null
        }
}
