package org.example.project.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.firestore // ← חשוב ל-import הזה!
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.model.Review
import org.example.project.model.Reviews

interface ReviewsRepository {
    fun listenReviews(): Flow<Reviews>
}

class FirebaseReviewsRepository : ReviewsRepository {

    private val collection: CollectionReference
        get() = Firebase.firestore.collection("reviews")

    override fun listenReviews(): Flow<Reviews> {
        return collection.snapshots.map { snapshot ->
            val items = snapshot.documents.map { doc ->
                // קוראים מסמך ל-DTO סריאליזבילי
                val w: Review = doc.data()  // דורש kotlinx-serialization-json

                // ממפים ל-Review המשותף שלך
                Review(
                    id = doc.id,
                    restaurantId = w.restaurantId.orEmpty(),
                    restaurantName = w.restaurantName.orEmpty(),
                    rating = w.rating ?: 0,
                    comment = w.comment.orEmpty(),

                    // שימי לב: במודל שלך היה imagePath; ממפים מ-imageUrl של Firestore
                    imagePath = w.imagePath,  // ← מומלץ ליישר את המודל שלך לשם הזה

                    address = w.address,
                    latitude = w.latitude,
                    longitude = w.longitude,
                    placeId = w.placeId,
                    createdAt = w.createdAt
                )
            }

            Reviews(items.sortedByDescending { it.createdAt ?: 0L })
        }
    }
}
