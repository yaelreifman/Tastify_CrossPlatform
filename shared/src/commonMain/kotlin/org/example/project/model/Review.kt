package org.example.project.model


data class Review(
    var id: String = "",
    var userId: String = "",
    var restaurantId: String = "",
    var rating: Int = 0,
    var comment: String = "",
    var imagePath: String? = null,
)

