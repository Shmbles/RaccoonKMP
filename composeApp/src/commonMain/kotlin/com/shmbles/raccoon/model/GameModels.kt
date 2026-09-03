package com.shmbles.raccoon.model

import kotlinx.serialization.Serializable

/**
 * Representa los posibles colores de las cartas en el juego.
 */
enum class CardColor {
    YELLOW, PINK, GREEN, BLUE, ORANGE
}

/**
 * Clase sellada que representa una carta en el juego.
 * Puede ser una [FoodCard] o una [BearCard].
 */
@Serializable
sealed class Card {
    /** El identificador único de la carta. */
    abstract val id: Int

    /**
     * Representa una carta de comida.
     * @param id El identificador único de la carta.
     * @param color El color de la carta de comida.
     * @param value El valor numérico de la carta de comida.
     */
    @Serializable
    data class FoodCard(
        override val id: Int,
        val color: CardColor,
        val value: Int
    ) : Card()

    /**
     * Representa una carta de oso.
     * @param id El identificador único de la carta.
     */
    @Serializable
    data class BearCard(
        override val id: Int
    ) : Card()
}

/**
 * Representa un jugador en el juego.
 * @param id El identificador único del jugador.
 * @param name El nombre del jugador.
 * @param hand La lista de cartas en la mano del jugador.
 * @param playedFood Un mapa que contiene las cartas de comida jugadas por color.
 * @param scorePile La lista de cartas en la pila de puntuación del jugador.
 */
@Serializable
data class Player(
    val id: String,
    val name: String,
    val hand: List<Card>,
    val playedFood: Map<CardColor, List<Card.FoodCard>>,
    val scorePile: List<Card.FoodCard>
)

/**
 * Representa el estado actual del juego.
 * @param players La lista de todos los jugadores en la partida.
 * @param drawDeck La lista de cartas restantes en el mazo para robar.
 * @param raccoonHolders Un mapa que indica qué jugador tiene el mapache de cada color.
 * @param currentPlayerId El ID del jugador cuyo turno es actualmente.
 * @param turnPhase La fase actual del turno.
 * @param status El estado general del juego (jugando o terminado).
 * @param winnerId El ID del jugador ganador, si el juego ha terminado.
 */
@Serializable
data class GameState(
    val players: List<Player>,
    val drawDeck: List<Card>,
    val raccoonHolders: Map<CardColor, String?>,
    val currentPlayerId: String,
    val turnPhase: TurnPhase,
    val status: GameStatus = GameStatus.PLAYING,
    val winnerId: String? = null
)

/**
 * Representa el estado general de la partida.
 */
enum class GameStatus {
    PLAYING,
    GAME_OVER
}

/**
 * Representa las diferentes fases dentro de un turno de juego.
 */
enum class TurnPhase {
    PLAY_FOOD,
    USE_BEAR,
    DRAW_AND_SCORE
}

/**
 * Representa la configuración de una partida de juego.
 * @param playerCount El número de jugadores en la partida.
 * @param winScore La puntuación necesaria para ganar la partida.
 */
@Serializable
data class GameConfig(
    val playerCount: Int = 2,
    val winScore: Int = 8
)