package com.shmbles.raccoon.ui.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

/**
 * Obtiene el [Painter] del fondo nocturno para la pantalla Splash.
 */
@Composable
expect fun getSplashBackgroundPainter(): Painter

/**
 * Obtiene el [Painter] del logotipo/título artístico "Guerra de Comida".
 */
@Composable
expect fun getSplashTitlePainter(): Painter

/**
 * Obtiene el [Painter] de la ilustración de los personajes mapaches.
 */
@Composable
expect fun getSplashCharacterPainter(): Painter
