import SwiftUI
import Shared

struct ReviewsScreen: View {
    @EnvironmentObject var wrapper: ReviewsVMiOS
    @State private var searchQuery = ""
    @State private var showAddSheet = false

    var body: some View {
        content()
            .navigationTitle("Reviews")
    }

    // מחזיר View לפי מצב ה־state בלי לבלבל את ה-ViewBuilder
    @ViewBuilder
        private func content() -> some View {
            switch onEnum(of: wrapper.state) {
            case .loading:
                VStack { ProgressView(); Text("Loading…") }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .error(let e):
                Text(e.errorMessage)
                    .foregroundStyle(.red)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .loaded(let l):
                ReviewsListContent(
                    items: l.reviews.items,
                    searchQuery: $searchQuery,
                    showAddSheet: $showAddSheet
                )
            }
        }
    }


private struct ReviewsListContent: View {
    let items: [Shared.Review]
    @Binding var searchQuery: String
    @Binding var showAddSheet: Bool
    @EnvironmentObject var wrapper: ReviewsVMiOS

    private var filtered: [Shared.Review] {
        let q = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard !q.isEmpty else { return items }
        return items.filter { r in
            r.restaurantName.lowercased().contains(q)
            || (r.comment ?? "").lowercased().contains(q)
            || (r.address ?? "").lowercased().contains(q)
        }
    }

    var body: some View {
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
                wrapper.addReview(newReview)       // כתיבה לפיירבייס דרך ה-shared
            }
            .presentationDetents([.medium, .large])
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

            let stars = Int(review.rating ?? 0)
            HStack(spacing: 2) {
                ForEach(0..<5, id: \.self) { i in
                    Image(systemName: "star.fill")
                        .foregroundStyle(i < stars ? .yellow : .gray.opacity(0.4))
                        .font(.caption)
                }
            }

            if !review.comment.isEmpty {
                Text(review.comment)
            }
            if let addr = review.address, !addr.isEmpty {
                Text("📍 \(addr)").font(.footnote).foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 6)
    }
}
