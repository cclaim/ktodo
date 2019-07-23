package com.test.repository

import com.test.model.*
import java.util.*

interface TodoRepository {
    suspend fun add(todo: Todo): Todo
    suspend fun all(): List<Todo>
    suspend fun getById(id: Int): Todo?
    suspend fun remove(id: Int): Boolean
    suspend fun remove(todo: Todo)
    suspend fun clear()
}
