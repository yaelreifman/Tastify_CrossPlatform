package org.example.project.location

import org.example.project.data.GeocodingRepository
import org.example.project.model.Coordinates
import org.example.project.model.Review

/**
 * מממש את RestaurantLocationDataSource בעזרת GeocodingRepository:
 * - אם יש כבר latitude/longitude בביקורת — מחזיר אותם.
 * - אחרת, מנסה לפי address; ואם ריק — לפי restaurantName.
 */
class GeocodingLocationDataSource : RestaurantLocationDataSource {

    override suspend fun resolve(review: Review): Coordinates? {
        // אם כבר יש קואורדינטות — נחזיר אותן כפי שהן
        val lat = review.latitude
        val lng = review.longitude
        if (lat != null && lng != null) {
            return Coordinates(lat, lng)
        }

        // נבחר כתובת/שם למסירה לג׳אוקודינג
        val query: String = when {
            // address יכול להיות nullable או ריק—נבדוק בבטחה
            (review.address ?: "").isNotBlank() -> review.address!!
            review.restaurantName.isNotBlank()  -> review.restaurantName
            else                                -> return null
        }

        // קריאה לריפו (הוא כבר מטפל ב-timeout/שגיאות וגם עושה fallback לת״א)
        val pair = GeocodingRepository.getCoordinatesFromAddress(query)
        return pair?.let { (latD, lngD) -> Coordinates(latD, lngD) }
    }
}
