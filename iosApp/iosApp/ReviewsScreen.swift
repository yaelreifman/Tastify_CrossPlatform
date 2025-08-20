import SwiftUI
import Shared

struct ReviewsScreen: View {
    @EnvironmentObject var wrapper: ReviewsVMiOS
    @State private var searchQuery = ""
    @State private var showAddSheet = false

    var body: some View {
        Group {
            switch castReviewsState(wrapper.state) {
            case .loading:
                VStack { ProgressView(); Text("Loading…") }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .error(let e):
                Text(e.errorMessage)
                    .foregroundStyle(.red)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .loaded(let payload):
                let items = payload.reviews.items
                let filtered = filter(items, by: searchQuery)

                List(filtered, id: \.id) { r in
                    NavigationLink {
                        DetailsScreen(review: r)
                    } label: {
                        ReviewRow(review: r)
                    }
                }
                .listStyle(.plain)
                .safeAreaInset(edge: .top) {
                    VStack(spacing: 8) {
                        Button("ADD NEW REVIEW") { showAddSheet = true }
                        .buttonStyle(.borderedProminent)

                        TextField("Search reviews", text: $searchQuery)
                            .textFieldStyle(.roundedBorder)
                    }
                    .padding(.horizontal)
                    .padding(.top, 8)
                    .background(.thinMaterial)
                }
                .sheet(isPresented: $showAddSheet) {
                    AddReviewSheet { newReview in
                        wrapper.addReview(newReview)   // ← כותב דרך ה-shared לפיירבייס
                    }
                    .presentationDetents([.medium, .large])
                }
            }
        }
        .navigationTitle("Reviews")
    }

    private func filter(_ items: [Shared.Review], by q: String) -> [Shared.Review] {
        let needle = q.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard !needle.isEmpty else { return items }
        return items.filter { r in
            r.restaurantName.lowercased().contains(needle) ||
                (r.comment ?? "").lowercased().contains(needle) ||
                (r.address ?? "").lowercased().contains(needle)
        }
    }
}

private struct ReviewRow: View {
    let review: Shared.Review

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(review.restaurantName.isEmpty ? "Restaurant" : review.restaurantName)
                .font(.headline)

            if let path = review.imagePath, !path.isEmpty, let url = URL(string: path) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let img): img.resizable().scaledToFill()
                    default: Color.gray.opacity(0.15)
                    }
                }
                .frame(height: 160)
                .clipped()
                .cornerRadius(8)
            }

            // כוכבים
            HStack(spacing: 2) {
                let stars = Int(review.rating ?? 0)
                ForEach(0..<5, id: \.self) { i in
                    Image(systemName: "star.fill")
                        .foregroundStyle(i < stars ? .yellow : .gray.opacity(0.4))
                        .font(.caption)
                }
            }

            if let c = review.comment, !c.isEmpty {
                Text(c)
            }
            if let addr = review.address, !addr.isEmpty {
                Text("📍 \(addr)").font(.footnote).foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 6)
    }
}
