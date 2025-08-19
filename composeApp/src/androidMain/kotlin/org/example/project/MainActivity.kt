package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import org.example.project.features.Reviews.ReviewsViewModel
import org.example.project.navigation.AppNavHost

class MainActivity : ComponentActivity(), OnMapsSdkInitializedCallback {

    private val viewModel: ReviewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // ✅ אתחול של Google Maps SDK
        MapsInitializer.initialize(applicationContext, Renderer.LATEST, this)

        setContent {
            MaterialTheme {
                AppNavHost()
            }
        }
    }

    // ✅ Callback שיוודא שאכן ה־Maps SDK אותחל
    override fun onMapsSdkInitialized(renderer: Renderer) {
        println("✅ Google Maps SDK initialized with renderer: $renderer")
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
