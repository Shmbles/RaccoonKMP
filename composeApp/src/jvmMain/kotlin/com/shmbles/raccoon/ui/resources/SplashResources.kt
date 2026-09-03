package com.shmbles.raccoon.ui.resources

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.*
import org.jetbrains.compose.resources.*
import raccoonkmp.composeapp.generated.resources.*

@Composable
actual fun getSplashBackgroundPainter(): Painter {
    return painterResource(Res.drawable.splash_screen_background)
}

@Composable
actual fun getSplashTitlePainter(): Painter {
    return painterResource(Res.drawable.splash_screen_assets_title)
}

@Composable
actual fun getSplashCharacterPainter(): Painter {
    return painterResource(Res.drawable.splash_screen_assets_character)
}
