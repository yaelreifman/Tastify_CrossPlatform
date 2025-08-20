// iOSApp.swift
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
        // Firebase
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }
        Auth.auth().signInAnonymously { _, _ in }

        // Google Maps/Places — החליפי במפתחות שלך
        GMSServices.provideAPIKey("YOUR_GOOGLE_MAPS_KEY")
        GMSPlacesClient.provideAPIKey("YOUR_GOOGLE_PLACES_KEY")
        let vm = ReviewsViewModel(
            repo: FirebaseReviewsRepository(),
            enrichLocation: EnrichReviewLocationUseCase(dataSource: GeocodingLocationDataSource() )
        )
        // --- Build shared dependencies cleanly ---
        let repo = FirebaseReviewsRepository()
        let ds = DummyRestaurantLocationDataSource()                     // class → יש init()
        let enrich = EnrichReviewLocationUseCase(dataSource: ds)         // label נכון: dataSource

        // ⭐️ עיקר התיקון: label נכון ל־initializer של ה־VM
       

        // אם Xcode עדיין טוען "Extra argument 'enrichLocation' in call",
        // נסי במקום השורה למעלה את אחת משתי האופציות:
        // let vm = ReviewsViewModel(repo: repo)                  // אם יש init עם ברירת־מחדל לשני
        // let vm = ReviewsViewModel(repo: repo, enrichLocation_: enrich) // לעתים SKIE מוסיף "_"

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
