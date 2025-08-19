//
//  RestaurantMapScreen.swift
//  iosApp
//
//  Created by sharon bronshteyn on 19/08/2025.
//

import SwiftUI
import CoreLocation
import Shared
import GooglePlaces

extension RestaurantLocationResolver {
    func resolveViaPlaces(placeID: String) async -> CLLocationCoordinate2D? {
        await withCheckedContinuation { cont in
            GMSPlacesClient.shared().fetchPlace(
                fromPlaceID: placeID,
                placeFields: [.coordinate],
                sessionToken: nil
            ) { place, error in
                cont.resume(returning: place?.coordinate)
            }
        }
    }
}

struct RestaurantMapScreen: View {
    let review: Review
    @State private var coordinate: CLLocationCoordinate2D?

    var body: some View {
        Group {
            if let coord = coordinate {
                GoogleMapView(coordinate: coord, title: "Restaurant \(review.restaurantId)")
                    .edgesIgnoringSafeArea(.bottom)
            } else {
                VStack(spacing: 12) {
                    ProgressView()
                    Text("Loading location…").foregroundStyle(.secondary)
                }
            }
        }
        .navigationTitle("Location")
        .task {
            self.coordinate = await RestaurantLocationResolver.shared.resolveCoordinate(for: review)
        }
    }
}

actor RestaurantLocationResolver {
    static let shared = RestaurantLocationResolver()

    // מזהה פשוט ל-Place ID של גוגל (לרוב מתחיל ב-"ChIJ")
    private func isProbablyPlaceID(_ id: String) -> Bool {
        id.hasPrefix("ChIJ") && id.count > 10
    }

    func resolveCoordinate(for review: Review) async -> CLLocationCoordinate2D? {
        // ✅ אם נראה כמו Place ID – ננסה דרך Google Places
        if isProbablyPlaceID(review.restaurantId) {
            if let coord = await resolveViaPlaces(placeID: review.restaurantId) {
                return coord
            }
        }

        // TODO: החלפה ל-Places/שרת אמיתי
        switch review.restaurantId {
        case "111": return .init(latitude: 32.0853, longitude: 34.7818) // תל אביב
        case "222": return .init(latitude: 31.7683, longitude: 35.2137) // ירושלים
        default:    return .init(latitude: 32.0790, longitude: 34.7806)
        }
    
}
}

