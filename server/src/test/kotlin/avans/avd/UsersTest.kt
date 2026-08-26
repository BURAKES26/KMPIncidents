package avans.avd

import avans.avd.users.Role
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains

class UsersTest {

    @Test
    fun `get current user - happy path`() = testApplication {
        application { installTestModules() }

        client.get("/api/users/me") {
            authenticate(Role.USER)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `get current user - no access`() = testApplication {
        application { installTestModules() }

        client.get("/api/users/me").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `update current user profile - happy path`() = testApplication {
        application { installTestModules() }

        client.put("/api/users/me") {
            authenticate(Role.USER)
            contentType(ContentType.Application.Json)
            setBody("""{"username":"updateduser","email":"updated@example.com"}""")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val body = bodyAsText()
            assertContains(body, "updateduser")
        }
    }

    @Test
    fun `update current user profile - no access`() = testApplication {
        application { installTestModules() }

        client.put("/api/users/me") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"updateduser","email":"updated@example.com"}""")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `update current user profile - change password`() = testApplication {
        application { installTestModules() }

        client.put("/api/users/me") {
            authenticate(Role.USER)
            contentType(ContentType.Application.Json)
            setBody("""{"username":"updateduser","email":"updated@example.com","password":"newpassword123"}""")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `get all users - admin only`() = testApplication {
        application { installTestModules() }

        client.get("/api/users") {
            authenticate(Role.ADMIN)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `get all users - forbidden for regular user`() = testApplication {
        application { installTestModules() }

        client.get("/api/users") {
            authenticate(Role.USER)
        }.apply {
            assertEquals(HttpStatusCode.Forbidden, status)
        }
    }

    @Test
    fun `get all users - no access`() = testApplication {
        application { installTestModules() }

        client.get("/api/users").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }
}
