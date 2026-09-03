package com.shmbles.raccoon.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.shmbles.raccoon.ui.theme.*

/**
 * Primary action button styled with rounded corners, depth shadow, and vibrant green gradients.
 *
 * @param onClick Invoked when the user triggers the button.
 * @param modifier Layout modifiers applied to the button container.
 * @param enabled Whether interaction is permitted; transitions to disabled grey palette when false.
 * @param content Slot content placed inside the button layout.
 */
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 4.dp else 0.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(
                brush = if (enabled) {
                    Brush.verticalGradient(
                        colors = listOf(GameGreenLight, GameGreenDark)
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color.Gray, Color.DarkGray)
                    )
                }
            )
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        val textStyle = MaterialTheme.typography.titleLarge.copy(
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )

        ProvideTextStyle(value = textStyle) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                content()
            }
        }
    }
}
