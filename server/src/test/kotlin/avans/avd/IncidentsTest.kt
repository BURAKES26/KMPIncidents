package avans.avd

import avans.avd.users.Role
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IncidentsTest {
    @Test
    fun `list of incidents - happy path`() = testApplication {
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
    fun `list of incidents - no access`() = testApplication {
        application {
            installTestModules()
        }

        client.get("/api/incidents").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun `list of incidents - no role`() = testApplication {
        application {
            installTestModules()
        }

        client.get("/api/incidents") {
            authenticate(Role.USER)
        }.apply {
            assertEquals(HttpStatusCode.Forbidden, status)
        }
    }

    @Test
    fun `create incident - happy path`() = testApplication {
        application {
            installTestModules()
        }

        client.post("/api/incidents") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"category":"TRAFFIC","description":"Test incident","latitude":51.58,"longitude":4.80,"priority":"LOW"}"""
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            assertNotNull(headers["id"])
        }
    }

    @Test
    fun `create incident - missing required field`() = testApplication {
        application {
            installTestModules()
        }

        client.post("/api/incidents") {
            contentType(ContentType.Application.Json)
            setBody("""{"category":"TRAFFIC","latitude":51.58,"longitude":4.80}""")
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun `get incident by id - happy path`() = testApplication {
        application {
            installTestModules()
        }

        val incidentId = createIncident(client)

        client.get("/api/incidents/$incidentId") {
            authenticate(Role.ADMIN)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `get incident by id - not found`() = testApplication {
        application {
            installTestModules()
        }

        client.get("/api/incidents/999999") {
            authenticate(Role.ADMIN)
        }.apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun `update incident - happy path`() = testApplication {
        application {
            installTestModules()
        }

        val incidentId = createIncident(client)

        client.put("/api/incidents/$incidentId") {
            authenticate(Role.ADMIN)
            contentType(ContentType.Application.Json)
            setBody("""{"description":"Updated description"}""")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `update incident - no access`() = testApplication {
        application {
            installTestModules()
        }

        // reported anonymously, so a plain user without qualified role may not update it
        val incidentId = createIncident(client)

        client.put("/api/incidents/$incidentId") {
            authenticate(Role.USER)
            contentType(ContentType.Application.Json)
            setBody("""{"description":"Updated description"}""")
        }.apply {
            assertEquals(HttpStatusCode.Forbidden, status)
        }
    }

    @Test
    fun `delete incident - happy path`() = testApplication {
        application {
            installTestModules()
        }

        val incidentId = createIncident(client)

        client.delete("/api/incidents/$incidentId") {
            authenticate(Role.ADMIN)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `delete incident - not found`() = testApplication {
        application {
            installTestModules()
        }

        client.delete("/api/incidents/999999") {
            authenticate(Role.ADMIN)
        }.apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    private suspend fun createIncident(client: io.ktor.client.HttpClient): String {
        val response = client.post("/api/incidents") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"category":"COMMUNAL","description":"Incident for test setup","latitude":51.58,"longitude":4.80,"priority":"LOW"}"""
            )
        }
        return response.headers["id"] ?: throw AssertionError("No incident id returned")
    }
}
