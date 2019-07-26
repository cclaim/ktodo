package main.model

import com.squareup.moshi.*

@JsonClass(generateAdapter = true)
data class Upload(val uploadPath: String, val name: String, val extension: String, val type: String = "upload", var id: Int? = null)
