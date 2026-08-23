package com.example.kmpincidents.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.kmpincidents.data.api.IncidentApi
import com.example.kmpincidents.data.store.TokenPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject

private const val SERVICE_CHANNEL_ID = "incident_updates_service"
private const val SERVICE_CHANNEL_NAME = "Incident update listener"
private const val SERVICE_NOTIFICATION_ID = 1

/**
 * Foreground service that keeps listening for incident update push notifications (via the
 * server SSE endpoint) even while the app is in the background or the activity is closed.
 * Started as a foreground service so the system does not kill the process while it is
 * waiting for updates about incidents reported by the current user.
 */
class IncidentNotificationService : Service() {

    private val incidentApi: IncidentApi by inject()
    private val tokenPreferences: TokenPreferences by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createServiceNotificationChannel()
        startForegroundWithNotification()

        startIncidentNotificationListener(
            scope = serviceScope,
            context = this,
            incidentApi = incidentApi,
            tokenPreferences = tokenPreferences
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Restart the service automatically if the system kills it to reclaim memory.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                SERVICE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps the app listening for incident updates in the background"
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundWithNotification() {
        val notification: Notification = NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Listening for incident updates")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                SERVICE_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(SERVICE_NOTIFICATION_ID, notification)
        }
    }
}
