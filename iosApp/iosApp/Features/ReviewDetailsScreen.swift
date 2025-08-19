import SwiftUI
import CoreLocation
import Shared

struct ReviewDetailsScreen: View {
    let reviewId: String
    @StateObject private var wrapper = ReviewsVMiOS(
        vm: ReviewsViewModel(
            repo: FirebaseReviewsRepository(),
            enrichLocation: EnrichReviewLocationUseCase(
                dataSource: DummyRestaurantLocationDataSource()
            )
        )
    )

    var body: some View {
        Group {
            switch onEnum(of: wrapper.state) {
            case .loading:
                ProgressView("Loading…")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .error(let e):
                Text(e.errorMessage)
                    .foregroundStyle(.red)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .loaded(let payload):
                if let review = payload.reviews.items.first(where: { $0.id == reviewId }) {
                    ReviewDetailsContent(review: review)
                } else {
                    Text("Review not found")
                }
            }
        }
        .navigationTitle("Review")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct ReviewDetailsContent: View {
    let review: Review

    var body: some View {
        ScrollView {
            if let path = review.imagePath, let url = URL(string: path) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let img): img.resizable().scaledToFill()
                    default: Color.gray.opacity(0.15)
                    }
                }
                .frame(height: 220)
                .clipped()
            }

            VStack(alignment: .leading, spacing: 12) {
                Text(review.restaurantName.isEmpty ? "Restaurant" : review.restaurantName)
                    .font(.title)

                let rating = review.rating?.intValue ?? 0
                if rating > 0 {
                    HStack {
                        ForEach(0..<min(rating, 5), id: \.self) { _ in
                            Image(systemName: "star.fill")
                                .foregroundStyle(.yellow)
                        }
                    }
                }

                if let addr = review.address, !addr.isEmpty {
                    Text(addr).foregroundStyle(.secondary)
                }

                if !review.comment.isEmpty {
                    Text(review.comment)
                }
            }
            .padding()

            if let lat = review.latitude?.doubleValue,
               let lng = review.longitude?.doubleValue {
                GoogleMapView(
                    coordinate: CLLocationCoordinate2D(latitude: lat, longitude: lng),
                    title: review.restaurantName.isEmpty ? "Restaurant" : review.restaurantName
                )
                .frame(height: 240)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .padding()
            }
        }
    }
}
