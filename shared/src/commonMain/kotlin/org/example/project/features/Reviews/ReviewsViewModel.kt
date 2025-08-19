package org.example.project.features.Reviews

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
                .catch { e ->
                    e.printStackTrace()
                    _uiState.value = ReviewsState.Error(e.message ?: "Unknown error")
                }
                .collectLatest { reviews ->
                    // ❌ לא עושים Geocoding כאן – רק מציגים את מה שיש
                    _uiState.value = ReviewsState.Loaded(reviews)
                }
        }
    }

    fun addReview(review: Review) {
        scope.launch {
            try {
                var enrichedReview = review
                if (!review.address.isNullOrBlank() && review.latitude == null) {
                    withContext(Dispatchers.IO) {
                        try {
                            GeocodingRepository.getCoordinatesFromAddress(review.address!!)?.let { coords ->
                                enrichedReview = review.copy(
                                    latitude = coords.first,
                                    longitude = coords.second
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                repo.addReview(enrichedReview) // ✅ נשמר ל־Firestore עם קואורדינטות
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = ReviewsState.Error("Failed to add review: ${e.message}")
            }
        }
    }
}
