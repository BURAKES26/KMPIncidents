package com.example.kmpincidents

import com.example.kmpincidents.util.ChangeUserValidationHelper
import com.example.kmpincidents.util.IncidentDisplayHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 * Tests voor de gedeelde businesslogica in `commonMain`.
 * Dit dekt de scenario's C1-C6 uit `docs/test-scenarios.md`.
 */
class SharedCommonTest {

    // C1 - Validatie van profielgegevens met geldige input
    @Test
    fun `validateUserProfile - valid data without password change`() {
        val result = ChangeUserValidationHelper.validateUserProfile(
            username = "johndoe",
            email = "john@example.com",
            currentPassword = "",
            newPassword = "",
            confirmPassword = ""
        )
        assertNull(result)
    }

    @Test
    fun `validateUserProfile - valid data with password change`() {
        val result = ChangeUserValidationHelper.validateUserProfile(
            username = "johndoe",
            email = "john@example.com",
            currentPassword = "oldpassword",
            newPassword = "newpassword",
            confirmPassword = "newpassword"
        )
        assertNull(result)
    }

    // C2 - Validatie van profielgegevens met lege verplichte velden
    @Test
    fun `validateUserProfile - blank username`() {
        val result = ChangeUserValidationHelper.validateUserProfile(
            username = "",
            email = "john@example.com",
            currentPassword = "",
            newPassword = "",
            confirmPassword = ""
        )
        assertEquals("Username cannot be empty", result)
    }

    @Test
    fun `validateUserProfile - blank email`() {
        val result = ChangeUserValidationHelper.validateUserProfile(
            username = "johndoe",
            email = "",
            currentPassword = "",
            newPassword = "",
            confirmPassword = ""
        )
        assertEquals("Email cannot be empty", result)
    }

    // C6 - Foutafhandeling bij ongeldige input
    @Test
    fun `validateUserProfile - username too short`() {
        val result = ChangeUserValidationHelper.validateUserProfile(
            username = "jo",
            email = "john@example.com",
            currentPassword = "",
            newPassword = "",
            confirmPassword = ""
        )
        assertEquals("Username must be at least 3 characters", result)
    }

    @Test
    fun `validateUserProfile - invalid email format`() {
        val result = ChangeUserValidationHelper.validateUserProfile(
            username = "johndoe",
            email = "not-an-email",
            currentPassword = "",
            newPassword = "",
            confirmPassword = ""
        )
        assertEquals("Please enter a valid email address", result)
    }

    @Test
    fun `validateUserProfile - password change with mismatching passwords`() {
        val result = ChangeUserValidationHelper.validateUserProfile(
            username = "johndoe",
            email = "john@example.com",
            currentPassword = "oldpassword",
            newPassword = "newpassword",
            confirmPassword = "different"
        )
        assertEquals("Passwords do not match", result)
    }

    @Test
    fun `validateUserProfile - password change with too short new password`() {
        val result = ChangeUserValidationHelper.validateUserProfile(
            username = "johndoe",
            email = "john@example.com",
            currentPassword = "oldpassword",
            newPassword = "123",
            confirmPassword = "123"
        )
        assertEquals("Password must be at least 6 characters", result)
    }

    // C5 - Formattering van datums voor weergave
    @Test
    fun `formatDateForDisplay - formats valid ISO date`() {
        val result = IncidentDisplayHelper.formatDateForDisplay("2024-03-05T10:15:30")
        assertEquals("05-03-2024", result)
    }

    @Test
    fun `formatDateForDisplay - formats valid ISO date with Z suffix`() {
        val result = IncidentDisplayHelper.formatDateForDisplay("2024-12-25T00:00:00Z")
        assertEquals("25-12-2024", result)
    }

    // C6 - Foutafhandeling bij ongeldige input (geen crash, nette fallback)
    @Test
    fun `formatDateForDisplay - returns original string on invalid input`() {
        val invalidDate = "not-a-date"
        val result = IncidentDisplayHelper.formatDateForDisplay(invalidDate)
        assertEquals(invalidDate, result)
    }

    @Test
    fun `formatDateForDisplay - returns original string on blank input`() {
        val result = IncidentDisplayHelper.formatDateForDisplay("")
        assertEquals("", result)
    }

    // C3 - Mapping van domeinwaarden naar weergave-eigenschappen (kleuren zijn consistent per waarde)
    @Test
    fun `getStatusColor - returns distinct colors per status`() {
        val colors = com.example.kmpincidents.data.model.Status.entries.map { status ->
            IncidentDisplayHelper.getStatusColorValue(status)
        }
        assertEquals(colors.size, colors.toSet().size)
    }

    @Test
    fun `getPriorityColors - returns distinct colors per priority`() {
        val colors = com.example.kmpincidents.data.model.Priority.entries.map { priority ->
            IncidentDisplayHelper.getPriorityColorValues(priority)
        }
        assertEquals(colors.size, colors.toSet().size)
    }

    @Test
    fun `getRoleColor - admin and user colors differ`() {
        val adminColor = IncidentDisplayHelper.getRoleColorValue(com.example.kmpincidents.data.model.Role.ADMIN)
        val userColor = IncidentDisplayHelper.getRoleColorValue(com.example.kmpincidents.data.model.Role.USER)
        assertNotEquals(adminColor, userColor)
    }
}
