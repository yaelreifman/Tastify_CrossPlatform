import SwiftUI
import Shared
import CoreLocation

struct DetailsScreen: View {
    let review: Shared.Review

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {

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

                Text(review.restaurantName.isEmpty ? "Restaurant" : review.restaurantName)
                    .font(.title).bold()

                let stars = Int(review.rating ?? 0)
                if stars > 0 {
                    HStack(spacing: 2) {
                        ForEach(0..<min(stars, 5), id: \.self) { _ in
                            Image(systemName: "star.fill").foregroundStyle(.yellow)
                        }
                    }.font(.caption)
                }

                if let addr = review.address, !addr.isEmpty {
                    Text("📍 \(addr)").foregroundStyle(.secondary)
                }
                if let comment = review.comment, !comment.isEmpty {
                    Text(comment)
                }

                if let lat = review.latitude, let lng = review.longitude {
                    GoogleMapView(
                        coordinate: CLLocationCoordinate2D(latitude: lat, longitude: lng),
                        zoom: 15,
                        title: review.restaurantName
                    )
                        .frame(height: 240)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .padding()
        }
        .navigationTitle("Review")
        .navigationBarTitleDisplayMode(.inline)
    }
}
