package com.shmbles.raccoon.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.shmbles.raccoon.engine.GameEngine
import com.shmbles.raccoon.model.*
import com.shmbles.raccoon.network.ClientAction
import com.shmbles.raccoon.network.NetworkClient
import com.shmbles.raccoon.network.ServerEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * Configuración específica para una partida local (Pass and Play).
 *
 * @param config La configuración general del juego.
 * @param players La lista de jugadores con sus IDs y apodos.
 */
@Serializable
data class LocalGameConfig(
    val config: GameConfig,
    val players: List<Pair<String, String>>
)

/**
 * Implementación por defecto de [GameComponent].
 *
 * Este componente adapta su comportamiento según el [gameMode]:
 * - **ONLINE**: Escucha los eventos de estado del [NetworkClient] y envía las acciones del jugador al servidor.
 * - **PASS_AND_PLAY**: Instancia un [GameEngine] local y lo utiliza como única fuente de verdad, sin comunicación de red.
 *
 * @param componentContext El contexto del componente proporcionado por Decompose.
 * @param gameMode El modo de juego actual.
 * @param localGameConfig La configuración para una partida local, si aplica.
 * @param isHost Indica si el jugador actual es el anfitrión (relevante para el modo online).
 * @param onlinePlayerId El ID del jugador en partidas online.
 * @param onReturnToLobby Función lambda que se invoca para navegar de vuelta al lobby.
 */
class DefaultGameComponent(
    componentContext: ComponentContext,
    private val gameMode: GameMode,
    private val localGameConfig: LocalGameConfig? = null,
    override val isHost: Boolean,
    private val onlinePlayerId: String? = null,
    private val onReturnToLobby: () -> Unit
) : GameComponent, ComponentContext by componentContext {

    private var localGameEngine: GameEngine? = null

    private val _gameState = MutableStateFlow(createEmptyGameState())
    override val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _selectedBearCardId = MutableStateFlow<Int?>(null)
    override val selectedBearCardId: StateFlow<Int?> = _selectedBearCardId.asStateFlow()

    private val _isTurnTransitionVisible = MutableStateFlow(false)
    override val isTurnTransitionVisible: StateFlow<Boolean> = _isTurnTransitionVisible.asStateFlow()

    private val _scoreAnimationTrigger = MutableStateFlow(0)
    override val scoreAnimationTrigger: StateFlow<Int> = _scoreAnimationTrigger.asStateFlow()

    override val myPlayerId: String?
        get() = if (gameMode == GameMode.ONLINE) {
            onlinePlayerId
        } else {
            // En modo local, el jugador actual siempre es el que tiene el turno.
            gameState.value.currentPlayerId
        }

    init {
        val scope = coroutineScope()
        when (gameMode) {
            GameMode.ONLINE -> {
                // En modo online, el estado del juego proviene del servidor.
                scope.launch {
                    NetworkClient.statefulEvents.collect { event ->
                        if (event is ServerEvent.FullGameState) {
                            _gameState.value = event.state
                            _scoreAnimationTrigger.value++
                        }
                    }
                }
                scope.launch {
                    NetworkClient.transientEvents.collect { event ->
                        if (event is ServerEvent.ErrorMessage) {
                            println("Error del Servidor: ${event.message}")
                        }
                    }
                }
            }

            GameMode.PASS_AND_PLAY -> {
                // En modo local, se crea una instancia del motor de juego.
                if (localGameConfig != null) {
                    val engine = GameEngine(localGameConfig.config, localGameConfig.players)
                    localGameEngine = engine
                    scope.launch {
                        engine.gameState.collect { newState ->
                            _gameState.value = newState
                        }
                    }
                    // Muestra la superposición de transición de turno cuando cambia el jugador.
                    gameState
                        .map { it.currentPlayerId }
                        .distinctUntilChanged()
                        .drop(1)
                        .onEach {
                            if (gameState.value.status != GameStatus.GAME_OVER) {
                                _isTurnTransitionVisible.value = true
                            }
                        }
                        .launchIn(scope)
                }
            }
        }

        // Al cambiar de turno, deselecciona cualquier carta de oso.
        gameState
            .map { it.currentPlayerId }
            .distinctUntilChanged()
            .onEach { _selectedBearCardId.value = null }
            .launchIn(scope)
    }

    /**
     * Maneja la acción de volver al lobby.
     * En modo online, el anfitrión notifica al servidor. En modo local, simplemente navega hacia atrás.
     */
    override fun onReturnToLobbyClicked() {
        when (gameMode) {
            GameMode.ONLINE -> {
                if (isHost) {
                    coroutineScope().launch {
                        NetworkClient.sendAction(ClientAction.ReturnToLobby)
                    }
                }
            }
            GameMode.PASS_AND_PLAY -> {
                onReturnToLobby()
            }
        }
    }

    /**
     * Confirma que el jugador ha visto la transición de turno y está listo para continuar (solo en modo local).
     */
    override fun onTurnConfirmed() {
        _isTurnTransitionVisible.value = false
        _scoreAnimationTrigger.value++
    }

    /**
     * Envía la acción de jugar comida al servidor (online) o al motor local.
     */
    override fun onPlayFood(color: CardColor) {
        if (_isTurnTransitionVisible.value) return
        when (gameMode) {
            GameMode.ONLINE -> coroutineScope().launch { NetworkClient.sendAction(ClientAction.PlayFood(color)) }
            GameMode.PASS_AND_PLAY -> localGameEngine?.onPlayFood(myPlayerId!!, color)
        }
    }

    /**
     * Envía la acción de saltar la fase de comida al servidor (online) o al motor local.
     */
    override fun onSkipPlayFood() {
        if (_isTurnTransitionVisible.value) return
        when (gameMode) {
            GameMode.ONLINE -> coroutineScope().launch { NetworkClient.sendAction(ClientAction.SkipPlayFood) }
            GameMode.PASS_AND_PLAY -> localGameEngine?.onSkipPlayFood()
        }
    }

    /**
     * Envía la acción de usar una carta de oso al servidor (online) o al motor local.
     */
    override fun onUseBear(bearCardId: Int, targetPlayerId: String, targetColor: CardColor) {
        if (_isTurnTransitionVisible.value) return
        _selectedBearCardId.value = null
        when (gameMode) {
            GameMode.ONLINE -> coroutineScope().launch {
                NetworkClient.sendAction(ClientAction.UseBear(bearCardId, targetPlayerId, targetColor))
            }
            GameMode.PASS_AND_PLAY -> localGameEngine?.onUseBear(myPlayerId!!, bearCardId, targetPlayerId, targetColor)
        }
    }

    /**
     * Envía la acción de saltar la fase de oso al servidor (online) o al motor local.
     */
    override fun onSkipBear() {
        if (_isTurnTransitionVisible.value) return
        when (gameMode) {
            GameMode.ONLINE -> coroutineScope().launch { NetworkClient.sendAction(ClientAction.SkipBear) }
            GameMode.PASS_AND_PLAY -> localGameEngine?.onSkipBear(myPlayerId!!)
        }
    }

    /**
     * Envía la acción de robar y puntuar al servidor (online) o al motor local.
     */
    override fun onDrawAndScore() {
        if (_isTurnTransitionVisible.value) return
        when (gameMode) {
            GameMode.ONLINE -> coroutineScope().launch { NetworkClient.sendAction(ClientAction.DrawAndScore) }
            GameMode.PASS_AND_PLAY -> localGameEngine?.onDrawAndScore()
        }
    }

    /**
     * Selecciona o deselecciona una carta de oso en la mano del jugador.
     */
    override fun onBearCardSelect(cardId: Int?) {
        if (_isTurnTransitionVisible.value) return
        _selectedBearCardId.value = if (_selectedBearCardId.value == cardId) null else cardId
    }
}

/**
 * Crea un estado de juego vacío inicial.
 */
private fun createEmptyGameState(): GameState {
    return GameState(
        players = emptyList(),
        drawDeck = emptyList(),
        raccoonHolders = emptyMap(),
        currentPlayerId = "",
        status = GameStatus.PLAYING,
        turnPhase = TurnPhase.PLAY_FOOD,
        winnerId = null
    )
}
