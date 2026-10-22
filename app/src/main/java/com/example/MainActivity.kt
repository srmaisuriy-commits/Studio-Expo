package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.firebase.FirebaseConfig
import com.example.ui.screens.MainScreen
import com.example.ui.theme.StudioExpoTheme
import com.example.ui.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseConfig.initialize(applicationContext)
        enableEdgeToEdge()
        handleIncomingIntent(intent)
        setContent {
            StudioExpoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val uri: Uri = intent?.data ?: return
        val scheme = uri.scheme
        val host = uri.host
        val path = uri.path ?: ""

        val isCustomerBookingLink = (scheme == "https" && host == "studioexpo.app" && path.startsWith("/book"))
                || (scheme == "studioexpo" && (host == "book" || path.contains("book")))

        if (isCustomerBookingLink) {
            viewModel.switchToCustomerView()
            val dateParam = uri.getQueryParameter("date")
            if (!dateParam.isNullOrBlank()) {
                viewModel.selectDate(dateParam)
            }
        }
    }
}
