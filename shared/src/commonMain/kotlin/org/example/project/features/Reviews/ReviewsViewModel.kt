package org.example.project.features.Reviews

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.example.project.features.BaseViewModel
import org.example.project.model.Reviews
import org.example.project.model.Review
import org.example.project.data.ReviewsRepository
import org.example.project.data.FirebaseReviewsRepository
import org.example.project.data.GeocodingRepository

class ReviewsViewModel(
    private val repo: ReviewsRepository = FirebaseReviewsRepository()
) : BaseViewModel() {

    private val _uiState: MutableStateFlow<ReviewsState> =
        MutableStateFlow(ReviewsState.Loading)
    val uiState: StateFlow<ReviewsState> get() = _uiState

    init {
        fetchReviews()
    }

    private fun fetchReviews() {
        scope.launch {
            repo.listenReviews()
                .onStart { _uiState.value = ReviewsState.Loading }
                .map { reviews: Reviews ->   // ✅ טיפוס מפורש
                    val enriched = reviews.items.map { review ->
                        if (!review.address.isNullOrBlank() && review.latitude == null) {
                            GeocodingRepository.getCoordinatesFromAddress(review.address!!)
                                ?.let { coords ->
                                    review.copy(
                                        latitude = coords.first,
                                        longitude = coords.second
                                    )
                                } ?: review
                        } else review
                    }
                    Reviews(enriched)
                }
                .catch { e ->
                    _uiState.value = ReviewsState.Error(e.message ?: "Unknown error")
                }
                .collectLatest { reviews ->
                    _uiState.value = ReviewsState.Loaded(reviews)
                }
        }
    }

    fun addReview(review: Review) {
        scope.launch {
            try {
                var enrichedReview = review
                if (!review.address.isNullOrBlank()) {
                    GeocodingRepository.getCoordinatesFromAddress(review.address!!)
                        ?.let { coords ->
                            enrichedReview = review.copy(
                                latitude = coords.first,
                                longitude = coords.second
                            )
                        }
                }
                repo.addReview(enrichedReview)
            } catch (e: Exception) {
                _uiState.value = ReviewsState.Error("Failed to add review: ${e.message}")
            }
        }
    }
}
