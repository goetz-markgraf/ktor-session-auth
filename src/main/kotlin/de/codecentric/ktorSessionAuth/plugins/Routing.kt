package de.codecentric.ktorSessionAuth.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class WhoAmIResponse(val user: String, val accessRights: List<String>) {
    companion object {
        fun fromPrincipal(session: SessionContent) = WhoAmIResponse(session.user, session.accessRights)
    }
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
