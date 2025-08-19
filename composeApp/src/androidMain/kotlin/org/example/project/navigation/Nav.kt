package org.example.project.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.example.project.features.DetailsScreen
import org.example.project.features.ReviewsScreen

/** נתיבים לאפליקציה */
sealed interface Routes {
    data object Home : Routes
    data class Details(val id: String) : Routes
}

/** פונקציית עזר להמרה ל־String */
fun Routes.route(): String = when (this) {
    Routes.Home -> "home"
    is Routes.Details -> "details/${id}"
}

/** הגדרת הניווט */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: Routes = Routes.Home
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination.route(),
        modifier = modifier
    ) {
        // מסך בית
        composable("home") {
            ReviewsScreen(
                onNavigateToDetails = { id ->
                    navController.navigate(Routes.Details(id).route())
                }
            )
        }

        // מסך פרטים
        composable(
            route = "details/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            DetailsScreen(id = id, onBack = { navController.popBackStack() })
        }
    }
}
