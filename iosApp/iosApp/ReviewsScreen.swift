//
//  ReviewsScreen.swift
//  iosApp
//
//  Created by sharon bronshteyn on 18/08/2025.
//

import SwiftUI
import Shared

// MARK: - ViewModel Wrapper (Top-Level)

@MainActor
final class ReviewsViewModelWrapper: ObservableObject {
    let viewModel: ReviewsViewModel
    @Published var uiState: ReviewsState

    init() {
        self.viewModel = ReviewsViewModel()
        self.uiState = viewModel.uiState.value
    }

    func startObserving() async {
        // StateFlow<ReviewsState> נחשף ע״י SKIE כ-AsyncSequence
        for await state in viewModel.uiState {
            self.uiState = state
        }
    }
}

// MARK: - Screen

struct ReviewsScreen: View {
    @StateObject private var viewModel = ReviewsViewModelWrapper()

    var body: some View {
        VStack {
            switch onEnum(of: viewModel.uiState) {
            case .loading:
                LoadingView()

            case .loaded(let loaded):
                ReviewsListView(reviews: loaded.reviews)

            case .error(let err):
                ErrorView(message: err.errorMessage)
            }
        }
        .task {
            await viewModel.startObserving()
        }
        .navigationTitle("Reviews")
    }
}

// MARK: - List + Rows

struct ReviewsListView: View {
    let reviews: Reviews

    var body: some View {
        List(reviews.items, id: \.id) { review in
            ReviewRowView(review: review)
        }
        .listStyle(.plain)
    }
}

struct ReviewRowView: View {
    let review: Review

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            ReviewImageView(imagePath: review.imagePath)

            VStack(alignment: .leading, spacing: 6) {
                // אין title במודל, נשתמש ב-id ככותרת
                Text(nonEmpty(review.id) ?? "Untitled")
                    .font(.headline)
                    .lineLimit(2)

                if let text = nonEmpty(review.comment) {
                    Text(text)
                        .font(.body)
                        .lineLimit(3)
                }

                let rating = review.rating
                if rating > 0 {
                    HStack(spacing: 2) {
                        ForEach(0..<min(rating, 5), id: \.self) { _ in
                            Image(systemName: "star.fill")
                        }
                    }
                    .font(.caption)
                }
            }
            Spacer(minLength: 0)
        }
        .padding(.vertical, 8)
    }

    private func nonEmpty(_ s: String?) -> String? {
        guard let s, !s.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return nil }
        return s
    }
}

struct ReviewImageView: View {
    let imagePath: String?

    var body: some View {
        if let path = imagePath, let url = URL(string: path) {
            AsyncImage(url: url) { phase in
                switch phase {
                case .empty:
                    placeholder
                case .success(let image):
                    image.resizable().scaledToFill()
                case .failure:
                    placeholder
                @unknown default:
                    placeholder
                }
            }
            .frame(width: 56, height: 56)
            .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
        } else {
            placeholder
                .frame(width: 56, height: 56)
                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
        }
    }

    private var placeholder: some View {
        ZStack {
            Color.gray.opacity(0.15)
            Image(systemName: "photo")
                .imageScale(.medium)
                .foregroundStyle(.secondary)
        }
    }
}

// MARK: - States

struct LoadingView: View {
    var body: some View {
        VStack(spacing: 12) {
            ProgressView()
            Text("Loading…")
                .font(.callout)
                .foregroundStyle(.secondary)
        }
        .padding(.top, 40)
    }
}

struct ErrorView: View {
    var message: String
    var body: some View {
        VStack(spacing: 10) {
            Image(systemName: "exclamationmark.triangle.fill")
                .imageScale(.large)
                .foregroundStyle(.orange)
            Text(message)
                .font(.title3)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
        }
        .padding(.top, 40)
    }
}

// MARK: - Preview

#Preview {
    NavigationView {
        ReviewsScreen()
    }
}
