package com.shmbles.raccoon.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.shmbles.raccoon.model.CardColor

/**
 * Función de utilidad para obtener el color de Compose correspondiente a un [CardColor].
 *
 * @param cardColor El color de la carta del juego.
 * @return El objeto [Color] de Compose correspondiente.
 */
@Composable
fun getComposeColor(cardColor: CardColor): Color {
    return when (cardColor) {
        CardColor.YELLOW -> Color(0xFFFFF9C4) // Specific light colors can be kept for cards
        CardColor.PINK -> Color(0xFFF8BBD0)
        CardColor.GREEN -> Color(0xFFC8E6C9)
        CardColor.BLUE -> Color(0xFFB3E5FC)
        CardColor.ORANGE -> Color(0xFFFFE0B2)
    }
}