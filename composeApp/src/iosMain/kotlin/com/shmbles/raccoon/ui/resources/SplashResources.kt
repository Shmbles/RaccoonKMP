package com.shmbles.raccoon.ui.resources

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.*
import com.shmbles.raccoon.ui.resources.utils.*

@Composable
actual fun getSplashBackgroundPainter(): Painter {
    return imageFromBundle("splash_screen_background")
}

@Composable
actual fun getSplashTitlePainter(): Painter {
    return imageFromBundle("splash_screen_assets_title")
}

@Composable
actual fun getSplashCharacterPainter(): Painter {
    return imageFromBundle("splash_screen_assets_character")
}
