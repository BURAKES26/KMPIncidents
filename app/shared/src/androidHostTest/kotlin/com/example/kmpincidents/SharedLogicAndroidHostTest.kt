package com.example.kmpincidents

import com.example.kmpincidents.util.PlatformFile
import java.io.File
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests voor de Android-specifieke implementaties in `androidMain` die geen
 * Android `Context` vereisen. Dit dekt scenario P3 uit `docs/test-scenarios.md`.
 * (`PlatformSettings`/`TokenPreferences`/`PhotoFileResolver` op Android vereisen
 * een `Context` en zijn daarom niet gedekt door deze host-tests.)
 */
class SharedLogicAndroidHostTest {

    @Test
    fun `PlatformFile - reports name and existence for an existing file`() {
        val tempFile = File.createTempFile("incident", ".txt")
        tempFile.deleteOnExit()

        val platformFile = PlatformFile(tempFile)

        assertEquals(tempFile.name, platformFile.name)
        assertTrue(platformFile.exists())
    }

    @Test
    fun `PlatformFile - exists returns false for a deleted file`() {
        val tempFile = File.createTempFile("incident", ".txt")
        tempFile.delete()

        val platformFile = PlatformFile(tempFile)

        assertFalse(platformFile.exists())
    }

    @Test
    fun `PlatformFile - readBytes returns the file contents`() = runBlocking {
        val tempFile = File.createTempFile("incident", ".txt")
        tempFile.deleteOnExit()
        tempFile.writeText("hello android")

        val platformFile = PlatformFile(tempFile)

        assertEquals("hello android", platformFile.readBytes().decodeToString())
    }
}
