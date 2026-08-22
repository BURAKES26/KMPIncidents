package com.example.kmpincidents.util

/**
 * Desktop has no in-app language settings screen; locale is controlled by the OS.
 */
actual fun openLanguageSettings() {
    // no-op on desktop
}
