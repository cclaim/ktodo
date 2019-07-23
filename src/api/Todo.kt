package com.test.api

import com.test.*
import com.test.model.*
import com.test.repository.*
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

const val TODO_ENDPOINT = "$API_VERSION/todo"

fun Route.todo(db: TodoRepository) {
    post(TODO_ENDPOINT) {
        val request = call.receive<Request>()
        val todo = db.add(Todo(request.description, false))
        call.respond(todo)
    }

    get(TODO_ENDPOINT) {
        val todos = db.all()
        call.respond(todos.toTypedArray())
    }

    delete(TODO_ENDPOINT) {
        db.clear()
        call.respondText("Done")
    }


}
