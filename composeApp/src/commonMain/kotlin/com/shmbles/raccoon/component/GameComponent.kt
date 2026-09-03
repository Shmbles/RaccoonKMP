package com.shmbles.raccoon.component

import com.shmbles.raccoon.model.CardColor
import com.shmbles.raccoon.model.GameConfig
import com.shmbles.raccoon.model.GameState
import kotlinx.coroutines.flow.StateFlow

/**
 * Define los modos de juego disponibles.
 */
enum class GameMode {
    ONLINE,
    PASS_AND_PLAY
}

/**
 * Interfaz para el componente de la pantalla de juego.
 * Define el estado observable y las acciones que se pueden realizar durante una partida.
 */
interface GameComponent {
    /** El estado actual del juego. */
    val gameState: StateFlow<GameState>
    /** El ID de la carta de oso seleccionada actualmente por el jugador, o null si ninguna está seleccionada. */
    val selectedBearCardId: StateFlow<Int?>
    /** El ID del jugador actual, o null si no está asignado. */
    val myPlayerId: String?
    /** Indica si el jugador actual es el anfitrión de la partida. */
    val isHost: Boolean
    /** Indica si la transición de turno es visible. */
    val isTurnTransitionVisible: StateFlow<Boolean>
    /** Un disparador de estado para sincronizar animaciones de puntuación. */
    val scoreAnimationTrigger: StateFlow<Int>

    /**
     * Acción para jugar todas las cartas de comida de un color específico.
     * @param color El color de las cartas de comida a jugar.
     */
    fun onPlayFood(color: CardColor)
    /**
     * Acción para saltar la fase de jugar cartas de comida.
     */
    fun onSkipPlayFood()
    /**
     * Acción para usar una carta de oso contra un jugador y una pila de comida específicas.
     * @param bearCardId El ID de la carta de oso que se va a usar.
     * @param targetPlayerId El ID del jugador objetivo.
     * @param targetColor El color de la pila de comida del jugador objetivo.
     */
    fun onUseBear(bearCardId: Int, targetPlayerId: String, targetColor: CardColor)
    /**
     * Acción para saltar la fase de usar una carta de oso.
     */
    fun onSkipBear()
    /**
     * Acción para robar cartas y puntuar, finalizando el turno.
     */
    fun onDrawAndScore()
    /**
     * Acción para volver al lobby desde la partida.
     */
    fun onReturnToLobbyClicked()

    /**
     * Acción para seleccionar o deseleccionar una carta de oso en la mano del jugador.
     * @param cardId El ID de la carta de oso a seleccionar, o null para deseleccionar.
     */
    fun onBearCardSelect(cardId: Int?)
    /**
     * Acción para confirmar que el jugador está listo para el siguiente turno después de una transición.
     */
    fun onTurnConfirmed()
}