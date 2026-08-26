package avans.avd

import avans.avd.users.Role
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthTest {
    @Test
    fun `login bad password`() = testApplication {
        application {
            installTestModules()
        }

        client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("{\"username\":\"Henk\",\"password\":\"pwd0\"}")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `login happy path`() = testApplication {
        application {
            installTestModules()
        }

        client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("{\"username\":\"Henk\",\"password\":\"pwd\"}")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `login with non-existing username`() = testApplication {
        application {
            installTestModules()
        }

        client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("{\"username\":\"DoesNotExist\",\"password\":\"pwd\"}")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `accessing secured endpoint with valid token`() = testApplication {
        application {
            installTestModules()
        }

        client.get("/api/incidents") {
            authenticate(Role.ADMIN)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `accessing secured endpoint without token`() = testApplication {
        application {
            installTestModules()
        }

        client.get("/api/incidents").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }
}
