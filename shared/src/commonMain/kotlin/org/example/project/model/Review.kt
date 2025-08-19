package org.example.project.model


data class Review(
    var id: String = "",
    var restaurantId: String = "",
    var rating: Int = 0,
    var comment: String = "",
    var imagePath: String? = null,

    var restaurantName: String = "",
    var address: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var placeId: String? = null
)
