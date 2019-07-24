package com.test.repository

import com.couchbase.client.java.*
import com.couchbase.client.java.document.*
import com.couchbase.client.java.document.json.*
import com.couchbase.client.java.query.*
import com.couchbase.client.java.query.Delete.deleteFrom
import com.couchbase.client.java.query.Select.select
import com.couchbase.client.java.query.dsl.Expression.i
import com.couchbase.client.java.query.dsl.Expression.x
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

        return todos
    }

    override suspend fun clear() {
        val result = bucket.query(
            N1qlQuery.simple(
                "DELETE FROM `todo` WHERE type = \"todo\""
            )
        )
        if (!result.finalSuccess()) {
            throw Exception("Query error: " + result.errors())
        }
    }

    override suspend fun add(todo: Todo): Todo {
        val nextId = bucket.counter("idGeneratorForTodos", 1, 0).content()
        val id = TODO_ID_PREFIX + nextId
        todo.id = nextId.toInt()

        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(Todo::class.java)
        val todoJson = jsonAdapter.toJson(todo)

        bucket.upsert(JsonDocument.create(id, JsonObject.fromJson(todoJson)))
        return todo
    }

    override suspend fun getById(id: Int): Todo? {
        val statement = select("id", "description", "isDone")
            .from(i("todo"))
            .where(x("id").eq(x("$id")))
        val placeholderValues = JsonObject.create().put("id", id)
        val query = N1qlQuery.parameterized(statement, placeholderValues)
        val result = bucket.query(query)

        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(Todo::class.java)
        val todo = adapter.fromJson(result.first().toString())!!

        return todo
    }

    override suspend fun update(id: Int, todo: Todo): Todo? {
        val statement = com.couchbase.client.java.query.Update.update("todo")
            .set("description", todo.description)
            .set("isDone", todo.isDone)
            .where(x("id").eq(x("$id")))
        val placeholderValues = JsonObject.create()
            .put("id", id)
        val query = N1qlQuery.parameterized(statement, placeholderValues)
        val result = bucket.query(query)
        return getById(id)
    }

    override suspend fun remove(id: Int): Boolean {
        val statement = deleteFrom("todo")
            .where(x("id").eq(x("$id")))
        val query = N1qlQuery.simple(statement)
        val result = bucket.query(query)
        if (!result.finalSuccess()) {
            throw Exception("Query error: " + result.errors())
        } else {
            return true
        }
    }

    override suspend fun remove(todo: Todo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
