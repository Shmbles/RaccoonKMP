package com.shmbles.raccoon.engine

import com.shmbles.raccoon.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Motor del juego Raccoon, que gestiona la lógica del juego, el estado y las interacciones de los jugadores.
 *
 * @param gameConfig La configuración inicial del juego, incluyendo el número de jugadores y la puntuación para ganar.
 * @param playersInfo Una lista de pares (PlayerId, Nickname) para inicializar los jugadores.
 */
class GameEngine(
    private val gameConfig: GameConfig,
    private val playersInfo: List<Pair<String, String>>
) {
    private val _gameState: MutableStateFlow<GameState>
    val gameState: StateFlow<GameState>

    init {
        _gameState = MutableStateFlow(createInitialGameState())
        gameState = _gameState.asStateFlow()
    }

    /**
     * Procesa la acción de un jugador de jugar una carta de comida.
     *
     * @param playerId El ID del jugador que realiza la acción.
     * @param color El color de la carta de comida a jugar.
     */
    fun onPlayFood(playerId: String, color: CardColor) {
        val currentState = _gameState.value
        if (playerId != currentState.currentPlayerId || currentState.turnPhase != TurnPhase.PLAY_FOOD) return

        val player = currentState.players.find { it.id == playerId } ?: return
        val cardsToPlay = player.hand.filterIsInstance<Card.FoodCard>().filter { it.color == color }
        if (cardsToPlay.isEmpty()) return

        val newHand = player.hand.filterNot { it in cardsToPlay }
        val newPlayedFood = player.playedFood[color].orEmpty() + cardsToPlay

        val updatedPlayer = player.copy(
            hand = newHand,
            playedFood = player.playedFood + (color to newPlayedFood)
        )

        val updatedPlayers = currentState.players.map { if (it.id == playerId) updatedPlayer else it }
        var newState = currentState.copy(players = updatedPlayers)

        // Recalcula la posesión del mapache para el color afectado
        newState = recalculateRaccoonHolder(newState, color)

        val nextPhase = getNextPhaseAfterFood(updatedPlayer)
        _gameState.value = newState.copy(turnPhase = nextPhase)
    }

    /**
     * Procesa la acción de un jugador de saltar la fase de jugar comida.
     */
    fun onSkipPlayFood() {
        val currentState = _gameState.value
        if (currentState.turnPhase != TurnPhase.PLAY_FOOD) return
        val player = currentState.players.find { it.id == currentState.currentPlayerId } ?: return
        val nextPhase = getNextPhaseAfterFood(player)
        _gameState.value = currentState.copy(turnPhase = nextPhase)
    }

    /**
     * Procesa la acción de un jugador de usar una carta de oso.
     *
     * @param playerId El ID del jugador que usa la carta de oso.
     * @param bearCardId El ID de la carta de oso utilizada.
     * @param targetPlayerId El ID del jugador objetivo.
     * @param targetColor El color de la pila de comida del jugador objetivo.
     */
    fun onUseBear(
        playerId: String,
        bearCardId: Int,
        targetPlayerId: String,
        targetColor: CardColor
    ) {
        var currentState = _gameState.value
        if (playerId != currentState.currentPlayerId || currentState.turnPhase != TurnPhase.USE_BEAR) return

        val player = currentState.players.find { it.id == playerId } ?: return

        val bearCardInHand = player.hand.find { it.id == bearCardId && it is Card.BearCard }
        if (bearCardInHand == null) {
            println("SECURITY: Player $playerId tried to use a bear card they don't have (ID: $bearCardId).")
            return
        }

        val targetPlayer = currentState.players.find { it.id == targetPlayerId } ?: run {
            println("SECURITY: Player $playerId targeted a non-existent player $targetPlayerId.")
            return
        }

        val targetPile = targetPlayer.playedFood[targetColor].orEmpty()
        if (targetPile.isEmpty()) return

        val cardToEat = targetPile.maxByOrNull { it.value } ?: return

        val newHand = player.hand - bearCardInHand
        val updatedPlayer = player.copy(hand = newHand)

        val newTargetPile = targetPile.minus(cardToEat)
        val updatedTargetPlayer = targetPlayer.copy(
            playedFood = targetPlayer.playedFood + (targetColor to newTargetPile)
        )

        val updatedPlayers = currentState.players.map {
            when (it.id) {
                playerId -> updatedPlayer
                targetPlayerId -> updatedTargetPlayer
                else -> it
            }
        }
        var newState = currentState.copy(players = updatedPlayers)

        // Recalcula la posesión del mapache tras la pérdida de la carta por el ataque del oso
        newState = recalculateRaccoonHolder(newState, targetColor)

        _gameState.value = newState.copy(turnPhase = TurnPhase.DRAW_AND_SCORE)
    }

    /**
     * Procesa la acción de un jugador de saltar la fase de usar oso.
     *
     * @param playerId El ID del jugador que realiza la acción.
     */
    fun onSkipBear(playerId: String) {
        val currentState = _gameState.value
        if (playerId != currentState.currentPlayerId || currentState.turnPhase != TurnPhase.USE_BEAR) return
        _gameState.value = currentState.copy(turnPhase = TurnPhase.DRAW_AND_SCORE)
    }

    /**
     * Procesa la acción de un jugador de robar cartas y puntuar, finalizando su turno.
     */
    fun onDrawAndScore() {
        val currentState = _gameState.value
        val playerId = currentState.currentPlayerId
        if (playerId != currentState.currentPlayerId || currentState.turnPhase != TurnPhase.DRAW_AND_SCORE) return

        val player = currentState.players.find { it.id == playerId }!!
        val cardsToDrawCount = (5 - player.hand.size).coerceAtLeast(0)

        if (cardsToDrawCount > 0 && currentState.drawDeck.isEmpty()) {
            println("GAME OVER: Draw deck is empty and player needs to draw.")
            val maxScore = currentState.players.maxOfOrNull { it.scorePile.size } ?: 0
            val winners = currentState.players.filter { it.scorePile.size == maxScore }
            val winnerId = if (winners.size == 1) winners.first().id else null
            _gameState.value = currentState.copy(status = GameStatus.GAME_OVER, winnerId = winnerId)
            return
        }

        if (cardsToDrawCount == 0) {
            val currentPlayerIndex = currentState.players.indexOfFirst { it.id == playerId }
            val nextPlayerIndex = (currentPlayerIndex + 1) % currentState.players.size
            val nextPlayerId = currentState.players[nextPlayerIndex].id
            _gameState.value = currentState.copy(currentPlayerId = nextPlayerId, turnPhase = TurnPhase.PLAY_FOOD)
            return
        }

        val currentDeck = currentState.drawDeck
        val cardsDrawn = currentDeck.take(cardsToDrawCount)
        val remainingDeck = currentDeck.drop(cardsToDrawCount)
        val playerRaccoons = currentState.raccoonHolders.filter { it.value == playerId }.keys

        val (scoringCards, handCards) = cardsDrawn.partition { card ->
            card is Card.FoodCard && card.color in playerRaccoons
        }

        val finalHand = player.hand + handCards
        val finalScorePile = player.scorePile + scoringCards.filterIsInstance<Card.FoodCard>()

        val updatedPlayer = player.copy(hand = finalHand, scorePile = finalScorePile)
        val updatedPlayers = currentState.players.map { if (it.id == playerId) updatedPlayer else it }

        if (finalScorePile.size >= gameConfig.winScore) {
            _gameState.value = currentState.copy(
                players = updatedPlayers,
                drawDeck = remainingDeck,
                status = GameStatus.GAME_OVER,
                winnerId = playerId
            )
            return
        }

        val currentPlayerIndex = updatedPlayers.indexOfFirst { it.id == playerId }
        val nextPlayerIndex = (currentPlayerIndex + 1) % updatedPlayers.size
        val nextPlayerId = updatedPlayers[nextPlayerIndex].id

        _gameState.value = currentState.copy(
            players = updatedPlayers,
            drawDeck = remainingDeck,
            currentPlayerId = nextPlayerId,
            turnPhase = TurnPhase.PLAY_FOOD
        )
    }

    /**
     * Determina la siguiente fase del turno después de la fase de jugar comida.
     * Si el jugador tiene cartas de oso, la siguiente fase es [TurnPhase.USE_BEAR], de lo contrario, es [TurnPhase.DRAW_AND_SCORE].
     *
     * @param player El jugador actual.
     * @return La siguiente [TurnPhase].
     */
    private fun getNextPhaseAfterFood(player: Player): TurnPhase {
        return if (player.hand.any { it is Card.BearCard }) {
            TurnPhase.USE_BEAR
        } else {
            TurnPhase.DRAW_AND_SCORE
        }
    }

    /**
     * Recalcula la posesión del mapache para un color específico de acuerdo con las reglas oficiales:
     * - El jugador con la mayor suma acumulada de valores en su pila de comida para ese color toma el mapache.
     * - Si existe un empate en la puntuación máxima o si ningún jugador tiene comida jugada de dicho color,
     *   el mapache permanece o regresa al centro de la mesa (sin dueño).
     * - Si la posesión cambia de manos, el dueño anterior pierde todas las cartas de comida jugadas de ese color.
     *
     * @param state El estado actual del juego.
     * @param color El [CardColor] a evaluar.
     * @return Una nueva instancia inmutable de [GameState] con la posesión y las pilas actualizadas.
     */
     private fun recalculateRaccoonHolder(state: GameState, color: CardColor): GameState {
        val scores = state.players.associate { player ->
            player.id to (player.playedFood[color]?.sumOf { it.value } ?: 0)
        }

        val maxScore = scores.values.maxOrNull() ?: 0

        val newHolderId = if (maxScore > 0) {
            val playersWithMaxScore = scores.filter { it.value == maxScore }.keys
            if (playersWithMaxScore.size == 1) {
                playersWithMaxScore.first()
            } else {
                // En caso de empate en la puntuación más alta, el mapache queda sin dueño
                null
            }
        } else {
            null
        }

        var finalPlayers = state.players
        val previousHolderId = state.raccoonHolders[color]

        // Si el dueño cambió y existía un poseedor previo, este pierde su pila de dicho color
        if (previousHolderId != null && previousHolderId != newHolderId) {
            finalPlayers = state.players.map { player ->
                if (player.id == previousHolderId) {
                    player.copy(playedFood = player.playedFood + (color to emptyList()))
                } else {
                    player
                }
            }
        }

        return state.copy(
            players = finalPlayers,
            raccoonHolders = state.raccoonHolders + (color to newHolderId)
        )
    }

    /**
     * Crea el estado inicial del juego, incluyendo la baraja, los jugadores y la configuración inicial.
     *
     * @return El [GameState] inicial.
     */
    private fun createInitialGameState(): GameState {
        var cardId = 0
        val deck = mutableListOf<Card>()

        CardColor.entries.forEach { color ->
            repeat(10) { deck.add(Card.FoodCard(id = cardId++, color = color, value = 1)) }
            repeat(5) { deck.add(Card.FoodCard(id = cardId++, color = color, value = 2)) }
            repeat(4) { deck.add(Card.FoodCard(id = cardId++, color = color, value = 3)) }
        }

        repeat(13) { deck.add(Card.BearCard(id = cardId++)) }

        val shuffledDeck = deck.shuffled().toMutableList()

        val players = playersInfo.map { (playerId, nickname) ->
            val hand = shuffledDeck.take(5)
            shuffledDeck.removeAll(hand)
            Player(
                id = playerId,
                name = nickname,
                hand = hand,
                playedFood = CardColor.entries.associateWith { emptyList() },
                scorePile = emptyList()
            )
        }

        return GameState(
            players = players,
            drawDeck = shuffledDeck,
            raccoonHolders = CardColor.entries.associateWith { null },
            currentPlayerId = players.firstOrNull()?.id ?: "",
            turnPhase = TurnPhase.PLAY_FOOD
        )
    }
}
