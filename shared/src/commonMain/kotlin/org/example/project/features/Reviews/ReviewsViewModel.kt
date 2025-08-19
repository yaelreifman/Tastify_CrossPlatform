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
import org.example.project.domain.EnrichReviewLocationUseCase
import org.example.project.location.DummyRestaurantLocationDataSource

class ReviewsViewModel(
    private val repo: ReviewsRepository = FirebaseReviewsRepository(),
    private val enrichLocation: EnrichReviewLocationUseCase =
        EnrichReviewLocationUseCase(DummyRestaurantLocationDataSource())
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
                .map { reviews ->
                    val enriched = reviews.items.map { review ->
                        enrichLocation.enrich(review)
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
                repo.addReview(review)
            } catch (e: Exception) {
                _uiState.value = ReviewsState.Error("Failed to add review: ${e.message}")
            }
        }
    }
}
