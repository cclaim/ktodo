package com.test

import com.couchbase.client.java.*
import com.ryanharter.ktor.moshi.*
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.test.api.*
import com.test.repository.*
import com.test.routes.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*
import java.util.logging.Level.FINE
import com.sun.org.apache.xerces.internal.util.DOMUtil.getParent
import java.util.logging.*


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(DefaultHeaders)
    install(StatusPages) {
        exception<Throwable> { e ->
            call.respondText(e.localizedMessage,
                ContentType.Text.Plain, HttpStatusCode.InternalServerError
            )
        }
    }

    install(ContentNegotiation) {
        moshi {
            /*add(UUID::class.java, object: JsonAdapter<UUID>() {
                override fun toJson(writer: JsonWriter, value: UUID?) {
                    writer.value(value.toString())
                }

                override fun fromJson(reader: JsonReader): UUID? {
                    return UUID.fromString(reader.nextString())
                }
            })
            add(KotlinJsonAdapterFactory())*/
        }
    }

    // IN MEMORY REP
    //val db = InMemoryTodoRepository()

    // COUCHBASE REP
    val couchbaseCluster = CouchbaseCluster.create("localhost")
    couchbaseCluster.authenticate("admin", "password")
    val todoBucket = couchbaseCluster.openBucket("todo")
    todoBucket.bucketManager().createN1qlPrimaryIndex(true, false);
    val db = CouchbaseTodoRepository(todoBucket)

    routing {
        home()
        about()
        todo(db)
    }
}

const val API_VERSION = "/api/v1";


