import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import org.example.project.ui.ColorTokens
import org.example.project.ui.RadiusTokens
import org.example.project.ui.SpacingTokens
import org.example.project.ui.TypeScale
import org.example.project.ui.StarsTokens

object Dimens {
    val xs = SpacingTokens.XS.dp
    val sm = SpacingTokens.SM.dp
    val md = SpacingTokens.MD.dp
    val lg = SpacingTokens.LG.dp
    val xl = SpacingTokens.XL.dp
    val xxl = SpacingTokens.XXL.dp

    val radiusSm = RadiusTokens.SM.dp
    val radiusMd = RadiusTokens.MD.dp
    val radiusLg = RadiusTokens.LG.dp
}

object Palette {
    val primary = Color(ColorTokens.PRIMARY)
    val onPrimary = Color(ColorTokens.ON_PRIMARY)
    val surface = Color(ColorTokens.SURFACE)
    val onSurface = Color(ColorTokens.ON_SURFACE)
    val onSurfaceVariant = Color(ColorTokens.ON_SURFACE_VARIANT)
    val star = Color(ColorTokens.STAR)
    val placeholder = Color(ColorTokens.PLACEHOLDER)
    val divider = Color(ColorTokens.DIVIDER)
}

object AppTextStyles {
    val title = TextStyle(fontSize = TypeScale.TITLE.sp, fontFamily = FontFamily.Default)
    val subtitle = TextStyle(fontSize = TypeScale.SUBTITLE.sp, fontFamily = FontFamily.Default)
    val body = TextStyle(fontSize = TypeScale.BODY.sp, fontFamily = FontFamily.Default)
    val caption = TextStyle(fontSize = TypeScale.CAPTION.sp, fontFamily = FontFamily.Default)
}

object Stars { const val max = StarsTokens.MAX }