package org.example.project.features

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import org.example.project.data.StorageRepository
import org.example.project.features.Reviews.ReviewsState
import org.example.project.features.Reviews.ReviewsViewModel
import org.example.project.model.Review
import org.example.project.model.Reviews
import java.io.File
import java.io.FileOutputStream

@Composable
fun ReviewsScreen(
    viewModel: ReviewsViewModel = ReviewsViewModel(),
    onNavigateToDetails: (String) -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsState().value
    when (uiState) {
        is ReviewsState.Error -> ErrorContent(message = uiState.errorMessage)
        is ReviewsState.Loaded -> ReviewsContent(
            reviews = uiState.reviews,
            viewModel = viewModel,
            onNavigateToDetails = onNavigateToDetails
        )
        ReviewsState.Loading -> LoadingContent()
    }
}

@Composable
fun ReviewsContent(
    reviews: Reviews,
    viewModel: ReviewsViewModel,
    onNavigateToDetails: (String) -> Unit,
    lazyListState: LazyListState = rememberLazyListState()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val filteredReviews = remember(searchQuery, reviews.items) {
        if (searchQuery.isBlank()) {
            reviews.items
        } else {
            reviews.items.filter { review ->
                (review.restaurantName?.contains(searchQuery, ignoreCase = true) == true) ||
                        (review.comment?.contains(searchQuery, ignoreCase = true) == true) ||
                        (review.address?.contains(searchQuery, ignoreCase = true) == true)
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
            items(filteredReviews, key = { it.id }) { review ->
                ReviewRowCard(
                    review = review,
                    onClick = { onNavigateToDetails(review.id) }
                )
            }
        }
    }

    if (showDialog) {
        AddReviewDialog(
            onDismiss = { showDialog = false },
            onSave = { newReview ->
                viewModel.addReview(newReview)
                showDialog = false
            }
        )
    }
}

@Composable
private fun ReviewRowCard(
    review: Review,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = review.restaurantName ?: "",
                style = MaterialTheme.typography.titleMedium
            )

            if (!review.imagePath.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(review.imagePath),
                    contentDescription = "Review image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }

            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (index < (review.rating ?: 0)) Color(0xFFFFC107) else Color.Gray
                    )
                }
            }

            if (!review.comment.isNullOrBlank()) {
                Text(text = review.comment!!)
            }
            if (!review.address.isNullOrBlank()) {
                Text(text = "📍 ${review.address}", style = MaterialTheme.typography.bodySmall)
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
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // בוחר תמונה מהגלריה
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    // מצלמה (תמונה חדשה)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val file = File(context.cacheDir, "captured_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            selectedImageUri = Uri.fromFile(file)
        }
    }

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

                Row {
                    repeat(5) { index ->
                        IconToggleButton(
                            checked = index < rating,
                            onCheckedChange = { rating = index + 1 }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (index < rating) Color(0xFFFFC107) else Color.Gray
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

                // שני כפתורים: גלריה + מצלמה
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) { Text("Choose from Gallery") }

                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Take Photo") }
                }

                selectedImageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                if (uploading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val id = System.currentTimeMillis().toString()
                scope.launch {
                    uploading = true
                    var imageUrl: String? = null
                    selectedImageUri?.let { uri ->
                        imageUrl = StorageRepository.uploadImage(uri, id)
                    }
                    uploading = false

                    val review = Review(
                        id = id,
                        restaurantName = restaurantName,
                        rating = rating,
                        comment = comment,
                        address = address,
                        imagePath = imageUrl
                    )
                    onSave(review)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ErrorContent(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = TextStyle(fontSize = 28.sp, textAlign = TextAlign.Center)
        )
    }
}

@Composable
fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            trackColor = MaterialTheme.colorScheme.secondary
        )
    }
}
