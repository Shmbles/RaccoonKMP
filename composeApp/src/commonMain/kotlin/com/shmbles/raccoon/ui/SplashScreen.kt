package com.shmbles.raccoon.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmbles.raccoon.component.SplashComponent
import com.shmbles.raccoon.ui.components.ResponsiveLayout
import com.shmbles.raccoon.ui.resources.getSplashBackgroundPainter
import com.shmbles.raccoon.ui.resources.getSplashCharacterPainter
import com.shmbles.raccoon.ui.resources.getSplashTitlePainter
import com.shmbles.raccoon.ui.values.R

/**
 * Splash welcome screen welcoming users to the Raccoon card game.
 *
 * Renders the layered backdrop, character art, game logo, and a stylized action button
 * to advance to the main menu.
 *
 * @param component The Decompose [SplashComponent] governing navigation to the main menu.
 */
@Composable
fun SplashScreen(component: SplashComponent) {
    ResponsiveLayout {
        // Fullscreen immersive background
        Image(
            painter = getSplashBackgroundPainter(),
            contentDescription = R.string.splash_background_content_description,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Game Title Banner
        Image(
            painter = getSplashTitlePainter(),
            contentDescription = R.string.splash_title_content_description,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(start = 24.dp, top = 20.dp)
                .fillMaxWidth(0.35f)
                .rotate(-8f),
            contentScale = ContentScale.Fit
        )

        // Illustrated Character Artwork
        Image(
            painter = getSplashCharacterPainter(),
            contentDescription = R.string.splash_characters_content_description,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom = 68.dp)
                .offset(x = 20.dp)
                .fillMaxWidth(0.75f),
            contentScale = ContentScale.Fit
        )

        // Play/Start Button Action
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom = 40.dp)
                .wrapContentSize()
        ) {
            BotonGameStyle(
                texto = R.string.splash_button_text,
                onClick = { component.onNavigateToMainMenu() }
            )
        }
    }
}

/**
 * Arcade-styled primary action button featuring a magenta-to-purple gradient, neon glow, and white border.
 *
 * @param texto Button display text.
 * @param onClick Click handler.
 */
@Composable
fun BotonGameStyle(
    texto: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(50),
                spotColor = Color(0xFFD900FF),
                ambientColor = Color(0xFFD900FF)
            )
            .clip(RoundedCornerShape(50))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF55FF),
                        Color(0xFF8A00C2)
                    )
                )
            )
            .border(
                width = 3.dp,
                color = Color.White,
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 40.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            style = TextStyle(
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
    }
}
