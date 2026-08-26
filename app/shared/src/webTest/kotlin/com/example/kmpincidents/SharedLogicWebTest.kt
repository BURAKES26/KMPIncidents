package com.example.kmpincidents

import com.example.kmpincidents.data.store.TokenPreferences
import com.example.kmpincidents.util.PhotoFileResolver
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests voor de Web-specifieke implementaties in `webMain`.
 * Dit dekt de platformscenario's P2-P4 uit `docs/test-scenarios.md`.
 */
class SharedLogicWebTest {

    private val tokenPreferences = TokenPreferences()

    @AfterTest
    fun tearDown() = runTest {
        tokenPreferences.clearToken()
    }

    // P2 - Bepalen van bestandspad/resolutie van foto
    @Test
    fun `PhotoFileResolver - resolves a valid base64 data URI`() {
        val dataUri = "data:image/png;base64,aGVsbG8gd29ybGQ=" // "hello world"

        val resolved = PhotoFileResolver().resolveToFile(dataUri)

        assertTrue(resolved != null)
        assertEquals("photo.png", resolved.name)
        assertTrue(resolved.exists())
    }

    @Test
    fun `PhotoFileResolver - returns null for a non data URI`() {
        val resolved = PhotoFileResolver().resolveToFile("https://example.com/photo.png")
        assertNull(resolved)
    }

    // P3 - Lezen/schrijven van een PlatformFile
    @Test
    fun `PlatformFile - readBytes returns the decoded file contents`() = runTest {
        val dataUri = "data:image/jpeg;base64,aGVsbG8gd29ybGQ=" // "hello world"

        val platformFile = PhotoFileResolver().resolveToFile(dataUri)!!

        assertEquals("hello world", platformFile.readBytes().decodeToString())
    }

    // P4 - Token opslag en verwijdering
    @Test
    fun `TokenPreferences - token is retrievable after saving`() = runTest {
        tokenPreferences.saveToken("test-token-value")

        assertEquals("test-token-value", tokenPreferences.getToken())
    }

    @Test
    fun `TokenPreferences - token is empty after clearing`() = runTest {
        tokenPreferences.saveToken("test-token-value")
        tokenPreferences.clearToken()

        assertNull(tokenPreferences.getToken())
    }

    @Test
    fun `TokenPreferences - getUserRole returns null when no token is stored`() = runTest {
        tokenPreferences.clearToken()

        assertNull(tokenPreferences.getUserRole())
    }
}