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
    suspend fun addReview(review: Review)   // ← פונקציה חדשה
}

class FirebaseReviewsRepository : ReviewsRepository {

    private val collection: CollectionReference
        get() = Firebase.firestore.collection("reviews")

    override fun listenReviews(): Flow<Reviews> {
        return collection.snapshots.map { snapshot ->
            val items = snapshot.documents.map { doc ->
                val w: Review = doc.data()

                Review(
                    id = doc.id,
                    restaurantId = w.restaurantId.orEmpty(),
                    restaurantName = w.restaurantName.orEmpty(),
                    rating = w.rating ?: 0,
                    comment = w.comment.orEmpty(),
                    imagePath = w.imagePath,
                    address = w.address,
                    latitude = w.latitude,
                    longitude = w.longitude,
                    placeId = w.placeId,
                    createdAt = w.createdAt.orEmpty() // עכשיו זה מחרוזת
                )
            }

            Reviews(items.sortedByDescending { it.createdAt })
        }
    }

    override suspend fun addReview(review: Review) {
        // נשמור במסמך חדש עם id אוטומטי
        collection.add(review)
    }
}
