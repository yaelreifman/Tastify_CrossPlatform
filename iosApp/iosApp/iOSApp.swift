import SwiftUI
import FirebaseCore
import FirebaseAuth
import GoogleMaps
import GooglePlaces
import Shared

@main
struct iOSApp: App {
    @StateObject private var reviewsWrapper: ReviewsVMiOS

    init() {
        FirebaseApp.configure()
        Auth.auth().signInAnonymously { result, error in
            if let error = error { print("Auth error:", error.localizedDescription) }
        }

        // אם את משתמשת במפות של Google
        GMSServices.provideAPIKey("YOUR_MAPS_KEY")
        GMSPlacesClient.provideAPIKey("YOUR_PLACES_KEY")

        let vm = ReviewsViewModel(
            repo: FirebaseReviewsRepository(),
            enrichLocation: EnrichReviewLocationUseCase(dataSource: DummyRestaurantLocationDataSource())
        )
        _reviewsWrapper = StateObject(wrappedValue: ReviewsVMiOS(vm: vm))
    }

    var body: some Scene {
        WindowGroup {
            NavigationStack {
                ReviewsScreen()
                    .environmentObject(reviewsWrapper)
            }
        }
    }
}
