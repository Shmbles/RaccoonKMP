package com.shmbles.raccoon.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import com.shmbles.raccoon.model.Card
import com.shmbles.raccoon.model.CardColor
import com.shmbles.raccoon.model.Player
import com.shmbles.raccoon.model.TurnPhase
import com.shmbles.raccoon.ui.theme.GameBluePanel
import com.shmbles.raccoon.ui.theme.GameWhite
import kotlinx.coroutines.launch

/**
 * Renders the dashboard and interactive card hand for the active player.
 *
 * Handles hand presentation, card selection offsets, spring score increment animations,
 * turn phase restrictions, and discard/play animation transitions.
 *
 * @param player The current [Player] instance.
 * @param isTurnActive True if it is currently this player's active turn.
 * @param turnPhase Current game turn phase ([TurnPhase.PLAY_FOOD], [TurnPhase.USE_BEAR], [TurnPhase.DRAW_AND_SCORE]).
 * @param selectedBearCardId Unique ID of the currently selected Bear card, or null if none selected.
 * @param scoreAnimationTrigger Monotonically increasing trigger for pulsing the score badge on points scored.
 * @param onPlayFood Action invoked when the player taps a Food card of a given [CardColor].
 * @param onBearCardSelect Action invoked when the player selects or toggles a Bear card.
 * @param modifier Layout modifiers.
 */
@Composable
fun PlayerView(
    player: Player,
    isTurnActive: Boolean,
    turnPhase: TurnPhase,
    selectedBearCardId: Int?,
    scoreAnimationTrigger: Int,
    onPlayFood: (CardColor) -> Unit,
    onBearCardSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentScore = player.scorePile.size
    val previousScore = remember { mutableStateOf(currentScore) }
    val scale = remember { Animatable(1.0f) }

    var handVisible by remember(player.hand) { mutableStateOf(false) }
    var colorToAnimate by remember { mutableStateOf<CardColor?>(null) }
    val animatedProgress = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(player.hand) { handVisible = true }

    LaunchedEffect(scoreAnimationTrigger) {
        if (scoreAnimationTrigger > 0 && currentScore > previousScore.value) {
            scale.animateTo(1.5f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 800f))
            scale.animateTo(1.0f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f))
        }
        previousScore.value = currentScore
    }

    val borderColor by animateColorAsState(
        targetValue = if (isTurnActive) Color(0xFF8CD842) else Color.Transparent,
        animationSpec = tween(500),
        label = "PlayerBorderAnimation"
    )

    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .widthIn(max = 480.dp)
            .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
            .clip(shape)
            .background(GameBluePanel.copy(alpha = 0.95f))
            .border(3.dp, borderColor, shape)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Player Identity & Score Counter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                player.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = GameWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
            Box(
                modifier = Modifier
                    .background(GameWhite, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    }
            ) {
                Text(
                    text = "★ $currentScore",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GameBluePanel,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // Overlapping Hand Cards
        LazyRow(
            modifier = Modifier
                .height(80.dp)
                .weight(1f, fill = false),
            horizontalArrangement = Arrangement.spacedBy((-20).dp, Alignment.End),
            contentPadding = PaddingValues(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(player.hand, key = { it.id }) { card ->
                Row {
                    AnimatedVisibility(
                        visible = handVisible,
                        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
                    ) {
                        val isAnimating = card is Card.FoodCard && card.color == colorToAnimate
                        val animationModifier = if (isAnimating) {
                            Modifier.graphicsLayer {
                                val progress = animatedProgress.value
                                alpha = 1f - progress
                                scaleX = 1f + (progress * 0.5f)
                                scaleY = 1f + (progress * 0.5f)
                                translationY = -progress * 100f
                            }
                        } else Modifier

                        Box(modifier = animationModifier) {
                            val isSelectedBear = (card is Card.BearCard && card.id == selectedBearCardId)

                            val selectionOffset by animateDpAsState(
                                if (isSelectedBear) (-12).dp else 0.dp,
                                label = "CardSelectionOffset"
                            )

                            Box(modifier = Modifier.offset(y = selectionOffset)) {
                                CardView(
                                    card = card,
                                    isClickable = isTurnActive && colorToAnimate == null,
                                    isSelected = isSelectedBear,
                                    onClick = {
                                        if (isTurnActive) {
                                            when (card) {
                                                is Card.FoodCard -> if (turnPhase == TurnPhase.PLAY_FOOD) {
                                                    if (colorToAnimate == null) {
                                                        colorToAnimate = card.color
                                                        coroutineScope.launch {
                                                            animatedProgress.animateTo(1f, animationSpec = tween(400))
                                                            onPlayFood(card.color)
                                                            colorToAnimate = null
                                                            animatedProgress.snapTo(0f)
                                                        }
                                                    }
                                                }
                                                is Card.BearCard -> if (turnPhase == TurnPhase.USE_BEAR) onBearCardSelect(card.id)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
