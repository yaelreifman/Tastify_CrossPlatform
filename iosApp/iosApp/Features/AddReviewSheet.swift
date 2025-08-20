import SwiftUI
import PhotosUI
import FirebaseStorage
import Shared

struct AddReviewSheet: View {
    @Environment(\.dismiss) private var dismiss

    @State private var restaurantName = ""
    @State private var rating = 0
    @State private var comment = ""
    @State private var address = ""

    @State private var selectedItem: PhotosPickerItem?
    @State private var pickedImageData: Data?
    @State private var uploading = false

    var onSave: (Shared.Review) -> Void

    var body: some View {
        NavigationStack {
            Form {
                Section("Restaurant") {
                    TextField("Restaurant Name", text: $restaurantName)
                }

                Section("Rating") {
                    HStack {
                        ForEach(0..<5, id: \.self) { i in
                            Image(systemName: i < rating ? "star.fill" : "star")
                                .foregroundStyle(.yellow)
                                .onTapGesture { rating = i + 1 }
                        }
                    }
                }

                Section("Comment") {
                    TextField("Comment", text: $comment, axis: .vertical)
                        .lineLimit(3...6)
                }

                Section("Address") {
                    TextField("Address", text: $address)
                }

                Section("Image") {
                    PhotosPicker(selection: $selectedItem, matching: .images) {
                        Label("Choose from Photos", systemImage: "photo.on.rectangle")
                    }

                    if let data = pickedImageData, let img = UIImage(data: data) {
                        Image(uiImage: img)
                            .resizable()
                            .scaledToFit()
                            .frame(height: 150)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    }

                    if uploading {
                        ProgressView("Uploading…")
                    }
                }
            }
            .navigationTitle("Add Review")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { save() }
                    .disabled(restaurantName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || uploading)
                }
            }
        }
        .onChange(of: selectedItem) { _, newItem in
            Task {
                if let data = try? await newItem?.loadTransferable(type: Data.self) {
                    self.pickedImageData = data
                }
            }
        }
    }

    private func save() {
        let id = UUID().uuidString
        uploading = true

        // 1) מעלים תמונה (אם יש), 2) משיגים URL להכניס ל-imagePath, 3) בונים Review בסדר פרמטרים נכון
        uploadImageIfNeeded(data: pickedImageData, id: id) { urlString in
            let now = Int64(Date().timeIntervalSince1970 * 1000)

            // שימי לב: סדר/שמות פרמטרים = בדיוק מה שהקומפיילר ביקש
            let review = Shared.Review(
                id: id,
                restaurantId: nil,                                  // אם יש לך מזהה מסעדה פנימי
                rating: Int32(rating),                              // אם נופל, החליפי ל-Int(rating)
                comment: comment.isEmpty ? nil : comment,
                imagePath: urlString,
                restaurantName: restaurantName,
                address: address.isEmpty ? nil : address,
                latitude: nil,
                longitude: nil,
                placeId: nil,                                       // אם משתמשת ב-Google Places
                createdAt: now
            )

            uploading = false
            onSave(review)
            dismiss()
        }
    }

    private func uploadImageIfNeeded(data: Data?, id: String, completion: @escaping (String?) -> Void) {
        guard let data else { completion(nil); return }
        let storage = Storage.storage()
        let ref = storage.reference().child("reviews/\(id).jpg")
        ref.putData(data, metadata: nil) { _, error in
            if let error = error {
                print("Upload error:", error)
                completion(nil)
                return
            }
            ref.downloadURL { url, err in
                if let err = err {
                    print("Download URL error:", err)
                    completion(nil)
                } else {
                    completion(url?.absoluteString)
                }
            }
        }
    }
}
