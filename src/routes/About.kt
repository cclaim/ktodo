package com.test.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

const val ABOUT_ROUTE = "/about";

fun Route.about() {
    get(ABOUT_ROUTE) {
        call.respondText("About");
    }
}
