import SwiftUI
import Shared

struct AddReviewSheet: View {
    @Environment(\.dismiss) private var dismiss

    @State private var restaurantName = ""
    @State private var rating = 0
    @State private var comment = ""
    @State private var address = ""
    @State private var imageUrl: String?

    var onSave: (Review) -> Void

    var body: some View {
        NavigationStack {
            Form {
                TextField("Restaurant Name", text: $restaurantName)

                HStack {
                    ForEach(0..<5, id: \.self) { idx in
                        Image(systemName: idx < rating ? "star.fill" : "star")
                            .foregroundStyle(.yellow)
                            .onTapGesture { rating = idx + 1 }
                    }
                }

                TextField("Comment", text: $comment)
                TextField("Address", text: $address)

                if let path = imageUrl, let url = URL(string: path) {
                    AsyncImage(url: url) { img in
                        img.resizable().scaledToFit()
                    } placeholder: {
                        Color.gray.opacity(0.1)
                    }
                    .frame(height: 150)
                }

                // 📌 פה אפשר לחבר ImagePicker (גלריה/מצלמה)
                Button("Select Image") {
                    // TODO: integrate UIImagePickerController
                }
            }
            .navigationTitle("Add Review")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        let review = Review(
                            id: UUID().uuidString,
                            restaurantName: restaurantName,
                            rating: KotlinInt(value: Int32(rating)),
                            comment: comment,
                            address: address,
                            imagePath: imageUrl
                        )
                        onSave(review)
                        dismiss()
                    }
                }
            }
        }
    }
}
