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
import kotlinx.coroutines.Job
import kotlinx.datetime.Instant
import org.example.project.features.BaseViewModel
import org.example.project.model.Reviews
import org.example.project.model.Review
import org.example.project.data.ReviewsRepository
import org.example.project.data.FirebaseReviewsRepository
import org.example.project.data.GeocodingRepository
import kotlinx.datetime.Clock

class ReviewsViewModel(
    private val repo: ReviewsRepository = FirebaseReviewsRepository()
) : BaseViewModel() {

    private val _uiState: MutableStateFlow<ReviewsState> =
        MutableStateFlow(ReviewsState.Loading)
    val uiState: StateFlow<ReviewsState> get() = _uiState

    private var listenJob: Job? = null

    init {
        startListening()
    }

    private fun startListening() {
        listenJob?.cancel()
        listenJob = scope.launch {
            repo.listenReviews()
                .onStart { _uiState.value = ReviewsState.Loading }
                .catch { e ->
                    e.printStackTrace()
                    _uiState.value = ReviewsState.Error(e.message ?: "Unknown error")
                }
                .collectLatest { reviews ->
                    // ✅ מיון תמידי חדש→ישן לפי createdAt (תומך גם ב-ISO וגם במספר מיליס)
                    val sorted = reviews.items.sortedByDescending { createdAtToMillis(it.createdAt) }
                    _uiState.value = ReviewsState.Loaded(Reviews(sorted))
                }
        }
    }

    fun refresh() {
        _uiState.value = ReviewsState.Loading
        startListening()
    }

    fun addReview(review: Review) {
        scope.launch {
            try {
                _uiState.value = ReviewsState.Loading

                var enrichedReview = review

                // אם חסר createdAt – נמלא עכשיו בפורמט ISO-8601 UTC
                if (enrichedReview.createdAt.isNullOrBlank()) {
                    val nowIso = Clock.System.now().toString() // לדוגמה: 2025-08-20T11:24:33.512Z
                    enrichedReview = enrichedReview.copy(createdAt = nowIso)
                }

                // ג׳יאוקודינג אם יש כתובת אבל אין קואורדינטות
                if (!enrichedReview.address.isNullOrBlank() &&
                    enrichedReview.latitude == null &&
                    enrichedReview.longitude == null
                ) {
                    val coords = withContext(Dispatchers.IO) {
                        runCatching {
                            GeocodingRepository.getCoordinatesFromAddress(enrichedReview.address!!)
                        }.getOrNull()
                    }
                    coords?.let { (lat, lng) ->
                        enrichedReview = enrichedReview.copy(latitude = lat, longitude = lng)
                    }
                }

                // שמירה
                withContext(Dispatchers.IO) { repo.addReview(enrichedReview) }

                // רענון אחרי שמירה
                refresh()

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = ReviewsState.Error("Failed to add review: ${e.message}")
                startListening()
            }
        }
    }
}

/** ממיר createdAt (String?) למיליס לצורך מיון:
 * - אם זה מספר => מיליס ישירות
 * - אם זה ISO8601 => Instant.parse
 * - אחרת => 0 (כדי “ליפול” לסוף הרשימה)
 */
private fun createdAtToMillis(createdAt: String?): Long {
    if (createdAt.isNullOrBlank()) return 0L
    // מספר טהור (יכול להיות "1724140000000")
    createdAt.toLongOrNull()?.let { return it }
    // ISO8601
    return runCatching { Instant.parse(createdAt).toEpochMilliseconds() }.getOrElse { 0L }
}
