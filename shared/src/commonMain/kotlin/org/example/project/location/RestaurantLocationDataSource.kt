package org.example.project.location

import org.example.project.model.Coordinates
import org.example.project.model.Review

interface RestaurantLocationDataSource {
    /** משלים lat/lng אם חסרים (לפי placeId/מזהה/שם) */
    suspend fun resolve(review: Review): Coordinates?
}
