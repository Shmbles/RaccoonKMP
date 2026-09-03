package com.shmbles.raccoon.ui.resources

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.*

/**
 * Obtiene el [Painter] correspondiente a la textura o ilustración de fondo de la mesa de juego principal.
 * Resuelto nativamente según la plataforma (Assets.xcassets en iOS, drawable en Android y JVM).
 */
@Composable
expect fun getGameScreenBackgroundPainter(): Painter
