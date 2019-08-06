package com.test.model

import com.squareup.moshi.*
import java.util.*

@JsonClass(generateAdapter = true)
data class Todo(val identifier: String, val description: String, val isDone: Boolean, val type: String = "todo") {

    companion object {

        fun create(description: String) : Todo {
            return Todo(UUID.randomUUID().toString(), description, false)
        }
    }
}
