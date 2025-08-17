package org.example.project.features.Reviews

import org.example.project.model.Reviews

public sealed class ReviewsState{
    data object Loading: ReviewsState()
    data class Loaded(
        val reviews: Reviews

    ): ReviewsState()
    data class Error(
        var errorMessage: String
    ): ReviewsState()
}
