package com.shmbles.raccoon.ui.resources

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter

/**
 * Obtiene el [Painter] correspondiente a la ilustración de fondo de la pantalla de sala de espera (Lobby).
 * Resuelto nativamente según la plataforma (Assets.xcassets en iOS, drawable en Android y JVM).
 */
@Composable
expect fun getLobbyBackgroundPainter(): Painter
