//import Shared
//
//enum ReviewsStateCase {
//    case loading
//    case error(Shared.ReviewStateError)
//    case loaded(Shared.ReviewsStateLoaded)
//}
//
//func castReviewsState(_ s: Shared.ReviewsState) -> ReviewsStateCase {
//    if let l = s as? Shared.ReviewsStateLoaded { return .loaded(l) }
//    if let e = s as? Shared.ReviewsStateError  { return .error(e) }
//    return .loading
//}
