package br.edu.iftm.habittracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.util.Locale

val Green700 = Color(0xFF2E7D5B)
val Green500 = Color(0xFF3FA37A)
val Green100 = Color(0xFFDDF2E8)
val Ink = Color(0xFF1B2420)
val Paper = Color(0xFFF7F8F6)
val StreakColor = Color(0xFFF59E0B)

val HabitColors = listOf(
    Color(0xFF3FA37A),
    Color(0xFF3B82F6),
    Color(0xFFF59E0B),
    Color(0xFFEF4444),
    Color(0xFF8B5CF6),
    Color(0xFFEC4899)
)

val PtBr: Locale = Locale.forLanguageTag("pt-BR")

private val LightColors = lightColorScheme(
    primary = Green700,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Ink,
    secondary = Green500,
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink
)

private val DarkColors = darkColorScheme(
    primary = Green500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1F3D31),
    onPrimaryContainer = Green100,
    secondary = Green500,
    background = Color(0xFF111614),
    surface = Color(0xFF1A201D)
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyMedium = TextStyle(fontSize = 14.sp)
)

/**
 * Cor de um hábito na paleta. Posições fora da paleta usam a cor mais próxima.
 *
 * @param index posição gravada no hábito.
 * @return cor do hábito.
 */
fun habitColor(index: Int): Color = HabitColors[index.coerceIn(0, HabitColors.lastIndex)]

/**
 * Tema do app em Material 3, com paleta verde clara ou escura conforme o sistema.
 *
 * @param content telas desenhadas com o tema.
 */
@Composable
fun HabitTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
