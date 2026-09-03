package com.shmbles.raccoon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Adaptive root container applying proportional density scaling across mobile, tablet, and desktop viewports.
 *
 * Establishes a reference target width (850dp landscape baseline) and scales UI elements and typography
 * proportionally between a clamped range of 0.7x (small phones) to 1.4x (desktop monitors), preventing
 * microscopic layouts on high-res displays while preserving tabletop proportions on smaller handsets.
 *
 * @param modifier Layout modifiers applied to the root container.
 * @param content Composable slot representing the screen content within [BoxScope].
 */
@Composable
fun ResponsiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val baseWidth = 850f
        val screenWidth = maxWidth.value
        val scaleFactor = (screenWidth / baseWidth).coerceIn(0.7f, 1.4f)

        CompositionLocalProvider(
            LocalDensity provides Density(
                density = LocalDensity.current.density * scaleFactor,
                fontScale = LocalDensity.current.fontScale * scaleFactor
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                content = content
            )
        }
    }
}
