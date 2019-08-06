package com.test.repository

import com.couchbase.client.java.*
import com.couchbase.client.java.document.*
import com.couchbase.client.java.document.json.*
import com.couchbase.client.java.query.*
import com.couchbase.client.java.query.Delete.deleteFrom
import com.couchbase.client.java.query.Select.select
import com.couchbase.client.java.query.dsl.Expression.*
import com.squareup.moshi.*
import com.test.model.*

const val TODO_ID_PREFIX = "TODO::"

class CouchbaseTodoRepository(val bucket: Bucket): TodoRepository {

    override suspend fun all(): List<Todo> {
        val result = bucket.query(
            N1qlQuery.simple(
                "SELECT identifier, description, isDone FROM `todo` WHERE type = \"todo\""
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

        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(Todo::class.java)
        val todoJson = jsonAdapter.toJson(todo)

        bucket.insert(JsonDocument.create(id, JsonObject.fromJson(todoJson)))
        return todo
    }

    override suspend fun getByIdentifier(identifier: String): Todo? {
        val statement = select("identifier", "description", "isDone")
            .from(i("todo"))
            .where(x("identifier").eq(s(identifier)))
        val query = N1qlQuery.simple(statement)
        println(query)
        val result = bucket.query(query)

        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(Todo::class.java)
        val todo = adapter.fromJson(result.first().toString())!!

        return todo
    }

    override suspend fun update(todo: Todo): Todo? {
        val identifier = todo.identifier
        val statement = com.couchbase.client.java.query.Update.update("todo")
            .set("description", todo.description)
            .set("isDone", todo.isDone)
            .where(x("identifier").eq(s(identifier)))
        val placeholderValues = JsonObject.create()
            .put("identifier", identifier)

        val query = N1qlQuery.parameterized(statement, placeholderValues)
        bucket.query(query)

        return getByIdentifier(identifier)
    }

    override suspend fun remove(identifier: String): Boolean {
        val statement = deleteFrom("todo")
            .where(x("identifier").eq(s(identifier)))
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
