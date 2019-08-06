package main.viewmodel

import com.test.api.*
import com.test.viewmodel.*

data class TodoCollection(val objects: Collection<TodoResource>) {
    val links = mapOf(Pair("self", "$TODO_ENDPOINT"))
}
