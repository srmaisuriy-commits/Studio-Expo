package com.example.data.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Firebase Configuration for Studio Expo.
 *
 * In Android (Kotlin), Firebase automatically reads configuration
 * from `app/google-services.json` (Project: studio-expo, Package: com.StudioExpo.app).
 *
 * Equivalent to:
 * const app = initializeApp(firebaseConfig);
 * export const auth = getAuth(app);
 */
object FirebaseConfig {

    fun initialize(context: Context): FirebaseApp? {
        return if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        } else {
            FirebaseApp.getInstance()
        }
    }

    /**
     * Firebase Auth instance (Equivalent to `getAuth(app)`)
     */
    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    /**
     * Firebase Firestore instance (Equivalent to `getFirestore(app)`)
     */
    val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()
}
