import SwiftUI
import Shared

// MARK: - המרה של items מה-shared למערך Swift (KotlinArray -> [Shared.Review])
private func toSwiftReviews(_ raw: Any?) -> [Shared.Review] {
    if let arr = raw as? [Shared.Review] { return arr }
    if let karr = raw as? KotlinArray<Shared.Review> {
        return (0..<Int(karr.size)).compactMap { karr.get(index: Int32($0)) }
    }
    if let karr = raw as? KotlinArray<AnyObject> {
        return (0..<Int(karr.size)).compactMap { karr.get(index: Int32($0)) as? Shared.Review }
    }
    return []
}

// MARK: - זיהוי סטייטים (SKIE)
private func isLoadedState(_ s: ReviewsState) -> Bool {
    switch onEnum(of: s) { case .loaded: return true;
    default: return false }
}
private func isLoadingState(_ s: ReviewsState) -> Bool {
    switch onEnum(of: s) { case .loading: return true;
    default: return false }
}

// MARK: - מסך ראשי
struct ReviewsScreen: View {
    @EnvironmentObject var wrapper: ReviewsVMiOS
    @State private var searchQuery = ""
    @State private var showAddSheet = false
    @State private var isRefreshing = false   // טעינה עד שתגיע תשובה עדכנית

    var body: some View {
        let loadedFlag = isLoadedState(wrapper.state)
        let loadingFlag = isLoadingState(wrapper.state)

        ZStack {
            content()
            .navigationTitle("Reviews")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button { showAddSheet = true } label: {
                        Label("Add", systemImage: "plus")
                    }
                }
            }
            .sheet(isPresented: $showAddSheet) {
                AddReviewSheet { newReview in
                    wrapper.addReview(newReview) // כתיבה ל-Firestore דרך shared
                    isRefreshing = true          // מציגים שלד עד ה-Loaded הבא
                    wrapper.refresh()            // לבקש רענון אקטיבי ב-VM
                }
                .presentationDetents([.medium, .large])
                .onChange(of: wrapper.state) { s in
                    if case .loaded = onEnum(of: s) {
                        withAnimation(.easeInOut(duration: 0.2)) { isRefreshing = false }
                    }
                }
            }

            // Overlay טעינה (שימר) אם טוען או מרענן
            if loadingFlag || isRefreshing {
                Color.clear
                LoadingListSkeleton(count: 6)
                    .padding(.horizontal)
                    .transition(.opacity)
            }
        }
            // אם עברנו ל-Loaded מכל סיבה — נסתיר את ה-overlay
        .onChange(of: loadedFlag) { isNowLoaded in
            if isNowLoaded {
                withAnimation(.easeInOut(duration: 0.2)) { isRefreshing = false }
            }
        }
    }

    // תוכן מתחת ל-overlay לפי מצב ה-state
    @ViewBuilder
    private func content() -> some View {
        switch onEnum(of: wrapper.state) {
        case .loading:
            Color.clear.ignoresSafeArea() // השלד כבר באוברליי
        case .error(let e):
            VStack(spacing: 12) {
                Image(systemName: "exclamationmark.triangle.fill").font(.system(size: 36))
                Text(e.errorMessage).multilineTextAlignment(.center)
                Button("Try again") { wrapper.refresh() }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        case .loaded(let payload):
            let items = toSwiftReviews(payload.reviews.items)
            ReviewsListContent(items: items, searchQuery: $searchQuery)
                .animation(.spring(duration: 0.35), value: items.count)
        }
    }
}

// MARK: - רשימה + חיפוש + מיון חדש→ישן
private struct ReviewsListContent: View {
    let items: [Shared.Review]
    @Binding var searchQuery: String

    private var filteredAndSorted: [Shared.Review] {
        let trimmed = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines)
        let filtered: [Shared.Review]
        if trimmed.isEmpty {
            filtered = items
        } else {
            let q = trimmed.lowercased()
            filtered = items.filter { r in
                r.restaurantName.lowercased().contains(q)
                    || r.comment.lowercased().contains(q)
                    || r.addressSafe.lowercased().contains(q)
            }
        }
        // חדש → ישן לפי createdAt
        return filtered.sorted { $0.createdAtDateSafe > $1.createdAtDateSafe }

    }

    var body: some View {
        VStack(spacing: 12) {
            // חיפוש
            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                TextField("Search reviews", text: $searchQuery)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .strokeBorder(.gray.opacity(0.2))
            )
            .padding(.horizontal)

            // רשימה
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(filteredAndSorted, id: \.id) { review in
                        NavigationLink { DetailsScreen(review: review) } label: {
                            ReviewCard(review: review)
                        }
                        .buttonStyle(.plain)
                        .padding(.horizontal)
                        .transition(.move(edge: .top).combined(with: .opacity))
                    }
                    .padding(.bottom, 8)
                }
            }
        }
    }
}

// MARK: - כרטיס ביקורת (עם שימר לתמונה בזמן טעינה)
private struct ReviewCard: View {
    let review: Shared.Review

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            // תמונה
            if let path = review.imagePath, !path.isEmpty, let url = URL(string: path) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color.gray.opacity(0.12))
                            .frame(height: 180)
                            .shimmer()
                    case .success(let img):
                        img.resizable().scaledToFill()
                            .frame(height: 180)
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                            .overlay(alignment: .topLeading) {
                                if review.ratingInt > 0 {
                                    RatingPill(rating: review.ratingInt).padding(10)
                                }
                            }
                    case .failure:
                        ZStack {
                            RoundedRectangle(cornerRadius: 16)
                                .fill(Color.gray.opacity(0.12))
                            Image(systemName: "photo")
                                .font(.title2)
                                .foregroundStyle(.gray.opacity(0.6))
                        }
                        .frame(height: 180)
                    @unknown default:
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color.gray.opacity(0.12))
                            .frame(height: 180)
                    }
                }
            } else if review.ratingInt > 0 {
                RatingPill(rating: review.ratingInt).padding(.leading, 6)
            }

            // טקסטים
            VStack(alignment: .leading, spacing: 6) {
                HStack(alignment: .firstTextBaseline) {
                    Text(review.restaurantName.isEmpty ? "Restaurant" : review.restaurantName)
                        .font(.headline)
                        .lineLimit(1)
                    Spacer(minLength: 12)
                    Text(review.createdAtDateSafe, style: .date)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                if !review.addressSafe.isEmpty {
                    HStack(spacing: 6) {
                        Image(systemName: "mappin.and.ellipse")
                        Text(review.addressSafe).lineLimit(1)
                    }
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                }

                if !review.comment.isEmpty {
                    Text(review.comment)
                        .font(.subheadline)
                        .foregroundStyle(.primary)
                        .lineLimit(3)
                }
            }
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 16).fill(Color(.secondarySystemBackground))
        )
        .overlay(
            RoundedRectangle(cornerRadius: 16).stroke(.black.opacity(0.05), lineWidth: 0.5)
        )
        .shadow(color: .black.opacity(0.05), radius: 8, x: 0, y: 4)
    }
}

private struct RatingPill: View {
    let rating: Int
    var body: some View {
        HStack(spacing: 4) { Image(systemName: "star.fill"); Text("\(rating)").fontWeight(.semibold) }
        .font(.caption)
        .padding(.horizontal, 8)
        .padding(.vertical, 6)
        .background(.ultraThinMaterial, in: Capsule())
    }
}

// MARK: - Skeleton + Shimmer
private struct LoadingListSkeleton: View {
    let count: Int
    var body: some View {
        VStack(spacing: 12) {
            RoundedRectangle(cornerRadius: 12).fill(.gray.opacity(0.15)).frame(height: 44).shimmer()
            ForEach(0..<count, id: \.self) { _ in ReviewCardSkeleton().shimmer() }
        }
    }
}

private struct ReviewCardSkeleton: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            RoundedRectangle(cornerRadius: 16).fill(.gray.opacity(0.18)).frame(height: 180)
            RoundedRectangle(cornerRadius: 6).fill(.gray.opacity(0.18)).frame(height: 18)
            RoundedRectangle(cornerRadius: 6).fill(.gray.opacity(0.14)).frame(height: 14)
            RoundedRectangle(cornerRadius: 6).fill(.gray.opacity(0.12)).frame(height: 14).opacity(0.8)
        }
        .padding(12)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color(.secondarySystemBackground)))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(.black.opacity(0.05), lineWidth: 0.5))
        .shadow(color: .black.opacity(0.05), radius: 8, x: 0, y: 4)
        .padding(.horizontal)
    }
}

private struct ShimmerModifier: ViewModifier {
    @State private var phase: CGFloat = -1.0
    func body(content: Content) -> some View {
        content.overlay(
                LinearGradient(
                    gradient: Gradient(colors: [.clear, .white.opacity(0.35), .clear]),
                    startPoint: .topLeading, endPoint: .bottomTrailing
                )
                    .rotationEffect(.degrees(20))
                    .offset(x: phase * 220, y: phase * 220)
                    .blendMode(.plusLighter)
            )
            .onAppear {
                withAnimation(.linear(duration: 1.25).repeatForever(autoreverses: false)) { phase = 1.0 }
            }
    }
}
private extension View { func shimmer() -> some View { modifier(ShimmerModifier()) } }
