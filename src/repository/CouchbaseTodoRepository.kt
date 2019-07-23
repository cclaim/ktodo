package com.test.repository

import com.couchbase.client.java.*
import com.couchbase.client.java.document.*
import com.couchbase.client.java.document.json.*
import com.couchbase.client.java.query.*
import com.squareup.moshi.*
import com.test.model.*
import java.util.*

const val TODO_ID_PREFIX = "TODO::"

class CouchbaseTodoRepository(val bucket: Bucket): TodoRepository {

    override suspend fun all(): List<Todo> {
        val result = bucket.query(
            N1qlQuery.simple(
                "SELECT * FROM `todo`"
            )
        )
        val todos = ArrayList<Todo>()
        for (row in result) {
            System.out.println("row" + row.value());
        }
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
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(Todo::class.java)
        val todoJson = jsonAdapter.toJson(todo)

        val nextId = bucket.counter("idGeneratorForTodos", 1, 0).content();
        val id = TODO_ID_PREFIX + nextId

        bucket.upsert(JsonDocument.create(id, JsonObject.fromJson(todoJson)));
        return todo
    }

    override suspend fun getById(id: String): Todo? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun remove(id: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun remove(todo: Todo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
