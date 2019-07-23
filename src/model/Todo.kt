package com.test.model

import com.squareup.moshi.*
import java.util.*

@JsonClass(generateAdapter = true)
data class Todo(val description: String, val isDone: Boolean = false, val type: String = "todo")
