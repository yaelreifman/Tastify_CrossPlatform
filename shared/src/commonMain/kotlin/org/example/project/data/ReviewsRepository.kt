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
                val w: Review = doc.data()

                Review(
                    id = doc.id,
                    restaurantName = w.restaurantName.orEmpty(),
                    rating = w.rating ?: 0,
                    comment = w.comment.orEmpty(),
                    address = w.address.orEmpty(),
                    imagePath = w.imagePath.orEmpty()
                )
            }
            Reviews(items.sortedByDescending { it.id })
        }
    }

    override suspend fun addReview(review: Review) {
        collection.add(
            Review(
                restaurantName = review.restaurantName,
                rating = review.rating,
                comment = review.comment,
                address = review.address,
                imagePath = review.imagePath
            )
        )
    }
}
