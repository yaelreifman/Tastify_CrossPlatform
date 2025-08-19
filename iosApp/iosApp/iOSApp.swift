import SwiftUI
import GoogleMaps
import GooglePlaces
import FirebaseCore

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            NavigationStack {
                ReviewsScreen()
            }
        }
    }
}

final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        GMSServices.provideAPIKey("AIzaSyBQZU4_AGMRIvFf-B5Jo2QxVkqt2v8749E")
        GMSPlacesClient.provideAPIKey("AIzaSyBQZU4_AGMRIvFf-B5Jo2QxVkqt2v8749E")
        return true
    }
}
