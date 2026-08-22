package com.example.kmpincidents.util

/**
 * Browsers do not expose an app-level language settings screen.
 * Locale is controlled by the browser/OS, so this is intentionally a no-op.
 */
actual fun openLanguageSettings() {
    // no-op on web
}
