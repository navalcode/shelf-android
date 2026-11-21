package dev.navalcode.shelf.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.navalcode.shelf.R

private val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
)
private val ShelfType = Typography(
    displaySmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.8).sp),
    headlineLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 38.sp, letterSpacing = (-0.7).sp),
    headlineMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 34.sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp),
    titleSmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)
private val Light = lightColorScheme(
    primary = Color(0xFF2946C6), onPrimary = Color.White,
    primaryContainer = Color(0xFFDFE6FF), onPrimaryContainer = Color(0xFF14266E),
    secondary = Color(0xFF505E86), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6EAF7), onSecondaryContainer = Color(0xFF27345B),
    background = Color(0xFFF7F8FC), onBackground = Color(0xFF171E31),
    surface = Color(0xFFF7F8FC), onSurface = Color(0xFF171E31),
    surfaceVariant = Color(0xFFE8ECF7), onSurfaceVariant = Color(0xFF515D76),
    surfaceContainer = Color(0xFFEDF0F9), surfaceContainerLow = Color(0xFFF0F2FA),
    outline = Color(0xFF6B7892), outlineVariant = Color(0xFFD7DDEC),
)
private val Dark = darkColorScheme(
    primary = Color(0xFFBBC8FF), onPrimary = Color(0xFF10236D),
    primaryContainer = Color(0xFF253B83), onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFC0C9E7), onSecondary = Color(0xFF243052),
    secondaryContainer = Color(0xFF344163), onSecondaryContainer = Color(0xFFE1E7FA),
    background = Color(0xFF101526), onBackground = Color(0xFFE5EAFA),
    surface = Color(0xFF101526), onSurface = Color(0xFFE5EAFA),
    surfaceVariant = Color(0xFF303A53), onSurfaceVariant = Color(0xFFBEC8E2),
    surfaceContainer = Color(0xFF1B2338), surfaceContainerLow = Color(0xFF161D30),
    outline = Color(0xFF8895B3), outlineVariant = Color(0xFF3A4560),
)

@Composable
fun ShelfTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = ShelfType, content = content)
}
