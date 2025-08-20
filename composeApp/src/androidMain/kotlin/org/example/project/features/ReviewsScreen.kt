package org.example.project.features

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import org.example.project.data.StorageRepository
import org.example.project.features.Reviews.ReviewsState
import org.example.project.features.Reviews.ReviewsViewModel
import org.example.project.model.Review
import org.example.project.model.Reviews
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/* ─────────────────────────  טופ מסך  ───────────────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    viewModel: ReviewsViewModel = ReviewsViewModel(),
    onNavigateToDetails: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    // כשמגיע Loaded – נסיר את overlay של הטעינה/רענון
    LaunchedEffect(uiState) {
        if (uiState is ReviewsState.Loaded) isRefreshing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tastify",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            Crossfade(uiState, label = "reviews_state") { state ->
                when (state) {
                    ReviewsState.Loading -> {
                        // התוכן האמיתי ריק; הסקליטון מוצג כ־overlay למטה
                        Box(Modifier.fillMaxSize())
                    }
                    is ReviewsState.Error -> ErrorContent(message = state.errorMessage)
                    is ReviewsState.Loaded -> ReviewsContent(
                        reviews = state.reviews,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onClickItem = { onNavigateToDetails(it) }
                    )
                }
            }

            // Overlay של סקליטון בזמן טעינה ראשונית או ריענון אחרי Save
            if (uiState is ReviewsState.Loading || isRefreshing) {
                LoadingOverlay()
            }
        }
    }

    if (showDialog) {
        AddReviewDialog(
            onDismiss = { showDialog = false },
            onSave = { newReview ->
                // אחרי שמירה – נציג overlay עד שה־Flow ישלח Loaded מעודכן
                isRefreshing = true
                val nowIso = nowIsoUtc() // "2025-08-20T11:31:57.873Z"
                viewModel.addReview(newReview.copy(createdAt = nowIso))
                showDialog = false
            }
        )
    }
}

/* ─────────────────────────  תוכן המסך  ───────────────────────── */

@Composable
private fun ReviewsContent(
    reviews: Reviews,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClickItem: (String) -> Unit
) {
    val items = remember(reviews.items) {
        // מיון חדש → ישן לפי createdAt (ISO-8601 או millis fallback)
        reviews.items.sortedByDescending { it.createdAtSortKey() }
    }

    val filtered = remember(searchQuery, items) {
        val q = searchQuery.trim()
        if (q.isEmpty()) items
        else items.filter { r ->
            (r.restaurantName?.contains(q, ignoreCase = true) == true) ||
                    (r.comment?.contains(q, ignoreCase = true) == true) ||
                    (r.address?.contains(q, ignoreCase = true) == true)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // שדה חיפוש מעוגל
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            placeholder = { Text("Search reviews") },
            leadingIcon = {
                // אייקון “חיפוש” מינימלי
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(2.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(contentPadding = PaddingValues(bottom = 12.dp)) {
            items(filtered, key = { it.id }) { review ->
                ReviewCard(
                    review = review,
                    onClick = { onClickItem(review.id) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/* ─────────────────────────  כרטיס ביקורת  ───────────────────────── */

@Composable
private fun ReviewCard(
    review: Review,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            // תמונת קאבר עם שימר + תג דירוג
            if (!review.imagePath.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = review.imagePath,
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
            }
            }

            // טקסטים – שם מסעדה + תאריך, כתובת, תגובה
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = review.restaurantName?.takeIf { it.isNotBlank() } ?: "Restaurant",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val dateText = review.createdAtFormatted()
                    if (dateText.isNotEmpty()) {
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                review.address?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                review.comment?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }


@Composable
private fun RatingPill(rating: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107))
            Spacer(Modifier.width(4.dp))
            Text("$rating", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* ─────────────────────────  דיאלוג הוספה (גלריה + מצלמה)  ───────────────────────── */

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

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // גלריה
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    // מצלמה (תמונה חדשה) – שומר קובץ לקאש ואז מצביע אליו
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val file = File(ctx.cacheDir, "captured_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            selectedImageUri = Uri.fromFile(file)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Review") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = restaurantName,
                    onValueChange = { restaurantName = it },
                    label = { Text("Restaurant Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // דירוג – בדיוק כמו ב-iOS (כפתורי כוכבים)
                Row {
                    repeat(5) { i ->
                        IconToggleButton(
                            checked = i < rating,
                            onCheckedChange = { rating = i + 1 }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (i < rating) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
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

                // בחירת תמונה: גלריה + מצלמה
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) { Text("Choose from Photos") }

                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Take Photo") }
                }

                // תצוגה מקדימה
                selectedImageUri?.let { uri ->
                    Spacer(Modifier.height(6.dp))
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                if (uploading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !uploading && restaurantName.isNotBlank(),
                onClick = {
                    val id = System.currentTimeMillis().toString()
                    val nowIso = nowIsoUtc() // כמו ב-iOS

                    scope.launch {
                        uploading = true
                        // העלאה ל-Firebase Storage אם יש תמונה
                        val imageUrl = selectedImageUri?.let { StorageRepository.uploadImage(it, id) }
                        uploading = false

                        onSave(
                            Review(
                                id = id,
                                restaurantName = restaurantName.trim(),
                                rating = rating,
                                comment = comment,
                                address = address,
                                imagePath = imageUrl,
                                // אם יש לך שדות נוספים כמו lat/lng – השארי null
                                createdAt = nowIso // מחרוזת ISO-8601 UTC, תואם iOS
                            )
                        )
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/* ─────────────────────────  שגיאה / טעינה  ───────────────────────── */

@Composable
fun ErrorContent(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(Modifier.fillMaxWidth()) {
            // שדה חיפוש מדומה
            Box(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(44.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x22AAAAAA))
                    .shimmer()
            )
            // כרטיסים מדומים
            repeat(6) {
                Box(
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x15AAAAAA))
                        .shimmer()
                )
            }
        }
    }
}

/* ─────────────────────────  Utils: זמן/מיון + שימר  ───────────────────────── */
private fun nowIsoUtc(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date())
}
private fun parseIsoToMillis(s: String): Long? {
    val tz = TimeZone.getTimeZone("UTC")
    val patterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    for (p in patterns) {
        try {
            val sdf = SimpleDateFormat(p, Locale.US)
            sdf.timeZone = tz
            return sdf.parse(s)?.time
        } catch (_: Exception) { }
    }
    return null
}

private fun Review.createdAtSortKey(): Long {
    val s = this.createdAt ?: return Long.MIN_VALUE
    // קודם ננסה ISO-8601, אחרת millis כמחרוזת
    return parseIsoToMillis(s) ?: s.toLongOrNull() ?: Long.MIN_VALUE
}

private fun Review.createdAtFormatted(): String {
    val epoch = createdAtSortKey()
    if (epoch == Long.MIN_VALUE) return ""
    val df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
    return df.format(Date(epoch))
}

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
        start = androidx.compose.ui.geometry.Offset(x - 200f, x - 200f),
        end = androidx.compose.ui.geometry.Offset(x, x)
    )
    this.background(brush = brush, shape = RoundedCornerShape(12.dp))
}
