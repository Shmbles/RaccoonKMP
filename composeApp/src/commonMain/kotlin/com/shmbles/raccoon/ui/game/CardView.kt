package com.shmbles.raccoon.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmbles.raccoon.model.Card
import com.shmbles.raccoon.ui.theme.GameWhite

/**
 * Visual representation of an individual playing card (Food card or Bear card).
 *
 * @param card The [Card] instance to render.
 * @param isClickable Whether the card responds to user click interactions.
 * @param isSelected Whether the card has an active selected state (highlighted with a gold border).
 * @param onClick Action invoked when the card is clicked.
 */
@Composable
fun CardView(
    card: Card,
    isClickable: Boolean,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFFFD700) else GameWhite
    val borderWidth = if (isSelected) 3.dp else 2.dp
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .height(76.dp)
            .width(54.dp)
            .shadow(3.dp, shape)
            .clip(shape)
            .background(
                when (card) {
                    is Card.FoodCard -> getComposeColor(card.color)
                    is Card.BearCard -> Color(0xFF5D4037)
                }
            )
            .border(borderWidth, borderColor, shape)
            .then(
                if (isClickable) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onClick() }
                ) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        when (card) {
            is Card.FoodCard -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(4.dp)
                        .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${card.value}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black.copy(alpha = 0.7f),
                            shadow = null
                        )
                    )
                }
            }

            is Card.BearCard -> {
                Text("🐻", fontSize = 26.sp)
            }
        }
    }
}
