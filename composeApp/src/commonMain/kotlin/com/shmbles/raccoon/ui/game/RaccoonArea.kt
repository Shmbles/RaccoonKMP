package com.shmbles.raccoon.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmbles.raccoon.model.CardColor
import com.shmbles.raccoon.model.Player
import com.shmbles.raccoon.ui.theme.GameWhite

/**
 * Center-table display presenting the current possession of colored Raccoon figurines.
 *
 * Each token indicates which player holds the majority of food played for that specific color.
 * Possessed tokens are highlighted with an animated gold border and display the current owner's tag.
 *
 * @param raccoonHolders Mapping of each [CardColor] to the player ID currently possessing the token.
 * @param players Full roster of players to resolve IDs to display nicknames.
 * @param modifier Layout modifiers.
 */
@Composable
fun RaccoonArea(
    raccoonHolders: Map<CardColor, String?>,
    players: List<Player>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .widthIn(max = 400.dp)
            .background(Color.Black.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        raccoonHolders.forEach { (color, playerId) ->
            val holderName = players.find { it.id == playerId }?.name ?: ""
            val hasOwner = playerId != null

            val borderColor by animateColorAsState(
                targetValue = if (hasOwner) Color(0xFFFFD700) else Color.Transparent,
                animationSpec = tween(500),
                label = "RaccoonBorderColor"
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(40.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(getComposeColor(color))
                        .border(2.dp, borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🦝", fontSize = 20.sp)
                }

                if (hasOwner) {
                    Text(
                        holderName.take(3),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = GameWhite,
                            fontSize = 8.sp
                        )
                    )
                }
            }
        }
    }
}
