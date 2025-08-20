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
        GMSServices.provideAPIKey("AIzaSyC3FvmE6h_7Xea8pZMs46zxH1kcTfLn0lE")
        GMSPlacesClient.provideAPIKey("AIzaSyC3FvmE6h_7Xea8pZMs46zxH1kcTfLn0lE")
        let vm = ReviewsViewModel(repo: FirebaseReviewsRepository())

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
