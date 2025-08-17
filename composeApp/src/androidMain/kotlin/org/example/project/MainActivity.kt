package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.features.Reviews.ReviewsViewModel
import org.example.project.features.ReviewsScreen

class MainActivity : ComponentActivity() {
    private val viewModel: ReviewsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme{
                ReviewsScreen(viewModel)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}