package com.shmbles.raccoon.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores primaria del juego ("Guerra de Comida").
 */

// Tonos verdes para botones y llamadas a la acción principal
val GameGreenLight = Color(0xFF8CD842)
val GameGreenDark = Color(0xFF4FA80F)
val GameGreenBorder = Color(0xFF2E6606)

// Tonos azules para paneles contenedores y fondos de interfaz
val GameBluePanel = Color(0xFF2D4A68)
val GameBlueText = Color(0xFF152C45)

// Acentos cromáticos para destacados y efectos
val GamePinkLight = Color(0xFFFF7AF3)
val GamePinkDark = Color(0xFFAC26D8)

// Colores neutrales y sombras de contraste
val GameWhite = Color(0xFFFFFFFF)
val GameWhiteCream = Color(0xFFFFFBE6)
val GameBlackOutline = Color(0xFF000000)
val GameShadow = Color(0x80000000)

// Mapeo a tokens de Material 3
val md_theme_light_primary = GameGreenDark
val md_theme_light_onPrimary = GameWhite
val md_theme_light_primaryContainer = GameGreenLight
val md_theme_light_onPrimaryContainer = Color(0xFF0F2F00)

val md_theme_light_secondary = GameBluePanel
val md_theme_light_onSecondary = GameWhite
val md_theme_light_secondaryContainer = Color(0xFFD3E4FF)
val md_theme_light_onSecondaryContainer = Color(0xFF001C38)

val md_theme_light_tertiary = GamePinkDark
val md_theme_light_onTertiary = GameWhite
val md_theme_light_tertiaryContainer = Color(0xFFFFD7F5)
val md_theme_light_onTertiaryContainer = Color(0xFF380047)

val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color.White
val md_theme_light_onErrorContainer = Color(0xFF410002)

val md_theme_light_background = Color(0xFFF8FDFF)
val md_theme_light_onBackground = Color(0xFF001F25)
val md_theme_light_surface = GameWhiteCream
val md_theme_light_onSurface = Color(0xFF191C1C)

val md_theme_dark_primary = GameGreenLight
val md_theme_dark_onPrimary = Color(0xFF183800)
val md_theme_dark_primaryContainer = GameGreenDark
val md_theme_dark_onPrimaryContainer = Color(0xFFB5F487)
