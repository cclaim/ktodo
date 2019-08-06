package com.test.api

import com.test.*
import com.test.model.*
import com.test.repository.*
import com.test.viewmodel.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import main.viewmodel.*

const val TODO_ENDPOINT = "$API_VERSION/todos"

fun Route.todo(db: TodoRepository) {

    post(TODO_ENDPOINT) {
        val request = call.receive<TodoResource>()
        val todoResource = db.add(Todo.create(request.description)).toResource()
        call.respond(todoResource)
    }

    get(TODO_ENDPOINT) {
        val collection  = db.all().map { it.toResource() }.toResource()
        call.respond(collection)
    }

    get(TODO_ENDPOINT + "/{identifier}") {
        val identifier = getIdentifier() ?: return@get
        val todo = db.getByIdentifier(identifier)
        if (todo == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        replyDTO(todo)
    }

    put(TODO_ENDPOINT + "/{identifier}") {
        val identifier = getIdentifier() ?: return@put

        val todo = call.receive<TodoResource>()
            .let { db.update(Todo(identifier, it.description, it.isDone)) }

        if (todo == null) {
            call.respond(HttpStatusCode.NotFound)
            return@put
        }

        replyDTO(todo)
    }

    delete(TODO_ENDPOINT) {
        db.clear()

        call.respondText("Done")
    }

    delete(TODO_ENDPOINT + "/{identifier}") {
        val identifier = getIdentifier() ?: return@delete

        db.remove(identifier)

        call.respond(HttpStatusCode.NoContent)
    }
}

private fun Todo.toResource(): TodoResource {
    return TodoResource(this.description, this.isDone, this.identifier)
}

private fun Collection<TodoResource>.toResource(): TodoCollection {
    return TodoCollection(this)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.replyDTO(
    todo: Todo
) {
    call.respond(
        todo.toResource())
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getIdentifier(): String? {
    val identifier = call.parameters["identifier"]
    if (identifier == null) {
        call.respond(HttpStatusCode.BadRequest, "Missing parameter `identifier'")
        return null
    }
    return identifier
}
