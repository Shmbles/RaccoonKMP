package com.shmbles.raccoon.ui.resources

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.*
import org.jetbrains.compose.resources.*
import raccoonkmp.composeapp.generated.resources.*

@Composable
actual fun getGameScreenBackgroundPainter(): Painter {
    return painterResource(Res.drawable.game_screen_background)
}
