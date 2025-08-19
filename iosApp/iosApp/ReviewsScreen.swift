import SwiftUI
import Shared

struct ReviewsScreen: View {
    @StateObject private var wrapper = ReviewsVMiOS(
        vm: ReviewsViewModel(
            repo: FirebaseReviewsRepository(),
            enrichLocation: EnrichReviewLocationUseCase(
                dataSource: DummyRestaurantLocationDataSource()
            )
        )
    )

    @State private var showAddReview = false
    @State private var searchQuery = ""

    var body: some View {
        NavigationStack {
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
                    VStack {
                        Button("ADD NEW REVIEW") {
                            showAddReview = true
                        }
                        .buttonStyle(.borderedProminent)
                        .padding()

                        TextField("Search reviews", text: $searchQuery)
                            .textFieldStyle(.roundedBorder)
                            .padding(.horizontal)

                        List {
                            ForEach(payload.reviews.items.filter { review in
                                searchQuery.isEmpty
                                || review.restaurantName.localizedCaseInsensitiveContains(searchQuery)
                                || review.comment.localizedCaseInsensitiveContains(searchQuery)
                                || review.address.localizedCaseInsensitiveContains(searchQuery)
                            }, id: \.id) { review in
                                NavigationLink {
                                    ReviewDetailsScreen(reviewId: review.id)
                                } label: {
                                    ReviewRow(review: review)
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Reviews")
            .sheet(isPresented: $showAddReview) {
                AddReviewSheet { newReview in
                    wrapper.addReview(newReview)
                }
            }
        }
    }
}

private struct ReviewRow: View {
    let review: Review

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(review.restaurantName.isEmpty ? "Restaurant" : review.restaurantName)
                .font(.headline)

            if let path = review.imagePath, let url = URL(string: path) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let img): img.resizable().scaledToFill()
                    default: Color.gray.opacity(0.2)
                    }
                }
                .frame(height: 120)
                .clipped()
            }

            HStack {
                ForEach(0..<5, id: \.self) { idx in
                    Image(systemName: "star.fill")
                        .foregroundStyle(idx < (review.rating?.intValue ?? 0) ? .yellow : .gray)
                }
            }

            if !review.comment.isEmpty {
                Text(review.comment)
            }

            if let addr = review.address, !addr.isEmpty {
                Text("📍 \(addr)").font(.caption).foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 8)
    }
}
