package com.shmbles.raccoon.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.shmbles.raccoon.component.*
import com.shmbles.raccoon.model.*
import com.shmbles.raccoon.ui.components.*
import com.shmbles.raccoon.ui.game.*
import com.shmbles.raccoon.ui.resources.*
import com.shmbles.raccoon.ui.theme.*
import com.shmbles.raccoon.ui.values.*

/**
 * Main gameplay screen for the Raccoon card game.
 *
 * Coordinates the full tabletop experience for both Online Multiplayer and Local "Pass & Play" modes.
 * It observes the reactive game state from [GameComponent], distributes opponent hands and food piles
 * around the table perimeter, renders the central raccoon zone, and presents the active player's hand.
 *
 * @param component The Decompose [GameComponent] governing game actions, phases, and state flows.
 */
@Composable
fun GameScreen(component: GameComponent) {
    val gameState by component.gameState.collectAsState()
    val selectedBearCardId by component.selectedBearCardId.collectAsState()
    val isTurnTransitionVisible by component.isTurnTransitionVisible.collectAsState()
    val scoreAnimationTrigger by component.scoreAnimationTrigger.collectAsState()

    // In local "Pass & Play" mode, the active player's identity shifts sequentially each turn.
    // derivedStateOf ensures Compose recomposes player-specific zones (hand, played food, opponents).
    val myPlayerId by remember(gameState) {
        derivedStateOf { component.myPlayerId }
    }

    ResponsiveLayout {
        if (myPlayerId == null) {
            ConnectingOverlay(R.string.game_connecting_message)
            return@ResponsiveLayout
        }

        val me = gameState.players.find { it.id == myPlayerId }
        if (me == null) {
            ConnectingOverlay(R.string.game_waiting_for_state_message)
            return@ResponsiveLayout
        }

        // Dynamically position opponents around the table based on active room player count
        val allOpponents = remember(gameState.players, myPlayerId) {
            gameState.players.filter { it.id != myPlayerId }
        }

        val (leftOpponents, topOpponents, rightOpponents) = remember(allOpponents) {
            when (allOpponents.size) {
                0 -> Triple(emptyList(), emptyList(), emptyList())
                1 -> Triple(emptyList(), allOpponents, emptyList())
                2 -> Triple(listOf(allOpponents[0]), emptyList(), listOf(allOpponents[1]))
                3 -> Triple(listOf(allOpponents[0]), listOf(allOpponents[1]), listOf(allOpponents[2]))
                4 -> Triple(listOf(allOpponents[0]), listOf(allOpponents[1], allOpponents[2]), listOf(allOpponents[3]))
                else -> Triple(
                    allOpponents.take(2),
                    allOpponents.drop(2).dropLast(2),
                    allOpponents.takeLast(2)
                )
            }
        }

        val turnPhase = gameState.turnPhase
        val currentPlayerId = gameState.currentPlayerId
        val meIsActive = (currentPlayerId == myPlayerId)

        // Background visual layer
        Image(
            painter = getGameScreenBackgroundPainter(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        val safeAreaModifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(8.dp)

        // Tabletop layout container
        Box(modifier = safeAreaModifier) {

            // Center: Raccoon Control Area
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                RaccoonArea(
                    raccoonHolders = gameState.raccoonHolders,
                    players = gameState.players
                )
            }

            // Bottom: Active Player Dashboard (Played Food & Hand)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-12).dp)
            ) {
                PlayedFoodArea(
                    playedFood = me.playedFood,
                    isHorizontal = true,
                    onTargetSelect = { /* Cannot target own food */ },
                    isTargetable = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                PlayerView(
                    player = me,
                    isTurnActive = meIsActive,
                    turnPhase = turnPhase,
                    selectedBearCardId = selectedBearCardId,
                    scoreAnimationTrigger = scoreAnimationTrigger,
                    onPlayFood = { color -> component.onPlayFood(color) },
                    onBearCardSelect = { cardId -> component.onBearCardSelect(cardId) }
                )
            }

            // Top: Top Opponents Area
            if (topOpponents.isNotEmpty()) {
                Row(
                    modifier = Modifier.align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    topOpponents.forEach { opponent ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            OpponentViewTop(
                                player = opponent,
                                isTurnActive = opponent.id == currentPlayerId
                            )
                            Spacer(Modifier.height(4.dp))
                            PlayedFoodArea(
                                playedFood = opponent.playedFood,
                                isHorizontal = true,
                                onTargetSelect = { color ->
                                    selectedBearCardId?.let { component.onUseBear(it, opponent.id, color) }
                                },
                                isTargetable = (turnPhase == TurnPhase.USE_BEAR) && (selectedBearCardId != null)
                            )
                        }
                    }
                }
            }

            // Left: Left Flank Opponents
            if (leftOpponents.isNotEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    leftOpponents.forEach { opponent ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OpponentViewSide(
                                player = opponent,
                                isTurnActive = opponent.id == currentPlayerId,
                                isLeft = true
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.rotate(90f)) {
                                PlayedFoodArea(
                                    playedFood = opponent.playedFood,
                                    isHorizontal = true,
                                    onTargetSelect = { color ->
                                        selectedBearCardId?.let { component.onUseBear(it, opponent.id, color) }
                                    },
                                    isTargetable = (turnPhase == TurnPhase.USE_BEAR) && (selectedBearCardId != null)
                                )
                            }
                        }
                    }
                }
            }

            // Right: Right Flank Opponents
            if (rightOpponents.isNotEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    rightOpponents.forEach { opponent ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.rotate(-90f)) {
                                PlayedFoodArea(
                                    playedFood = opponent.playedFood,
                                    isHorizontal = true,
                                    onTargetSelect = { color ->
                                        selectedBearCardId?.let { component.onUseBear(it, opponent.id, color) }
                                    },
                                    isTargetable = (turnPhase == TurnPhase.USE_BEAR) && (selectedBearCardId != null)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            OpponentViewSide(
                                player = opponent,
                                isTurnActive = opponent.id == currentPlayerId,
                                isLeft = false
                            )
                        }
                    }
                }
            }

            // Bottom-Right: Turn Action Controls
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                TurnActionArea(
                    isTurnActive = meIsActive,
                    turnPhase = turnPhase,
                    onSkipPlayFood = { component.onSkipPlayFood() },
                    onSkipBear = { component.onSkipBear() },
                    onDraw = { component.onDrawAndScore() }
                )
            }
        }

        // Overlay: Game Over State
        if (gameState.status == GameStatus.GAME_OVER) {
            GameOverOverlay(
                winner = gameState.players.find { it.id == gameState.winnerId },
                isHost = component.isHost,
                onReturnToLobby = { component.onReturnToLobbyClicked() }
            )
        }

        // Overlay: Pass & Play Turn Transition Handover
        AnimatedVisibility(
            visible = isTurnTransitionVisible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            TurnTransitionOverlay(
                nextPlayerName = gameState.players.find { it.id == gameState.currentPlayerId }?.name,
                onConfirm = { component.onTurnConfirmed() }
            )
        }
    }
}

/**
 * Displays the collection of food cards played by a player, grouped by color.
 *
 * @param playedFood Mapping of card colors to the list of played food cards.
 * @param isHorizontal Whether to lay out the color piles horizontally or vertically.
 * @param onTargetSelect Callback invoked when a targetable food pile is clicked (e.g. for Bear attacks).
 * @param isTargetable Whether the piles can currently be targeted by an active Bear card.
 */
@Composable
fun PlayedFoodArea(
    playedFood: Map<CardColor, List<Card.FoodCard>>,
    isHorizontal: Boolean,
    onTargetSelect: (CardColor) -> Unit,
    isTargetable: Boolean
) {
    val activePiles = playedFood.filter { it.value.isNotEmpty() }

    if (activePiles.isEmpty()) {
        Box(modifier = Modifier.size(36.dp, 48.dp))
        return
    }

    if (isHorizontal) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            activePiles.forEach { (color, cards) ->
                FoodPileView(color, cards, isTargetable) { onTargetSelect(color) }
            }
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            activePiles.forEach { (color, cards) ->
                FoodPileView(color, cards, isTargetable) { onTargetSelect(color) }
            }
        }
    }
}

/**
 * Renders a compact pile of food cards of a specific color, displaying the total combined point value.
 *
 * @param color The [CardColor] category of this pile.
 * @param cards The food cards accumulated in this pile.
 * @param isTargetable Indicates if this pile is an eligible target for a Bear card attack.
 * @param onClick Click handler triggered when selected as an attack target.
 */
@Composable
fun FoodPileView(
    color: CardColor,
    cards: List<Card.FoodCard>,
    isTargetable: Boolean,
    onClick: () -> Unit
) {
    val totalValue = cards.sumOf { it.value }
    val pileColor = getComposeColor(color)
    val borderColor = if (isTargetable) Color.Red else GameWhite

    Box(
        modifier = Modifier
            .size(36.dp, 48.dp)
            .shadow(2.dp, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(pileColor)
            .border(if (isTargetable) 2.dp else 1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(enabled = isTargetable, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$totalValue",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            )
        }
    }
}

/**
 * Renders an opponent stationed at the top edge of the game board.
 *
 * @param player The opponent [Player] state.
 * @param isTurnActive Whether it is currently this opponent's turn.
 */
@Composable
private fun OpponentViewTop(player: Player, isTurnActive: Boolean) {
    val borderColor = if (isTurnActive) Color(0xFFFF7AF3) else Color.Transparent

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .background(GameBluePanel.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.labelSmall.copy(color = GameWhite, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("★${player.scorePile.size}", style = MaterialTheme.typography.labelSmall.copy(color = GameWhite))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            repeat(player.hand.size.coerceAtMost(5)) {
                CardBackView(modifier = Modifier.size(width = 24.dp, height = 36.dp))
            }
        }
    }
}

/**
 * Renders an opponent stationed at either the left or right lateral flank of the board.
 *
 * @param player The opponent [Player] state.
 * @param isTurnActive Whether it is currently this opponent's turn.
 * @param isLeft True if placed on the left flank; false for the right flank.
 */
@Composable
private fun OpponentViewSide(player: Player, isTurnActive: Boolean, isLeft: Boolean) {
    val borderColor = if (isTurnActive) Color(0xFFFF7AF3) else Color.Transparent
    val rotation = if (isLeft) 90f else -90f

    Box(modifier = Modifier.rotate(rotation)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(GameBluePanel.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.labelSmall.copy(color = GameWhite),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "★${player.scorePile.size}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GameWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                repeat(player.hand.size.coerceAtMost(5)) {
                    CardBackView(modifier = Modifier.size(width = 24.dp, height = 36.dp))
                }
            }
        }
    }
}

/**
 * Renders contextual primary action buttons depending on the current phase of the turn.
 *
 * @param isTurnActive True if the local player is the current turn holder.
 * @param turnPhase The current [TurnPhase] (playing food, using bear, or drawing & scoring).
 * @param onSkipPlayFood Handler to pass on playing food cards.
 * @param onSkipBear Handler to pass on using an active Bear card.
 * @param onDraw Handler to draw cards, calculate score, and conclude the turn.
 */
@Composable
private fun TurnActionArea(
    isTurnActive: Boolean,
    turnPhase: TurnPhase,
    onSkipPlayFood: () -> Unit,
    onSkipBear: () -> Unit,
    onDraw: () -> Unit
) {
    if (isTurnActive) {
        val (text, action) = when (turnPhase) {
            TurnPhase.PLAY_FOOD -> R.string.game_turn_action_skip to onSkipPlayFood
            TurnPhase.USE_BEAR -> R.string.game_turn_action_skip to onSkipBear
            TurnPhase.DRAW_AND_SCORE -> R.string.game_turn_action_draw_and_finish to onDraw
        }

        PrimaryButton(onClick = action, modifier = Modifier.height(45.dp)) {
            Text(text, style = MaterialTheme.typography.titleMedium, color = GameWhite)
        }
    }
}

/**
 * Fullscreen overlay displayed during initial network connection or while awaiting server synchronization.
 *
 * @param message Informational message presented to the player.
 */
@Composable
private fun ConnectingOverlay(message: String) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.8f)), contentAlignment = Alignment.Center) {
        Text(message, color = GameWhite)
    }
}

/**
 * End-of-game celebration and results overlay declaring the match winner.
 *
 * @param winner The winning [Player], or null in the event of an unresolved draw.
 * @param isHost Whether the local user is the room host with permission to return players to lobby.
 * @param onReturnToLobby Callback to disband or reset the match back to the lobby.
 */
@Composable
private fun GameOverOverlay(winner: Player?, isHost: Boolean, onReturnToLobby: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.9f)).clickable {},
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(R.string.game_over_title, style = MaterialTheme.typography.headlineLarge.copy(color = Color.Yellow))
            Text(
                "${R.string.game_over_winner} ${winner?.name ?: R.string.game_over_winner_unknown}",
                color = GameWhite,
                style = MaterialTheme.typography.titleLarge
            )
            if (isHost) PrimaryButton(onClick = onReturnToLobby) { Text(R.string.game_over_return_to_lobby) }
        }
    }
}

/**
 * Handover overlay displayed in "Pass & Play" mode between consecutive player turns.
 * Ensures the subsequent player can take possession of the device privately before revealing their hand.
 *
 * @param nextPlayerName The display name of the upcoming turn player.
 * @param onConfirm Callback when the next player confirms they are ready.
 */
@Composable
private fun TurnTransitionOverlay(nextPlayerName: String?, onConfirm: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(GameBluePanel).clickable {}, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(30.dp)) {
            Text(R.string.game_turn_transition_title, color = GameWhite)
            Text(nextPlayerName ?: "...", style = MaterialTheme.typography.displayMedium.copy(color = GameWhite))
            PrimaryButton(onClick = onConfirm) { Text(R.string.game_turn_transition_continue) }
        }
    }
}
