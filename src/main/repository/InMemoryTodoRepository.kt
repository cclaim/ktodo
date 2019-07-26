package com.test.repository

import com.test.model.*
import java.util.concurrent.atomic.*

class InMemoryTodoRepository: TodoRepository {
    override suspend fun update(id: Int, todo: Todo): Todo? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val todos = ArrayList<Todo>()
    val counter = AtomicInteger()

    override suspend fun add(todo: Todo): Todo {
        if(todo in todos) {
            return todos.find { it == todo }!!
        }
        todos.add(todo)
        return todo
    }

    override suspend fun getById(id: Int): Todo? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun remove(id: Int) = TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    override suspend fun remove(todo: Todo) {
        if(!todos.contains(todo)) {
            throw IllegalArgumentException("No todo found for ${todo.id}.")
        } else {
            todos.remove(todo)
        }
    }

    override suspend fun clear() = todos.clear()

    override suspend fun all() = todos.toList()
}
