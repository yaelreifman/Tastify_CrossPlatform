package org.example.project.domain
import org.example.project.location.RestaurantLocationDataSource
import org.example.project.model.Review


class EnrichReviewLocationUseCase(
    private val dataSource: RestaurantLocationDataSource
) {
    suspend fun enrich(review: Review): Review {
        if (review.latitude != null && review.longitude != null) return review
        dataSource.resolve(review)?.let {
            review.latitude = it.latitude
            review.longitude = it.longitude
        }
        return review
    }
}