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
import kotlinx.datetime.Clock // ← חדש: לשימוש ב-ISO8601 UTC


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
                    _uiState.value = ReviewsState.Loaded(reviews)
                }
        }
    }

    fun addReview(review: Review) {
        scope.launch {
            try {
                var enrichedReview = review

                // אם חסר createdAt – נמלא עכשיו (ISO-8601 UTC)
                if (enrichedReview.createdAt.isNullOrBlank()) {
                    val nowIso = Clock.System.now().toString() // "2025-08-20T11:24:33.512Z"
                    enrichedReview = enrichedReview.copy(createdAt = nowIso)
                }

                // אם יש כתובת ואין קואורדינטות – ג׳יאוקודינג לפני שמירה
                if (!enrichedReview.address.isNullOrBlank() && enrichedReview.latitude == null) {
                    val coords = withContext(Dispatchers.IO) {
                        try {
                            GeocodingRepository.getCoordinatesFromAddress(enrichedReview.address!!)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }

                    coords?.let { (lat, lng) ->
                        enrichedReview = enrichedReview.copy(
                            latitude = lat,
                            longitude = lng
                        )
                    }
                }

                // שמירה ל-Firestore
                repo.addReview(enrichedReview)

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = ReviewsState.Error("Failed to add review: ${e.message}")
            }
        }
    }
}
