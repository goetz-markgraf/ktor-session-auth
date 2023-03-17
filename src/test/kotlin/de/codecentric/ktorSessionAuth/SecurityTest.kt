package de.codecentric.ktorSessionAuth

import de.codecentric.ktorSessionAuth.plugins.LoginRequest
import de.codecentric.ktorSessionAuth.plugins.WhoAmIResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class SecurityTest {
    @Test
    fun `should login successfully`() = clientTest { client ->

        client.post("/public/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("Philip J. Fry", "fry"))
        }.let {
            assertEquals(HttpStatusCode.OK, it.status)
        }

        client.get("/api/who-am-i").let {
            assertEquals(HttpStatusCode.OK, it.status)
            assertEquals(WhoAmIResponse("Philip J. Fry"), it.body())
        }
    }

    @Test
    fun `should not be able to call who-am-i without login first`() = clientTest { client ->
        client.get("/api/who-am-i").let {
            assertEquals(HttpStatusCode.Forbidden, it.status)
        }
    }
}

fun clientTest(testCode: suspend ApplicationTestBuilder.(HttpClient) -> Unit) =
    testApplication {
        val client = createClient {
            install(ContentNegotiation) { json() }
            install(HttpCookies)
        }

        testCode(client)
    }
