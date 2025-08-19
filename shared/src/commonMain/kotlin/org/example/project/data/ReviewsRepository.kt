package org.example.project.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.model.Review
import org.example.project.model.Reviews

interface ReviewsRepository {
    fun listenReviews(): Flow<Reviews>
    suspend fun addReview(review: Review)
}

class FirebaseReviewsRepository : ReviewsRepository {

    private val collection: CollectionReference
        get() = Firebase.firestore.collection("reviews")

    override fun listenReviews(): Flow<Reviews> {
        return collection.snapshots.map { snapshot ->
            val items = snapshot.documents.map { doc ->
                Review(
                    id = doc.id,
                    restaurantName = doc.get("restaurantName") as? String ?: "",
                    rating = (doc.get("rating") as? Long)?.toInt() ?: 0,
                    comment = doc.get("comment") as? String ?: "",
                    address = doc.get("address") as? String ?: "",
                    imagePath = doc.get("imagePath") as? String ?: "",
                    latitude = (doc.get("latitude") as? Double),
                    longitude = (doc.get("longitude") as? Double)
                )
            }
            Reviews(items.sortedByDescending { it.id })
        }
    }

    override suspend fun addReview(review: Review) {
        val data = mapOf(
            "restaurantName" to review.restaurantName,
            "rating" to review.rating,
            "comment" to review.comment,
            "address" to review.address,
            "imagePath" to review.imagePath,
            "latitude" to review.latitude,   // ✅ תמיד נשלח, גם אם null
            "longitude" to review.longitude  // ✅ תמיד נשלח, גם אם null
        )
        collection.add(data)
    }
}
