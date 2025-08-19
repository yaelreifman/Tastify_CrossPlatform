package org.example.project.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.model.Review
import org.example.project.model.Reviews

interface ReviewsRepository {
    fun listenReviews(): Flow<Reviews>
}

class FirebaseReviewsRepository : ReviewsRepository {

    private val collection: FirebaseFirestoreCollectionReference
        get() = Firebase.firestore.collection("reviews")

    override fun listenReviews(): Flow<Reviews> {
        // מאזין בזמן אמת לשינויים ב-Firestore
        return collection.snapshots.map { snap ->
            val items = snap.documents.mapNotNull { doc ->
                // מיפוי מסמך → Review
                val data = doc.data<Map<String, Any?>>()
                Review(
                    id = doc.id,
                    restaurantId = data["restaurantId"] as? String ?: "",
                    restaurantName = data["restaurantName"] as? String ?: "",
                    rating = (data["rating"] as? Number)?.toInt() ?: 0,
                    comment = data["comment"] as? String ?: "",
                    imagePath = data["imageUrl"] as? String,
                    address = data["address"] as? String,
                    latitude = (data["latitude"] as? Number)?.toDouble(),
                    longitude = (data["longitude"] as? Number)?.toDouble(),
                    placeId = data["placeId"] as? String,
                )
            }
            Reviews(items.sortedByDescending { it.createdAt ?: 0L })
        }
    }
}
