package de.codecentric.ktorSessionAuth

import de.codecentric.ktorSessionAuth.plugins.configureRouting
import de.codecentric.ktorSessionAuth.plugins.configureSecurity
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    configureSecurity()

    configureRouting()
}
