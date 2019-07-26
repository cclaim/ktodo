package main.repository

import com.couchbase.client.java.*
import com.couchbase.client.java.document.*
import com.couchbase.client.java.document.json.*
import com.couchbase.client.java.query.*
import com.couchbase.client.java.query.dsl.*
import com.squareup.moshi.*
import com.test.model.*
import com.test.repository.*
import main.model.*

const val UPLOAD_ID_PREFIX = "UPLOAD::"

class CouchbaseUploadRepository(val bucket: Bucket): UploadRepository {

    override suspend fun getById(id: Int): Upload? {
        val statement = Select.select("id", "uploadPath", "name", "extension")
            .from(Expression.i("todo"))
            .where(Expression.x("id").eq(Expression.x("$id")))
        val placeholderValues = JsonObject.create().put("id", id)
        val query = N1qlQuery.parameterized(statement, placeholderValues)
        val result = bucket.query(query)

        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(Upload::class.java)
        val upload = adapter.fromJson(result.first().toString())!!

        return upload
    }

    override suspend fun all(): List<Upload> {
        val result = bucket.query(
            N1qlQuery.simple(
                "SELECT id, name, uploadPath, extension FROM `todo` WHERE type = \"upload\""
            )
        )

        val moshi = Moshi.Builder().build()
        val adapterType = Types.newParameterizedType(List::class.java, Upload::class.java)
        val adapter : JsonAdapter<List<Upload>> = moshi.adapter(adapterType)
        val uploads: List<Upload> = adapter.fromJson(result.allRows().toString())!!

        return uploads
    }

    override suspend fun remove(id: Int): Boolean {
        val statement = Delete.deleteFrom("todo")
            .where(Expression.x("id").eq(Expression.x("$id")))
        val query = N1qlQuery.simple(statement)
        val result = bucket.query(query)
        if (!result.finalSuccess()) {
            throw Exception("Query error: " + result.errors())
        } else {
            return true
        }
    }

    override suspend fun clear() {
        val result = bucket.query(
            N1qlQuery.simple(
                "DELETE FROM `todo` WHERE type = \"upload\""
            )
        )
        if (!result.finalSuccess()) {
            throw Exception("Query error: " + result.errors())
        }
    }

    override suspend fun add(upload: Upload): Upload {
        val nextId = bucket.counter("idGeneratorForUploads", 1, 0).content()
        val id = UPLOAD_ID_PREFIX + nextId
        upload.id = nextId.toInt()

        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(Upload::class.java)
        val uploadJson = jsonAdapter.toJson(upload)

        bucket.upsert(JsonDocument.create(id, JsonObject.fromJson(uploadJson)))
        return upload
    }
}
