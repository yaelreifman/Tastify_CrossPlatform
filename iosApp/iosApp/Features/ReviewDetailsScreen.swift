// DetailsScreen.swift
import SwiftUI
import Shared
import CoreLocation

struct DetailsScreen: View {
    let review: Shared.Review

    @State private var coord: CLLocationCoordinate2D?

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // HERO IMAGE
                HeaderImage(imagePath: review.imagePath, rating: review.ratingInt)
                    .frame(height: 260)
                    .clipShape(RoundedRectangle(cornerRadius: 18))
                    .padding(.horizontal)

                // TITLE + DATE + ADDRESS + COMMENT
                VStack(alignment: .leading, spacing: 10) {
                    HStack(alignment: .firstTextBaseline) {
                        Text(review.restaurantName.isEmpty ? "Restaurant" : review.restaurantName)
                            .font(.title2).bold()
                            .lineLimit(1)
                        Spacer(minLength: 12)
                        Text(review.createdAtDate, style: .date)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }

                    // ⭐️ שורת כוכבים
                    if review.ratingInt > 0 {
                        HStack(spacing: 2) {
                            ForEach(0..<min(review.ratingInt, 5), id: \.self) { _ in
                                Image(systemName: "star.fill")
                            }
                        }
                        .font(.caption)
                        .foregroundStyle(.yellow)
                    }

                    if !review.addressSafe.isEmpty {
                        HStack(spacing: 6) {
                            Image(systemName: "mappin.and.ellipse")
                            Text(review.addressSafe).lineLimit(2)
                        }
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                    }

                    if !review.comment.isEmpty {
                        Text(review.comment)
                            .font(.body)
                    }
                }
                .padding(.horizontal)

                // GOOGLE MAP
                if let coord {
                    GoogleMapView(
                        coordinate: coord,
                        zoom: 15,
                        title: review.restaurantName.isEmpty ? "Restaurant" : review.restaurantName
                    )
                        .frame(height: 240)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .padding(.horizontal)
                        .transition(.opacity.combined(with: .scale))
                }
            }
            .padding(.vertical, 12)
        }
        .navigationTitle("Review")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await resolveCoordinateIfNeeded()
        }
    }

    // אם אין lat/lng – ננסה להשיג:
    // 1) אם ה-VM שלך "מעשיר" כבר ב-shared, לרוב זה יספיק.
    // 2) אחרת—fallback: גאוקודינג לפי כתובת דרך GeocodingRepository.
    @MainActor
    private func resolveCoordinateIfNeeded() async {
        if let lat = review.latitude?.doubleValue,
           let lng = review.longitude?.doubleValue {
            coord = CLLocationCoordinate2D(latitude: lat, longitude: lng)
            return
        }
        guard !review.addressSafe.isEmpty else { return }

        do {
            if let pair = try await Shared.GeocodingRepository.shared
                .getCoordinatesFromAddress(address: review.addressSafe)
            {
                let lat: Double? = (pair.first as? NSNumber)?.doubleValue ?? (pair.first as? Double)
                let lng: Double? = (pair.second as? NSNumber)?.doubleValue ?? (pair.second as? Double)
                if let lat, let lng {
                    withAnimation(.easeInOut) {
                        coord = CLLocationCoordinate2D(latitude: lat, longitude: lng)
                    }
                }
            }
        } catch {
            // אם יש fallback בצד ה-shared (ת״א), ייתכן שתחזרי לשם.
            print("Geocoding failed:", error.localizedDescription)
        }
    }
}

// MARK: - Header Image עם שימר ובדג'

private struct HeaderImage: View {
    let imagePath: String?
    let rating: Int

    var body: some View {
        ZStack(alignment: .topLeading) {
            if let path = imagePath, !path.isEmpty, let url = URL(string: path) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        RoundedRectangle(cornerRadius: 18)
                            .fill(Color.gray.opacity(0.12))
                            .localShimmer()
                    case .success(let image):
                        image.resizable().scaledToFill()
                    case .failure:
                        ZStack {
                            RoundedRectangle(cornerRadius: 18)
                                .fill(Color.gray.opacity(0.12))
                            Image(systemName: "photo")
                                .font(.title2)
                                .foregroundStyle(.gray.opacity(0.6))
                        }
                    @unknown default:
                        RoundedRectangle(cornerRadius: 18)
                            .fill(Color.gray.opacity(0.12))
                    }
                }
                .overlay(LinearGradient(
                    colors: [Color.black.opacity(0.0), Color.black.opacity(0.25)],
                    startPoint: .center, endPoint: .bottom
                ))
            } else {
                ZStack {
                    RoundedRectangle(cornerRadius: 18)
                        .fill(Color.gray.opacity(0.12))
                    Image(systemName: "photo")
                        .font(.title2)
                        .foregroundStyle(.gray.opacity(0.6))
                }
            }

            if rating > 0 {
                HStack(spacing: 6) {
                    Image(systemName: "star.fill")
                    Text("\(rating)")
                        .fontWeight(.semibold)
                }
                .font(.caption)
                .padding(.horizontal, 10)
                .padding(.vertical, 7)
                .background(.ultraThinMaterial, in: Capsule())
                .padding(12)
            }
        }
        .contentShape(Rectangle())
        .clipped()
    }
}

// MARK: - הרחבות עזר למודל

private extension Shared.Review {
    var ratingInt: Int { Int(self.rating ?? 0) }
    var createdAtMillis: Int64 { Int64(self.createdAt) ?? 0 }
    var createdAtDate: Date {
        Date(timeIntervalSince1970: TimeInterval(createdAtMillis) / 1000.0)
    }
    var addressSafe: String { self.address ?? "" }
}

// MARK: - שימר מקומי (כדי לא להתנגש עם מה שב-ReviewsScreen)

private struct LocalShimmerModifier: ViewModifier {
    @State private var phase: CGFloat = -1.0
    func body(content: Content) -> some View {
        content
            .overlay(
                LinearGradient(
                    gradient: Gradient(colors: [.clear, .white.opacity(0.35), .clear]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                    .rotationEffect(.degrees(20))
                    .offset(x: phase * 220, y: phase * 220)
                    .blendMode(.plusLighter)
            )
            .onAppear {
                withAnimation(.linear(duration: 1.25).repeatForever(autoreverses: false)) {
                    phase = 1.0
                }
            }
    }
}
private extension View {
    func localShimmer() -> some View { modifier(LocalShimmerModifier()) }
}
