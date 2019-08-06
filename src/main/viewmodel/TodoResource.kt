package com.test.viewmodel

import com.test.api.*
import com.test.model.*
import java.util.*

data class TodoResource(val description: String, val isDone: Boolean = false, @Transient val identifier: String) {
    val links = mapOf(Pair("self", "$TODO_ENDPOINT/$identifier"))
}
