package com.example.kmpincidents

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.kmpincidents.navigation.AppNavigation
import com.example.kmpincidents.ui.theme.IncidentsComposeTheme
import com.example.kmpincidents.util.PermissionHandler
import com.example.kmpincidents.util.rememberPermissionLauncher

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            IncidentsComposeTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }

                // Request notification permission on Android 13+ so incident update
                // push notifications can be shown to the user.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notificationPermissionHandler = PermissionHandler(
                        context = this,
                        permission = Manifest.permission.POST_NOTIFICATIONS
                    )
                    val launcher = rememberPermissionLauncher(onPermissionResult = {})

                    LaunchedEffect(Unit) {
                        if (!notificationPermissionHandler.hasPermission()) {
                            notificationPermissionHandler.requestPermission(launcher)
                        }
                    }
                }
            }
        }
    }
}