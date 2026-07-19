package com.fitnesse.app.data.local

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object PhotoStorage {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun savePhoto(bytes: ByteArray): String = withContext(Dispatchers.IO) {
        val dir = File(appContext.filesDir, "photos")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.writeBytes(bytes)
        Uri.fromFile(file).toString()
    }

    fun deletePhoto(photoUrl: String) {
        try {
            val file = File(Uri.parse(photoUrl).path!!)
            if (file.exists()) file.delete()
        } catch (_: Exception) {}
    }
}
