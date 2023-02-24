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

data class SessionContent(val user: String, val accessRights: List<String>) : Principal

@Serializable
data class LoginRequest(val username: String, val password: String)

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
                if (isValidLoginLDAP(loginRequestBody)) {
                    call.sessions.set(SessionContent(loginRequestBody.username, emptyList()))
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



private fun isValidLogin(loginRequestBody: LoginRequest) =
    loginRequestBody.username == "Philip J. Fry" && loginRequestBody.password == "fry"



private fun isValidLoginLDAP(loginRequestBody: LoginRequest) =
    ldapAuthenticate(
        UserPasswordCredential(loginRequestBody.username, loginRequestBody.password),
        "ldap://localhost:10389",
        "cn=%s,ou=people,dc=planetexpress,dc=com"
    ) != null



@Serializable
data class WhoAmIResponse(val user: String, val accessRights: List<String>) {
    companion object {
        fun fromPrincipal(session: SessionContent) = WhoAmIResponse(session.user, session.accessRights)
    }
}
