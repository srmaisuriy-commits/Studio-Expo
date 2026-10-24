package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseConfig {

    private var firestoreInstance: FirebaseFirestore? = null

    fun initialize(context: Context): FirebaseApp? {
        val app = try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                FirebaseApp.getInstance()
            }
        } catch (e: Throwable) {
            Log.e("FirebaseConfig", "FirebaseApp init: ${e.message}")
            null
        }

        try {
            val dbId = context.getString(R.string.firestore_database_id)
            if (app != null && dbId.isNotBlank()) {
                firestoreInstance = FirebaseFirestore.getInstance(app, dbId)
            }
        } catch (e: Throwable) {
            Log.e("FirebaseConfig", "Firestore init: ${e.message}")
        }

        return app
    }

    val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.e("FirebaseConfig", "FirebaseAuth getInstance: ${e.message}")
            null
        }

    fun getFirestore(context: Context): FirebaseFirestore? {
        if (firestoreInstance == null) {
            try {
                val dbId = context.getString(R.string.firestore_database_id)
                val app = FirebaseApp.getInstance()
                firestoreInstance = FirebaseFirestore.getInstance(app, dbId)
            } catch (e: Throwable) {
                Log.e("FirebaseConfig", "Firestore getFirestore: ${e.message}")
            }
        }
        return firestoreInstance
    }
}
