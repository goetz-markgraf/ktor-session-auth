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

class ApplicationTest {
    @Test
    fun `should login successfully`() = clientTest { client ->

        val responseLogin = client.post("/public/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("Philip J. Fry", "fry"))
        }
        assertEquals(HttpStatusCode.OK, responseLogin.status)

        val whoAmI = client.get("/api/who-am-i")
        assertEquals(HttpStatusCode.OK, whoAmI.status)
        assertEquals(WhoAmIResponse("Philip J. Fry", emptyList()), whoAmI.body())
    }

    @Test
    fun `should not be able to call who-am-i without login first`() = clientTest { client ->
        val whoAmI = client.get("/api/who-am-i")
        assertEquals(HttpStatusCode.Forbidden, whoAmI.status)
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
