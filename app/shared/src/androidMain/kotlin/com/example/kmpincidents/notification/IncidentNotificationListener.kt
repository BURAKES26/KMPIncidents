package com.example.kmpincidents.notification

import android.content.Context
import com.example.kmpincidents.data.api.IncidentApi
import com.example.kmpincidents.data.store.TokenPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val RECONNECT_DELAY_MS = 5_000L

/**
 * Continuously listens for incident update push notifications (Server-Sent Events) for the
 * currently authenticated user and shows a system notification for each one.
 *
 * The listener keeps retrying: it only actively streams events while the user is logged in
 * (a token is present), and automatically reconnects after a connection drop or logout/login.
 */
fun startIncidentNotificationListener(
    scope: CoroutineScope,
    context: Context,
    incidentApi: IncidentApi,
    tokenPreferences: TokenPreferences
) {
    IncidentNotifier.createNotificationChannel(context)

    scope.launch {
        while (true) {
            val hasToken = tokenPreferences.getToken() != null
            if (hasToken) {
                incidentApi.observeIncidentNotifications()
                    .catch { /* connection dropped, will retry below */ }
                    .collect { incident ->
                        IncidentNotifier.notifyIncidentUpdated(context, incident)
                    }
            }
            delay(RECONNECT_DELAY_MS.milliseconds)
        }
    }
}
