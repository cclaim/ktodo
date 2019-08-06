package com.test.repository

import com.test.model.*

interface TodoRepository {
    suspend fun add(todo: Todo): Todo
    suspend fun all(): List<Todo>
    suspend fun getByIdentifier(identifier: String): Todo?
    suspend fun update(todo: Todo): Todo?
    suspend fun remove(identifier: String): Boolean
    suspend fun remove(todo: Todo)
    suspend fun clear()
}
