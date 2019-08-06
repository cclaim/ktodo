package com.test.api;

import com.test.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import main.model.*
import main.repository.*
import java.io.*

const val UPLOAD_ENDPOINT = "$API_VERSION/upload"

fun Route.upload(db: UploadRepository) {
    post(UPLOAD_ENDPOINT) {
        val multipart = call.receiveMultipart()
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val extension = File(part.originalFileName).extension
                    val file = File(Configuration.uploadDirectory, part.originalFileName.toString())
                    part.streamProvider()
                        .use { input ->
                            file.outputStream().buffered().use { output ->
                                input.copyTo(output)
                            }
                        }
                    val upload = db.add(Upload(file.absolutePath, part.originalFileName.toString(), extension))
                    call.respond(upload)
                }
            }
            part.dispose()
        }
    }

    get(UPLOAD_ENDPOINT + "/{id}") {
        val upload = db.getById((Integer.parseInt(call.parameters["id"])))
        if(upload != null) {
            val file = File(upload.uploadPath)
            if(file.exists()) {
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } else {
            call.respond(HttpStatusCode.NotFound)
        }

    }

    get(UPLOAD_ENDPOINT) {
        val uploads = db.all()
        call.respond(uploads.toTypedArray())
    }

    delete(UPLOAD_ENDPOINT + "/{id}") {
        val uploadId = Integer.parseInt(call.parameters["id"])
        db.remove(uploadId)
        call.respondText("Done")
    }
}
