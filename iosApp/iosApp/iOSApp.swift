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
        // התחברות אנונימית (שלא ייעצרו כללי ה־rules)
        Auth.auth().signInAnonymously { _, _ in }

        // Google Maps/Places – החליפי במפתחות שלך
        GMSServices.provideAPIKey("AIzaSyC3FvmE6h_7Xea8pZMs46zxH1kcTfLn0lE")
        GMSPlacesClient.provideAPIKey("AIzaSyC3FvmE6h_7Xea8pZMs46zxH1kcTfLn0lE")

        // ⬇️ יוצרים את ה־wrapper עם "מפעל" שמחזיר VM חדש בכל refresh()
        _reviewsWrapper = StateObject(
            wrappedValue: ReviewsVMiOS {
                ReviewsViewModel(
                    repo: FirebaseReviewsRepository()
                    // אם בעתיד תוסיפי העשרה/ג׳יאוקודינג: הוסיפי כאן פרמטר enrichLocation=...
                )
            }
        )
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
