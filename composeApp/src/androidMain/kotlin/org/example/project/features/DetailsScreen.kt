package org.example.project.features

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import org.example.project.data.GeocodingRepository
import org.example.project.features.Reviews.ReviewsState
import org.example.project.features.Reviews.ReviewsViewModel
import org.example.project.model.Review
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.min
import androidx.compose.animation.core.*
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.TileMode
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent

/* ─────────────────────────  צבעים  ───────────────────────── */

private val Blue      = Color(0xFF1E88E5) // Primary Blue
private val BlueLight = Color(0xFFBBDEFB) // Light Blue (לרקעים/שימר עדין)

/* ─────────────────────────  מסך פרטי ביקורת  ───────────────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    id: String,
    onBack: () -> Unit,
    viewModel: ReviewsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val review: Review? = when (val s = uiState) {
        is ReviewsState.Loaded -> s.reviews.items.firstOrNull { it.id == id }
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Review",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            when (uiState) {
                ReviewsState.Loading -> {
                    // אפשר לשים כאן סקליטון אם תרצי
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Blue)
                    }
                }
                is ReviewsState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = (uiState as ReviewsState.Error).errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
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

/* ─────────────────────────  תוכן הפרטים (iOS-like)  ───────────────────────── */

@Composable
private fun ReviewDetailsContent(review: Review) {
    var coordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val scope = rememberCoroutineScope()

    // אם אין קואורדינטות – נביא מג׳יאוקודינג לפי הכתובת
    LaunchedEffect(review.address) {
        if (review.latitude == null && review.longitude == null && !review.address.isNullOrBlank()) {
            scope.launch {
                val result = GeocodingRepository.getCoordinatesFromAddress(review.address!!)
                result?.let { coordinates = it }
            }
        } else if (review.latitude != null && review.longitude != null) {
            coordinates = Pair(review.latitude!!, review.longitude!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        /* Header Image עם שימר ותג דירוג */
        HeaderImage(
            imageUrl = review.imagePath,
            rating = review.rating ?: 0
        )

        /* Title + Date + Address + Comment */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.restaurantName?.takeIf { it.isNotBlank() } ?: "Restaurant",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val created = review.createdAtFormatted()
                if (created.isNotEmpty()) {
                    Text(
                        text = created,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val rating = review.rating ?: 0
            if (rating > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(min(rating, 5)) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Blue
                        )
                    }
                }
            }

            review.address?.takeIf { it.isNotBlank() }?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            review.comment?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge)
            }
        }

        /* מפה (GoogleMap) בפינה מעוגלת */
        val lat = coordinates?.first ?: review.latitude
        val lng = coordinates?.second ?: review.longitude
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
                shape = RoundedCornerShape(16.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = pos),
                        title = review.restaurantName?.takeIf { it.isNotBlank() } ?: "Restaurant"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/* ─────────────────────────  Header Image (שימר + תג דירוג)  ───────────────────────── */

@Composable
private fun HeaderImage(
    imageUrl: String?,
    rating: Int
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(18.dp))
    ) {
        if (!imageUrl.isNullOrBlank() && imageUrl.startsWith("http")) {
            // יש URL חוקי → נטען עם Coil (כולל לוג שגיאות אם צריך)

            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        // שלד טעינה (שימר). אם אין לך shimmer(), אפשר לשים ProgressView במקום.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x1F000000)) // בסיס בהיר
                                .shimmer(),                    // <- הסירי/החליפי אם אין את המודיפייר הזה
                            contentAlignment = Alignment.Center
                        ) {
                            // אם אין shimmer(): שימי כאן
                            // CircularProgressIndicator()
                        }
                    }
                    is AsyncImagePainter.State.Success -> {
                        SubcomposeAsyncImageContent()

                        // גרדיאנט עדין מלמטה (לא חובה)
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.25f)
                                        )
                                    )
                                )
                        )
                    }
                    is AsyncImagePainter.State.Error -> {
                        // פלייסהולדר אם טעינה נכשלה
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x15000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No image", color = Color.Gray)
                        }
                    }
                    else -> {
                        // מצב ברירת מחדל/ריק – אפשר להשאיר ריק
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        } else {
            // פלייסהולדר כשאין תמונה או כשזה gs://
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x15000000)),
                contentAlignment = Alignment.Center
            ) {
                Text("No image", color = Color.Gray)
            }
        }

        if (rating > 0) {
            RatingPillBlue(
                rating = rating,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )
        }
    }
}

/* ─────────────────────────  תג דירוג בכחול  ───────────────────────── */

@Composable
private fun RatingPillBlue(rating: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = BlueLight.copy(alpha = 0.35f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Blue)
            Spacer(Modifier.width(4.dp))
            Text(
                "$rating",
                style = MaterialTheme.typography.labelSmall,
                color = Blue
            )
        }
    }
}

/* ─────────────────────────  Utils: תאריך + שימר  ───────────────────────── */

/** ממיר createdAt (ISO-8601 עם/בלי חלקי שניות או millis כמחרוזת) ל־Date */
private fun Review.createdAtDate(): java.util.Date? {
    val s = this.createdAt ?: return null

    // 1) אם נשמר כמספר (millis / seconds)
    s.toLongOrNull()?.let {
        // אם זה שניות (10 ספרות) נהפוך למילישניות
        val millis = if (it < 1_000_000_000_000L) it * 1000L else it
        return java.util.Date(millis)
    }

    // 2) ISO8601 UTC עם/בלי שבריות שניות
    val tzUTC = TimeZone.getTimeZone("UTC")
    val patterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    for (p in patterns) {
        try {
            val sdf = SimpleDateFormat(p, Locale.US).apply { timeZone = tzUTC }
            return sdf.parse(s)
        } catch (_: Exception) { /* continue */ }
    }
    return null
}

/** מפתח מיון חדש→ישן */
private fun Review.createdAtSortKey(): Long {
    val d = createdAtDate() ?: return Long.MIN_VALUE
    return d.time
}

/** פורמט תאריך להצגה לוקלית (כמו iOS style: .date) */
private fun Review.createdAtFormatted(): String {
    val d = createdAtDate() ?: return ""
    val df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
    return df.format(d)
}

/** אפקט שימר פשוט (ללא תלות בגרסאות) */
private fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer-x"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.35f),
            Color.Transparent
        ),
        start = androidx.compose.ui.geometry.Offset(x - 220f, x - 220f),
        end = androidx.compose.ui.geometry.Offset(x, x),
        tileMode = TileMode.Clamp
    )
    this.background(brush, RoundedCornerShape(18.dp))
}
