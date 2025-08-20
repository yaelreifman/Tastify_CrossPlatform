import SwiftUI
import PhotosUI
import FirebaseStorage
import Shared
import Foundation
import UIKit

// ISO-8601 עם שבריות שניות ו-Z (UTC)
extension Date {
    var iso8601ZString: String {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        f.timeZone = TimeZone(secondsFromGMT: 0)
        return f.string(from: self)
    }
}

// עטיפה ל-Camera (UIKit)
private struct CameraPicker: UIViewControllerRepresentable {
    var onImage: (Data) -> Void
    @Environment(\.dismiss) private var dismiss

    final class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
        let parent: CameraPicker
        init(_ parent: CameraPicker) { self.parent = parent }

        func imagePickerController(_ picker: UIImagePickerController,
                                   didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            let image = (info[.editedImage] ?? info[.originalImage]) as? UIImage
            if let img = image, let data = img.jpegData(compressionQuality: 0.9) {
                parent.onImage(data)
            }
            parent.dismiss()
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) { parent.dismiss() }
    }

    func makeCoordinator() -> Coordinator { Coordinator(self) }
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.allowsEditing = false
        picker.delegate = context.coordinator
        return picker
    }
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
}

struct AddReviewSheet: View {
    @Environment(\.dismiss) private var dismiss

    @State private var restaurantName = ""
    @State private var rating = 0
    @State private var comment = ""
    @State private var address = ""

    // גלריה
    @State private var selectedItem: PhotosPickerItem?
    @State private var pickedImageData: Data?

    // מצלמה
    @State private var showCamera = false
    @State private var showCameraUnavailableAlert = false

    // העלאה
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
                    TextField("Comment", text: $comment, axis: .vertical).lineLimit(3...6)
                }
                Section("Address") {
                    TextField("Address", text: $address)
                }
                Section("Image") {
                    // גלריה
                    PhotosPicker(selection: $selectedItem, matching: .images) {
                        Label("Choose from Photos", systemImage: "photo.on.rectangle")
                    }
                    // מצלמה
                    Button {
                        if UIImagePickerController.isSourceTypeAvailable(.camera) {
                            showCamera = true
                        } else {
                            showCameraUnavailableAlert = true
                        }
                    } label: { Label("Take Photo", systemImage: "camera") }

                    if let data = pickedImageData, let img = UIImage(data: data) {
                        Image(uiImage: img)
                            .resizable().scaledToFit().frame(height: 150)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                            .overlay { RoundedRectangle(cornerRadius: 8).stroke(.black.opacity(0.08), lineWidth: 0.5) }
                    }

                    if uploading { ProgressView("Uploading…") }
                }
            }
            .navigationTitle("Add Review")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { save() }
                    .disabled(restaurantName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || uploading)
                }
            }
        }
            // המרה של פריט גלריה ל-Data
        .onChange(of: selectedItem) { _, newItem in
            Task {
                if let data = try? await newItem?.loadTransferable(type: Data.self) {
                    self.pickedImageData = data
                }
            }
        }
        // מצלמה
        .sheet(isPresented: $showCamera) { CameraPicker { data in self.pickedImageData = data } }
        .alert("Camera Unavailable",
               isPresented: $showCameraUnavailableAlert,
               actions: { Button("OK", role: .cancel) {} },
               message: { Text("Camera is not available on this device/simulator.") })
    }

    // MARK: - Actions
    private func save() {
        let id = UUID().uuidString
        let nowIso = Date().iso8601ZString
        uploading = true

        uploadImageIfNeeded(data: pickedImageData, id: id) { urlString in
            let trimmedName = restaurantName.trimmingCharacters(in: .whitespacesAndNewlines)
            let slug = trimmedName.isEmpty
                ? id
                : trimmedName.lowercased()
                .replacingOccurrences(of: "\\s+", with: "-", options: .regularExpression)

            let review = Shared.Review(
                id: id,
                restaurantId: slug,            // חובה String
                rating: Int32(rating),         // Int32
                comment: comment,              // חובה String
                imagePath: urlString,          // String?
                restaurantName: restaurantName,
                address: address,              // אפשר ריק
                latitude: nil,
                longitude: nil,
                placeId: nil,
                createdAt: nowIso              // String ISO-8601
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
