package org.example.project.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReviewsScreen(
    viewModel: ReviewsViewModel
) {
    val uiState = viewModel.uiState.collectAsState().value
    when (uiState) {
        is ReviewsState.Error -> ErrorContent(message = uiState.errorMessage)
        is ReviewsState.Loaded -> ReviewsContent(uiState.reviews, viewModel)
        ReviewsState.Loading -> LoadingContent()
    }
}

@Composable
fun ReviewsContent(
    reviews: Reviews,
    viewModel: ReviewsViewModel,
    lazyListState: LazyListState = rememberLazyListState()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val filteredReviews = remember(searchQuery, reviews.items) {
        if (searchQuery.isBlank()) {
            reviews.items
        } else {
            reviews.items.filter { review ->
                review.id.contains(searchQuery, ignoreCase = true) ||
                        review.comment?.contains(searchQuery, ignoreCase = true) == true ||
                        review.restaurantName?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize()
    ) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("ADD NEW REVIEW")
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            label = { Text("Search reviews") },
            singleLine = true
        )

        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredReviews) { review ->
                ReviewContent(review)
            }
        }
    }

    if (showDialog) {
        AddReviewDialog(
            onDismiss = { showDialog = false },
            onSave = { newReview ->
                viewModel.addReview(newReview)   // ✅ נשמר ב-Firestore
                showDialog = false
            }
        )
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
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = review.restaurantName)
                Text(text = "Rating: ${review.rating}")
                Text(text = review.comment ?: "")
            }
        }
    }
}

@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSave: (Review) -> Unit
) {
    var restaurantName by remember { mutableStateOf("") }
    var restaurantId by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var placeId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Review") },
        text = {
            Column {
                OutlinedTextField(
                    value = restaurantName,
                    onValueChange = { restaurantName = it },
                    label = { Text("Restaurant Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = restaurantId,
                    onValueChange = { restaurantId = it },
                    label = { Text("Restaurant ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rating,
                    onValueChange = { rating = it },
                    label = { Text("Rating (1-5)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imagePath,
                    onValueChange = { imagePath = it },
                    label = { Text("Image Path (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = placeId,
                    onValueChange = { placeId = it },
                    label = { Text("Place ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val review = Review(
                    id = System.currentTimeMillis().toString(),
                    restaurantId = restaurantId,
                    restaurantName = restaurantName,
                    rating = rating.toIntOrNull() ?: 0,
                    comment = comment,
                    imagePath = imagePath.ifBlank { null },
                    address = address.ifBlank { null },
                    latitude = latitude.toDoubleOrNull(),
                    longitude = longitude.toDoubleOrNull(),
                    placeId = placeId.ifBlank { null },
                    createdAt = now
                )
                onSave(review)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = TextStyle(
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
            color = MaterialTheme.colorScheme.surfaceVariant,
            trackColor = MaterialTheme.colorScheme.secondary
        )
    }
}
