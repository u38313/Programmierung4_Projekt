package de.medieninformatik.mypmaapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun MyPmaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // <- aus
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) darkColorScheme(
        primary = Color(0xFF8B5CF6),
        secondary = Color(0xFF22C55E),
        tertiary = Color(0xFF14B8A6),
        // … gern ergänzen …
    ) else lightColorScheme(
        primary = Color(0xFF8B5CF6),
        secondary = Color(0xFF22C55E),
        tertiary = Color(0xFF14B8A6),
        // … gern ergänzen …
    )
    MaterialTheme(colorScheme = colors, content = content)
}
