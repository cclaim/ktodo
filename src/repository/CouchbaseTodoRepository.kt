package com.test.repository

import com.couchbase.client.java.*
import com.couchbase.client.java.document.*
import com.couchbase.client.java.document.json.*
import com.couchbase.client.java.query.*
import com.squareup.moshi.*
import com.test.model.*

const val TODO_ID_PREFIX = "TODO::"

class CouchbaseTodoRepository(val bucket: Bucket): TodoRepository {

    override suspend fun all(): List<Todo> {
        val result = bucket.query(
            N1qlQuery.simple(
                "SELECT id, description, isDone FROM `todo` WHERE type = \"todo\""
            )
        )

        val moshi = Moshi.Builder().build()
        val adapterType = Types.newParameterizedType(List::class.java, Todo::class.java)
        val adapter : JsonAdapter<List<Todo>> = moshi.adapter(adapterType)
        val todos: List<Todo> = adapter.fromJson(result.allRows().toString())!!

        return todos;
    }

    override suspend fun clear() {
        val result = bucket.query(
            N1qlQuery.simple(
                "DELETE FROM `todo` WHERE type = \"todo\""
            )
        )
        if (!result.finalSuccess()) {
            throw Exception("Query error: " + result.errors());
        }
    }

    override suspend fun add(todo: Todo): Todo {
        val nextId = bucket.counter("idGeneratorForTodos", 1, 0).content();
        val id = TODO_ID_PREFIX + nextId
        todo.id = nextId.toInt()

        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(Todo::class.java)
        val todoJson = jsonAdapter.toJson(todo)

        bucket.upsert(JsonDocument.create(id, JsonObject.fromJson(todoJson)));
        return todo
    }

    override suspend fun getById(id: Int): Todo? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun remove(id: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun remove(todo: Todo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
