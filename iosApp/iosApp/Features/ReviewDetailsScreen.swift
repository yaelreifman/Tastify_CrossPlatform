import SwiftUI
import CoreLocation
import Shared

struct ReviewDetailsScreen: View {
    let reviewId: String
    @StateObject private var wrapper = ReviewsVMiOS(  vm: ReviewsViewModel(
        repo: FirebaseReviewsRepository(),
        enrichLocation: EnrichReviewLocationUseCase(
            dataSource: DummyRestaurantLocationDataSource()
        )))

    var body: some View {
        Group {
            switch onEnum(of: wrapper.state) {
            case .loading:
                VStack { ProgressView(); Text("Loading…") }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .error(let e):
                Text(e.errorMessage)
                    .foregroundStyle(.red)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .loaded(let payload):
                if let review = payload.reviews.items.first(where: { $0.id == reviewId }) {
                    ReviewDetailsContent(review: review)
                } else {
                    Text("Review not found").frame(maxWidth: .infinity, maxHeight: .infinity)
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
            // תמונה
            if let path = review.imagePath, !path.isEmpty, let url = URL(string: path) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let img): img.resizable().scaledToFill()
                    default: Color.gray.opacity(0.15)
                    }
                }
                .frame(height: 220)
                .clipped()
            }

            // פרטים
            VStack(alignment: .leading, spacing: Dimens.md) {
                Text(review.restaurantName.isEmpty ? "Restaurant" : review.restaurantName)
                    .font(Fonts.title)

                let rating = Int(review.rating ?? 0)
                if rating > 0 {
                    HStack(spacing: 2) {
                        ForEach(0..<min(rating, Stars.max), id: \.self) { _ in
                            Image(systemName: "star.fill").foregroundStyle(AppColors.star)
                        }
                    }
                    .font(.caption)
                }

                if let addr = review.address, !addr.isEmpty {
                    Text(addr).font(Fonts.body).foregroundStyle(AppColors.onSurfaceVariant)
                }

                if !review.comment.isEmpty {
                    Text(review.comment).font(Fonts.body)
                }
            }
            .padding(.horizontal, Dimens.lg)
            .padding(.top, Dimens.md)

            // מפה (אם יש קואורדינטות; אם אין—דולג)
            if let lat = review.latitude?.doubleValue, let lng = review.longitude?.doubleValue {
                GoogleMapView(
                    coordinate: CLLocationCoordinate2D(latitude: lat, longitude: lng),
                    title: review.restaurantName.isEmpty ? "Restaurant" : review.restaurantName
                )
                .frame(height: 240)
                .clipShape(RoundedRectangle(cornerRadius: Dimens.radiusLg))
                .padding(.horizontal, Dimens.lg)
                .padding(.vertical, Dimens.md)
            }
        }
    }
}
//
//  ReviewDetailsScreen.swift
//  iosApp
//
//  Created by sharon bronshteyn on 19/08/2025.
//

