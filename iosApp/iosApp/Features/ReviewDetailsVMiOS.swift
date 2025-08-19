
import Foundation
import Shared

@MainActor
final class ReviewsVMiOS: ObservableObject {
    let vm: ReviewsViewModel
    @Published var state: ReviewsState

    init(vm: ReviewsViewModel) {
        self.vm = vm
        self.state = vm.uiState.value
        Task {
            for await s in vm.uiState {
                self.state = s
            }
        }
    }

    func addReview(_ review: Review) {
        vm.addReview(review: review)
    }
}
