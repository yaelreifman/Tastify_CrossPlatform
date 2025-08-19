import SwiftUI
import Shared  // מודול ה־KMM

// המרה מ־ARGB Int32 לצבע SwiftUI
private extension Color {
    init(argb: Int32) {
        let v = UInt32(bitPattern: argb)
        let a = Double((v >> 24) & 0xFF) / 255.0
        let r = Double((v >> 16) & 0xFF) / 255.0
        let g = Double((v >> 8) & 0xFF) / 255.0
        let b = Double(v & 0xFF) / 255.0
        self = Color(.sRGB, red: r, green: g, blue: b, opacity: a)
    }
}

// ריווחים/רדיוסים (Int32 → CGFloat) — תואם לשמות בקוטלין
enum Dimens {
    static let xs: CGFloat = CGFloat(SpacingTokens.shared.XS)
    static let sm: CGFloat = CGFloat(SpacingTokens.shared.SM)
    static let md: CGFloat = CGFloat(SpacingTokens.shared.MD)
    static let lg: CGFloat = CGFloat(SpacingTokens.shared.LG)
    static let xl: CGFloat = CGFloat(SpacingTokens.shared.XL)
    static let xxl: CGFloat = CGFloat(SpacingTokens.shared.XXL)

    static let radiusSm: CGFloat = CGFloat(RadiusTokens.shared.SM)
    static let radiusMd: CGFloat = CGFloat(RadiusTokens.shared.MD)
    static let radiusLg: CGFloat = CGFloat(RadiusTokens.shared.LG)
}

// צבעים מה־shared
enum AppColors {
    static let primary           = Color(argb: ColorTokens.shared.PRIMARY)
    static let onPrimary         = Color(argb: ColorTokens.shared.ON_PRIMARY)
    static let surface           = Color(argb: ColorTokens.shared.SURFACE)
    static let onSurface         = Color(argb: ColorTokens.shared.ON_SURFACE)
    static let onSurfaceVariant  = Color(argb: ColorTokens.shared.ON_SURFACE_VARIANT)
    static let star              = Color(argb: ColorTokens.shared.STAR)
    static let placeholder       = Color(argb: ColorTokens.shared.PLACEHOLDER)
    static let divider           = Color(argb: ColorTokens.shared.DIVIDER)
}

// טיפוגרפיה
enum Fonts {
    static let title    = Font.system(size: CGFloat(TypeScale.shared.TITLE),    weight: .semibold)
    static let subtitle = Font.system(size: CGFloat(TypeScale.shared.SUBTITLE), weight: .medium)
    static let body     = Font.system(size: CGFloat(TypeScale.shared.BODY))
    static let caption  = Font.system(size: CGFloat(TypeScale.shared.CAPTION))
}

// כוכבים
enum Stars {
    static let max = Int(StarsTokens.shared.MAX)
}
