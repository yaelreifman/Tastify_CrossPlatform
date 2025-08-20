// DetailsScreen.swift
import SwiftUI
import Shared
import CoreLocation

struct DetailsScreen: View {
    let review: Shared.Review

    @State private var coord: CLLocationCoordinate2D?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {

                // תמונה
                if let path = review.imagePath, !path.isEmpty, let url = URL(string: path) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let image): image.resizable().scaledToFill()
                        default: Color.gray.opacity(0.15)
                        }
                    }
                    .frame(height: 220)
                    .clipped()
                }

                // כותרת
                Text(review.restaurantName.isEmpty ? "Restaurant" : review.restaurantName)
                    .font(.title).bold()

                // דירוג
                let stars = Int(review.rating ?? 0)
                if stars > 0 {
                    HStack(spacing: 2) {
                        ForEach(0..<min(stars, 5), id: \.self) { _ in
                            Image(systemName: "star.fill").foregroundStyle(.yellow)
                        }
                    }
                    .font(.caption)
                }

                // כתובת
                let addrText = review.address ?? ""
                if !addrText.isEmpty {
                    Text("📍 \(addrText)").foregroundStyle(.secondary)
                }

                // תגובה
                let commentText = review.comment ?? ""
                if !commentText.isEmpty {
                    Text(commentText)
                }

                // מפה (אם יש/נמצאה קואורדינטה)
                if let coord {
                    GoogleMapView(coordinate: coord, zoom: 15, title: review.restaurantName)
                        .frame(height: 240)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .padding()
        }
        .navigationTitle("Review")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await resolveCoordinateIfNeeded()
        }
    }

    // אם אין lat/lng אבל יש כתובת—גיאוקודינג דרך ה-shared (KMM)
    @MainActor
    private func resolveCoordinateIfNeeded() async {
        // אם כבר יש קואורדינטות בביקורת—נשתמש בהן
        if let lat = review.latitude?.doubleValue,
           let lng = review.longitude?.doubleValue {
            coord = CLLocationCoordinate2D(latitude: lat, longitude: lng)
            return
        }

        // אחרת—ננסה גיאוקודינג לפי כתובת
        guard let address = review.address, !address.isEmpty else { return }

        do {
            // GeocodingRepository הוא object בקוטלין → ניגשים דרך .shared
            if let pair = try await GeocodingRepository.shared.getCoordinatesFromAddress(address: address) {
                var lat: Double?
                var lng: Double?

                // Pair יכול להגיע כ-NSNumber או Double — נחלץ בבטחה
                if let n = pair.first as? NSNumber { lat = n.doubleValue }
                else if let d = pair.first as? Double { lat = d }

                if let n = pair.second as? NSNumber { lng = n.doubleValue }
                else if let d = pair.second as? Double { lng = d }

                if let lat, let lng {
                    coord = CLLocationCoordinate2D(latitude: lat, longitude: lng)
                }
            }
        } catch {
            // ה־repo כבר מחזיר fallback לת״א; אם נכשל—פשוט לא נציג מפה
            print("Geocoding failed:", error.localizedDescription)
        }
    }
}
