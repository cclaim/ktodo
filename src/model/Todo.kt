package com.test.model

import com.squareup.moshi.*

@JsonClass(generateAdapter = true)
data class Todo(val description: String, val isDone: Boolean = false, val type: String = "todo", var id: Int? = null)
