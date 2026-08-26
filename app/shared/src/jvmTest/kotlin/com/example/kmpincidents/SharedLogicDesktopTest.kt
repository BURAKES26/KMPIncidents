package com.example.kmpincidents

import com.example.kmpincidents.data.store.TokenPreferences
import com.example.kmpincidents.util.PhotoFileResolver
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests voor de JVM/desktop-specifieke implementaties in `jvmMain`.
 * Dit dekt de platformscenario's P2-P4 uit `docs/test-scenarios.md`.
 */
class SharedLogicDesktopTest {

    private val tokenPreferences = TokenPreferences()

    @AfterTest
    fun tearDown() = runBlocking {
        tokenPreferences.clearToken()
    }

    // P2 - Bepalen van bestandspad/resolutie van foto
    @Test
    fun `PhotoFileResolver - resolves existing file`() {
        val tempFile = File.createTempFile("photo", ".jpg")
        tempFile.deleteOnExit()

        val resolved = PhotoFileResolver().resolveToFile(tempFile.absolutePath)

        assertTrue(resolved != null)
        assertEquals(tempFile.name, resolved.name)
    }

    @Test
    fun `PhotoFileResolver - returns null for non-existing file`() {
        val resolved = PhotoFileResolver().resolveToFile("does-not-exist-${System.nanoTime()}.jpg")
        assertNull(resolved)
    }

    // P3 - Lezen/schrijven van een PlatformFile
    @Test
    fun `PlatformFile - reports name and existence correctly`() {
        val tempFile = File.createTempFile("incident", ".txt")
        tempFile.deleteOnExit()
        tempFile.writeText("hello world")

        val platformFile = PhotoFileResolver().resolveToFile(tempFile.absolutePath)

        assertTrue(platformFile != null)
        assertTrue(platformFile.exists())
        assertEquals(tempFile.name, platformFile.name)
    }

    @Test
    fun `PlatformFile - readBytes returns file contents`() = runBlocking {
        val tempFile = File.createTempFile("incident", ".txt")
        tempFile.deleteOnExit()
        tempFile.writeText("hello world")

        val platformFile = PhotoFileResolver().resolveToFile(tempFile.absolutePath)!!

        assertEquals("hello world", platformFile.readBytes().decodeToString())
    }

    // P4 - Token opslag en verwijdering
    @Test
    fun `TokenPreferences - token is retrievable after saving`() = runBlocking {
        tokenPreferences.saveToken("test-token-value")

        assertEquals("test-token-value", tokenPreferences.getToken())
    }

    @Test
    fun `TokenPreferences - token is empty after clearing`() = runBlocking {
        tokenPreferences.saveToken("test-token-value")
        tokenPreferences.clearToken()

        assertNull(tokenPreferences.getToken())
    }

    @Test
    fun `TokenPreferences - getUserRole returns null when no token is stored`() = runBlocking {
        tokenPreferences.clearToken()

        assertNull(tokenPreferences.getUserRole())
    }
}
