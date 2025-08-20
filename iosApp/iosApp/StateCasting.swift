import Shared

enum ReviewsStateCase {
    case loading
    case error(ReviewsStateError)
    case loaded(ReviewsStateLoaded)
}

func castReviewsState(_ s: ReviewsState) -> ReviewsStateCase {
    if let l = s as? ReviewsStateLoaded { return .loaded(l) }
    if let e = s as? ReviewsStateError  { return .error(e) }
    return .loading
}
