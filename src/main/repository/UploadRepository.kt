package main.repository

import com.test.model.*
import main.model.*

interface UploadRepository {
    suspend fun add(upload: Upload): Upload
    suspend fun all(): List<Upload>
    suspend fun getById(id: Int): Upload?
    suspend fun remove(id: Int): Boolean
    suspend fun clear()
}
