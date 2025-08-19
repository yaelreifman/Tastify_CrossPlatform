package org.example.project.ui

object SpacingTokens {
    const val XS = 4
    const val SM = 8
    const val MD = 12
    const val LG = 16
    const val XL = 24
    const val XXL = 32
}

object RadiusTokens {
    const val SM = 6
    const val MD = 10
    const val LG = 14
}

object StarsTokens { const val MAX = 5 }

/** גודל פונט בנקודות/ספרים (ללא טיפוס פלטפורמה) */
object TypeScale {
    const val TITLE = 20
    const val SUBTITLE = 16
    const val BODY = 15
    const val CAPTION = 12
}

/** צבעים כ-ARGB Int (0xAARRGGBB) */
object ColorTokens {
    const val PRIMARY = 0xFF3366FF.toInt()
    const val ON_PRIMARY = 0xFFFFFFFF.toInt()
    const val SURFACE = 0xFFFFFFFF.toInt()
    const val ON_SURFACE = 0xFF111111.toInt()
    const val ON_SURFACE_VARIANT = 0xFF6B7280.toInt()
    const val STAR = 0xFFFFC107.toInt()
    const val PLACEHOLDER = 0xFFF3F4F6.toInt()
    const val DIVIDER = 0xFFE5E7EB.toInt()
}
