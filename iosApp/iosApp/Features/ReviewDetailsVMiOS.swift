// ReviewsVMiOS.swift
import Foundation
import Shared

@MainActor
final class ReviewsVMiOS: ObservableObject {
    /// מפעל ליצירת VM חדש כשצריך רענון (אופציונלי)
    private let makeVM: (() -> ReviewsViewModel)?
    private var streamTask: Task<Void, Never>?

    @Published var state: ReviewsState
    private(set) var vm: ReviewsViewModel

    // MARK: - Init מומלץ (עם מפעל)
    /// השתמשי בזה אם את רוצה ש-`refresh()` יבצע Fetch אמיתי (יצור VM חדש)
    init(makeVM: @escaping () -> ReviewsViewModel) {
        let v = makeVM()
        self.makeVM = makeVM
        self.vm = v
        self.state = v.uiState.value
        subscribe()
    }

    // MARK: - Init תואם לאחור (כמו שהיה לך)
    /// אם תשתמשי בזה, `refresh()` לא יוכל לייצר VM חדש (אין מפעל), אך עדיין נחדש מנוי.
    convenience init(vm: ReviewsViewModel) {
        self.init(_vm: vm, factory: nil)
    }

    // designated private init לשני המסלולים
    private init(_vm: ReviewsViewModel, factory: (() -> ReviewsViewModel)?) {
        self.makeVM = factory
        self.vm = _vm
        self.state = _vm.uiState.value
        subscribe()
    }

    // MARK: - Subscription לניטור ה-Flow מ-KMM
    private func subscribe() {
        streamTask?.cancel()
        streamTask = Task {
            for await s in vm.uiState {
                self.state = s
            }
        }
    }

    // MARK: - פעולות
    func addReview(_ review: Shared.Review) {
        vm.addReview(review: review)
    }

    /// רענון אקטיבי: אם יש "מפעל" ניצור VM חדש (שיבצע fetch/subscribe טריים).
    /// אם אין מפעל (init(vm:)), נבצע חידוש מנוי בלבד.
    func refresh() {
        if let makeVM {
            streamTask?.cancel()
            let newVM = makeVM()
            self.vm = newVM
            self.state = newVM.uiState.value
            subscribe()
        } else {
            // אין מפעל – נחדש רק מנוי (עדיין יקבל עדכון מה-listenReviews הקיים)
            subscribe()
        }
    }

    deinit {
        streamTask?.cancel()
    }
}
