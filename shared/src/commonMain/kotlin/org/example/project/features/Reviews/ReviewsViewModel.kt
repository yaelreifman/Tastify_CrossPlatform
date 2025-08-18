package org.example.project.features.Reviews

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.features.BaseViewModel
import org.example.project.model.Review
import org.example.project.model.Reviews

class ReviewsViewModel:BaseViewModel() {
private val _uiState: MutableStateFlow<ReviewsState> = MutableStateFlow(ReviewsState.Loading)
    val uiState: StateFlow<ReviewsState> get() = _uiState
init {
   fetchReviews()
}
    private fun fetchReviews(){
        scope.launch {
            val reviews = createMockReviewData()
            delay(1500)

            _uiState.emit(
                ReviewsState.Loaded(reviews)
                //ReviewsState.Error("the reviews did not load!")
            )
        }
    }
}
private fun createMockReviewData(): Reviews {
    val reviewsList = listOf(
        Review(
            id = "123",
            userId = "123",
            restaurantId = "111",
            rating = 5,
            comment = "nice",
            imagePath = "none",

        ),
        Review(
            id = "423",
            userId = "173",
            restaurantId = "111",
            rating = 5,
            comment = "nice",
            imagePath = "none",

            )
    )
    return Reviews(items = reviewsList)
}