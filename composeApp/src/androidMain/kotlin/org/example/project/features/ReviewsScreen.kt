package org.example.project.features

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
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
                review.restaurantName?.contains(searchQuery, ignoreCase = true) == true ||
                        review.comment?.contains(searchQuery, ignoreCase = true) == true ||
                        review.address?.contains(searchQuery, ignoreCase = true) == true
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
                viewModel.addReview(newReview)   // ✅ שמירה בפיירבייס
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
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = review.restaurantName ?: "", style = MaterialTheme.typography.titleMedium)

            // ⭐ כוכבים
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (index < (review.rating ?: 0)) Color.Yellow else Color.Gray
                    )
                }
            }

            Text(text = review.comment ?: "")
            Text(text = "📍 ${review.address ?: ""}", style = MaterialTheme.typography.bodySmall)

            if (!review.imagePath.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(review.imagePath),
                    contentDescription = "Review image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
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
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") } // כאן המשתמש יכניס קישור או אחרי העלאה ל-Firebase Storage

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Review") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = restaurantName,
                    onValueChange = { restaurantName = it },
                    label = { Text("Restaurant Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // ⭐ בורר כוכבים
                Row {
                    repeat(5) { index ->
                        IconToggleButton(
                            checked = index < rating,
                            onCheckedChange = {
                                rating = index + 1
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (index < rating) Color.Yellow else Color.Gray
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") }, // בהמשך נחליף להעלאת קובץ ל-Storage
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val review = Review(
                    id = System.currentTimeMillis().toString(),
                    restaurantName = restaurantName,
                    rating = rating,
                    comment = comment,
                    address = address,
                    imagePath = imageUrl
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
