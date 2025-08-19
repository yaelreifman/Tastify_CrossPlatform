package org.example.project.features
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import org.example.project.features.Reviews.ReviewsState
import org.example.project.features.Reviews.ReviewsViewModel
import org.example.project.model.Review
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    id: String,
    onBack: () -> Unit,
    viewModel: ReviewsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // חפשי את הביקורת לפי ה-id מהסטייט הטעון
    val review: Review? = when (val s = uiState) {
        is ReviewsState.Loaded -> s.reviews.items.firstOrNull { it.id == id }
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Details", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            when (val s = uiState) {
                ReviewsState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ReviewsState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = s.errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                }
                is ReviewsState.Loaded -> {
                    if (review == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Review not found")
                        }
                    } else {
                        ReviewDetailsContent(review = review)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewDetailsContent(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // תמונה עליונה
        if (!review.imagePath.isNullOrBlank()) {
            AsyncImage(
                model = review.imagePath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
        }

        // פרטים
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // שם המסעדה
            Text(
                text = review.restaurantName.orEmpty().ifBlank { "Restaurant" },
                style = MaterialTheme.typography.titleLarge
            )

            // דירוג
            val rating = review.rating ?: 0
            if (rating > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(min(rating, 5)) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107)
                        )
                    }
                }
            }

            // כתובת
            review.address?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "📍 $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // תגובה / טקסט
            review.comment?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // מפה (אם יש קואורדינטות)
        val lat = review.latitude
        val lng = review.longitude
        if (lat != null && lng != null) {
            val pos = LatLng(lat, lng)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(pos, 15f)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(240.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = pos),
                        title = review.restaurantName.orEmpty().ifBlank { "Restaurant" }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}