package org.example.project.features

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.features.Reviews.ReviewsState
import org.example.project.features.Reviews.ReviewsViewModel
import org.example.project.model.Review
import org.example.project.model.Reviews

@Composable
fun ReviewsScreen(
    viewModel: ReviewsViewModel
) {
    val uiState = viewModel.uiState.collectAsState().value
    when (uiState) {
        is ReviewsState.Error -> ErrorContent(message = uiState.errorMessage)
        is ReviewsState.Loaded -> ReviewsContent(uiState.reviews)
        ReviewsState.Loading -> LoadingContent()
    }
}

@Composable
fun ReviewsContent(
    reviews: Reviews,
    lazyListState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(8.dp)
    ) {
items(reviews.items) {review ->
    ReviewContent(review)
}
    }

}

@Composable
fun ReviewContent(
    review: Review,
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .height(160.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    )
    {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = review.id)
                Text(text = "${review.rating}")
            }
        }
    }
}
@Composable
fun ErrorContent(message: String){

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Text(
            text= message,
            style= TextStyle(
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color= MaterialTheme.colorScheme.surfaceVariant,
            trackColor = MaterialTheme.colorScheme.secondary
        )

    }
}
