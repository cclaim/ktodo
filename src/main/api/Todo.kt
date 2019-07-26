package com.test.api

import com.test.*
import com.test.model.*
import com.test.repository.*
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.network.util.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.*
import java.io.*

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

    get(TODO_ENDPOINT + "/{id}") {
        val todo = db.getById((Integer.parseInt(call.parameters["id"])))
        if(todo != null) {
            call.respond(todo)
        } else {
            throw IllegalArgumentException("could not find a todo for id ${call.parameters["id"]}")
        }
    }

    put(TODO_ENDPOINT + "/{id}") {
        val todoId = Integer.parseInt(call.parameters["id"])
        val request = call.receive<Request>()
        val newTodo = db.update(todoId, Todo(request.description, request.isDone!!))
        if(newTodo != null) {
            call.respond(newTodo)
        } else {
            throw IllegalArgumentException("could not find a todo for id ${call.parameters["id"]}")
        }
    }

    delete(TODO_ENDPOINT) {
        db.clear()
        call.respondText("Done")
    }

    delete(TODO_ENDPOINT + "/{id}") {
        val todoId = Integer.parseInt(call.parameters["id"])
        db.remove(todoId)
        call.respondText("Done")
    }
}
