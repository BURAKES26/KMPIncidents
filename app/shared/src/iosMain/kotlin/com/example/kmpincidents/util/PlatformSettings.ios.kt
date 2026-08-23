package com.example.kmpincidents.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

/**
 * iOS has no separate "locale settings" deep link, so open the app's Settings page instead.
 */
actual fun openLanguageSettings() {
    val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
    val application = UIApplication.sharedApplication
    if (application.canOpenURL(url)) {
        @Suppress("DEPRECATION")
        application.openURL(url)
    }
}
