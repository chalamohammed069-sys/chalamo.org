package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BananaYellow,
    secondary = NeonCyan,
    tertiary = White,
    background = AmoledBlack,
    surface = DarkGray,
    onPrimary = AmoledBlack,
    onSecondary = AmoledBlack,
    onTertiary = AmoledBlack,
    onBackground = White,
    onSurface = White,
    surfaceVariant = CardBg,
    onSurfaceVariant = White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark for AMOLED premium look
  dynamicColor: Boolean = false, // Disable dynamic colors to keep Banana Yellow brand colors
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
