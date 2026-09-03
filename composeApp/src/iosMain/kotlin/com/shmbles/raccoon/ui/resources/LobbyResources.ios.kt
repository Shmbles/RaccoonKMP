package com.shmbles.raccoon.ui.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.shmbles.raccoon.ui.resources.utils.imageFromBundle

@Composable
actual fun getLobbyBackgroundPainter(): Painter {
    return imageFromBundle("lobby_screen_background")
}
