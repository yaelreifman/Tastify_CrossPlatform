package org.example.project.location

import org.example.project.model.Coordinates
import org.example.project.model.Review

class DummyRestaurantLocationDataSource : RestaurantLocationDataSource {
    override suspend fun resolve(review: Review) = when (review.restaurantId) {
        "111" -> Coordinates(32.0853, 34.7818) // תל אביב
        "222" -> Coordinates(31.7683, 35.2137) // ירושלים
        else  -> null
    }
}