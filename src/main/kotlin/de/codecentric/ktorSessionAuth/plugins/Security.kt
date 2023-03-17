package de.codecentric.ktorSessionAuth.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.ldap.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import kotlin.collections.set

data class SessionContent(val user: String) : Principal

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class WhoAmIResponse(val user: String) {
    companion object {
        fun fromPrincipal(session: SessionContent) = WhoAmIResponse(session.user)
    }
}



fun Application.configureSecurity() {
    install(Sessions) {
        cookie<SessionContent>("KTOR_SESSION", SessionStorageMemory()) {
            cookie.path = "/"
            cookie.extensions["SameSite"] = "lax"
        }
    }


    authentication {
        session<SessionContent> {
            validate { it }

            challenge {
                call.sessions.clear<SessionContent>()
                call.respond(HttpStatusCode.Forbidden, "not authorized")
            }
        }
    }


    routing {
        route("/public") {
            post("/login") {
                val loginRequestBody = call.receive<LoginRequest>()
                val correct = validateInLDAP(loginRequestBody)
                if (correct) {
                    call.sessions.set(SessionContent(loginRequestBody.username))
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.Forbidden)
                }
            }

            post("invalidate-session") {
                call.sessions.clear<SessionContent>()
                call.respond(HttpStatusCode.OK)
            }
        }

        authenticate {
            route("/api") {
                get("/who-am-i") {
                    call.principal<SessionContent>()?.let {
                        call.respond(WhoAmIResponse.fromPrincipal(it))
                    }
                }
            }
        }
    }
}



private fun validateLocally(loginRequestBody: LoginRequest) =
    loginRequestBody.username == "Philip J. Fry" && loginRequestBody.password == "fry"



private fun validateInLDAP(loginRequestBody: LoginRequest): Boolean {
    val user = ldapAuthenticate(
        UserPasswordCredential(loginRequestBody.username, loginRequestBody.password),
        "ldap://localhost:10389",
        "cn=%s,ou=people,dc=planetexpress,dc=com"
    ) {
        // do something with the context in `this`
        UserIdPrincipal(it.name)
    }

    return user != null
}
