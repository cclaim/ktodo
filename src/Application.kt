package com.test

import com.couchbase.client.java.*
import com.ryanharter.ktor.moshi.*
import com.test.api.*
import com.test.repository.*
import com.test.routes.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*

const val API_VERSION = "/api/v1"

object Configuration {
    private val properties: Properties = Properties()

    val databaseHost: String?
    val databaseUser: String?
    val databasePassword: String?
    val databaseBucketName: String?

    init {
        this.javaClass.getResourceAsStream("/app.properties")
            .use { stream ->
                properties.load(stream)
            }
        databaseHost = properties.getProperty("database.host")
        databaseBucketName = properties.getProperty("database.bucket")
        databaseUser = properties.getProperty("database.user")
        databasePassword = properties.getProperty("database.password")
    }
}

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
    install(CORS) {
        host("localhost:3000")
        method(HttpMethod.Delete)
        method(HttpMethod.Put)
    }

    install(ContentNegotiation) {
        moshi()
    }

    // IN MEMORY REP
    //val db = InMemoryTodoRepository()

    // COUCHBASE REP
    val couchbaseCluster = CouchbaseCluster.create(Configuration.databaseHost)
    couchbaseCluster.authenticate(Configuration.databaseUser, Configuration.databasePassword)
    val todoBucket = couchbaseCluster.openBucket(Configuration.databaseBucketName)
    todoBucket.bucketManager().createN1qlPrimaryIndex(true, false)
    val db = CouchbaseTodoRepository(todoBucket)

    // Routing
    routing {
        home()
        about()
        todo(db)
    }
}



