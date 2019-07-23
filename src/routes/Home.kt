package com.test.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

const val HOME_ROUTE = "/"

fun Route.home() {
    get(HOME_ROUTE) {
        call.respondText("Hello Kotlin")
    }
}
