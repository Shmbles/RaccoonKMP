package com.shmbles.raccoon.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmbles.raccoon.ui.theme.GameWhite

/**
 * Visual representation of the back face of a playing card.
 *
 * Rendered for face-down opponent hands and draw deck representations.
 *
 * @param modifier Layout modifiers.
 */
@Composable
fun CardBackView(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .shadow(4.dp, shape)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE53935), Color(0xFFC62828))
                )
            )
            .border(2.dp, GameWhite, shape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(6.dp)
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
        )
        Text(
            text = "R",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.ExtraBold
        )
    }
}
