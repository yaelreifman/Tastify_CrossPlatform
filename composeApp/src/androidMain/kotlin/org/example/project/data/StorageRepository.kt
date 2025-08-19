package org.example.project.data

import android.net.Uri
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

object StorageRepository {
    private val storage = Firebase.storage("gs://tastifykmp.firebasestorage.app")

    suspend fun uploadImage(fileUri: Uri, reviewId: String): String? {
        return try {
            val fileName = fileUri.lastPathSegment ?: "image_${System.currentTimeMillis()}.jpg"
            val ref = storage.reference.child("reviews/$reviewId/$fileName")
            Log.d("StorageRepo", "Uploading to path: ${ref.path}")
            ref.putFile(fileUri).await()
            val url = ref.downloadUrl.await().toString()
            Log.d("StorageRepo", "Download URL: $url")
            url
        } catch (e: Exception) {
            Log.e("StorageRepo", "Upload failed", e)
            null
        }
    }
}
