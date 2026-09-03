package com.shmbles.raccoon.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shmbles.raccoon.model.CardColor
import com.shmbles.raccoon.model.Player
import com.shmbles.raccoon.model.TurnPhase
import com.shmbles.raccoon.ui.theme.GameBluePanel
import com.shmbles.raccoon.ui.theme.GameWhite

/**
 * Composite opponent area rendering a horizontal collection of compact opponent stations.
 *
 * @param opponents List of opponent [Player] models.
 * @param turnPhase Current game turn phase.
 * @param selectedBearCardId ID of currently selected Bear card, or null.
 * @param currentPlayerId ID of the active turn player.
 * @param scoreAnimationTrigger Trigger to pulse score updates on points scored.
 * @param onTargetPileSelect Handler invoked when a target food pile on an opponent is tapped.
 * @param modifier Layout modifiers.
 */
@Composable
fun OpponentsArea(
    opponents: List<Player>,
    turnPhase: TurnPhase,
    selectedBearCardId: Int?,
    currentPlayerId: String,
    scoreAnimationTrigger: Int,
    onTargetPileSelect: (playerId: String, color: CardColor) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        opponents.forEach { opponent ->
            val opponentIsActive = (opponent.id == currentPlayerId)
            OpponentViewCompact(
                player = opponent,
                isTargetable = (turnPhase == TurnPhase.USE_BEAR) && (selectedBearCardId != null),
                onTargetPileSelect = onTargetPileSelect,
                isTurnActive = opponentIsActive,
                scoreAnimationTrigger = scoreAnimationTrigger
            )
        }
    }
}

/**
 * Compact view of an individual opponent player showing their name, current score,
 * miniature face-down cards, and played food piles.
 *
 * @param player The opponent [Player] model.
 * @param isTargetable Whether the opponent's played food can be targeted by a Bear card attack.
 * @param onTargetPileSelect Callback invoked when a food pile is selected as target.
 * @param isTurnActive True if it is currently this opponent's turn.
 * @param scoreAnimationTrigger Animation trigger for score increases.
 * @param modifier Layout modifiers.
 */
@Composable
fun OpponentViewCompact(
    player: Player,
    isTargetable: Boolean,
    onTargetPileSelect: (playerId: String, color: CardColor) -> Unit,
    isTurnActive: Boolean,
    scoreAnimationTrigger: Int,
    modifier: Modifier = Modifier
) {
    val currentScore = player.scorePile.size
    val previousScore = remember { mutableStateOf(currentScore) }
    val scale = remember { Animatable(1.0f) }

    LaunchedEffect(scoreAnimationTrigger) {
        if (scoreAnimationTrigger > 0 && currentScore > previousScore.value) {
            scale.animateTo(1.5f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 800f))
            scale.animateTo(1.0f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f))
        }
        previousScore.value = currentScore
    }

    val backgroundColor = GameBluePanel.copy(alpha = 0.7f)

    val borderColor by animateColorAsState(
        targetValue = if (isTurnActive) Color(0xFFFF7AF3) else Color.Transparent,
        animationSpec = tween(500),
        label = "OpponentBorderColor"
    )

    Column(
        modifier = modifier
            .widthIn(max = 160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Opponent Header & Score Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                player.name,
                style = MaterialTheme.typography.bodyMedium.copy(color = GameWhite, fontWeight = FontWeight.Bold)
            )
            Box(
                modifier = Modifier
                    .background(GameWhite, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp)
                    .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            ) {
                Text(
                    "$currentScore",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black, color = GameBluePanel)
                )
            }
        }

        // Mini Card Backs
        Row(horizontalArrangement = Arrangement.spacedBy((-16).dp)) {
            repeat(player.hand.size.coerceAtMost(4)) {
                CardBackView(modifier = Modifier.size(width = 32.dp, height = 45.dp))
            }
        }

        // Played Food Piles (Targetable when attacking with Bear)
        if (isTargetable) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                player.playedFood.forEach { (color, cards) ->
                    if (cards.isNotEmpty()) {
                        val totalValue = cards.sumOf { it.value }

                        val scalePile by animateFloatAsState(
                            targetValue = if (isTargetable) 1.1f else 1f,
                            label = "PileScaleAnimation"
                        )

                        val pileModifier = if (isTargetable) {
                            Modifier
                                .clickable { onTargetPileSelect(player.id, color) }
                                .graphicsLayer { scaleX = scalePile; scaleY = scalePile }
                                .border(2.dp, Color.Red, RoundedCornerShape(6.dp))
                        } else {
                            Modifier
                        }

                        Box(
                            modifier = pileModifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(getComposeColor(color))
                                .border(1.dp, GameWhite.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$totalValue",
                                color = Color.Black.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
