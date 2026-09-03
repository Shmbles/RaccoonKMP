package com.shmbles.raccoon.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.shmbles.raccoon.model.GameConfig
import com.shmbles.raccoon.network.ClientAction
import com.shmbles.raccoon.network.NetworkClient
import com.shmbles.raccoon.network.ServerEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Interfaz para el componente del lobby.
 * Define el estado observable y las acciones que se pueden realizar en la sala de espera multijugador.
 */
interface LobbyComponent {

    /** Código de 5 letras único de la sala. */
    val roomCode: String

    /** Indica si el jugador actual es el anfitrión con privilegios de configuración e inicio. */
    val isHost: Boolean

    /** Flujo reactivo con la lista de apodos de los jugadores actualmente en la sala. */
    val players: StateFlow<List<String>>

    /** Flujo reactivo con mensajes de error no fatales o informativos. */
    val error: StateFlow<String?>

    /** Flujo reactivo con la configuración de reglas acordada para la partida. */
    val config: StateFlow<GameConfig?>

    /** Solicita al servidor el inicio de la partida. Solo aplicable al anfitrión. */
    fun onStartGameClicked()

    /**
     * Actualiza la configuración de la sala en el servidor.
     *
     * @param playerCount Número máximo de jugadores permitidos.
     * @param winScore Puntuación requerida para ganar.
     */
    fun onConfigChanged(playerCount: Int, winScore: Int)

    /** Notifica la salida del usuario del lobby. */
    fun onBackClicked()

    /** Limpia cualquier mensaje de error activo en la interfaz. */
    fun clearError()
}

/**
 * Implementación por defecto de [LobbyComponent].
 * Gestiona la comunicación con el servidor para las funcionalidades del lobby,
 * incluyendo la visualización de errores y la intercepción del botón de retroceso del sistema.
 *
 * @param componentContext Contexto provisto por Decompose.
 * @param roomCode Código de la sala activa.
 * @param isHost `true` si el cliente es el anfitrión.
 * @param onGameStarted Callback invocado cuando la partida ha iniciado.
 * @param onBack Callback invocado para navegar hacia atrás.
 */
class DefaultLobbyComponent(
    componentContext: ComponentContext,
    override val roomCode: String,
    override val isHost: Boolean,
    private val onGameStarted: () -> Unit,
    private val onBack: () -> Unit
) : LobbyComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()

    private val _players = MutableStateFlow<List<String>>(emptyList())
    override val players: StateFlow<List<String>> = _players.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    private val _config = MutableStateFlow<GameConfig?>(null)
    override val config: StateFlow<GameConfig?> = _config.asStateFlow()

    private val backCallback = BackCallback { onBackClicked() }

    init {
        backHandler.register(backCallback)

        scope.launch {
            NetworkClient.statefulEvents.collect { event ->
                when (event) {
                    is ServerEvent.LobbyUpdate -> {
                        _error.value = null
                        _players.value = event.playerNicknames
                        _config.value = event.config
                    }
                    is ServerEvent.ConfigUpdate -> {
                        _config.value = event.newConfig
                    }
                    else -> {
                        // Eventos adicionales ignorados en la vista del lobby
                    }
                }
            }
        }
        scope.launch {
            NetworkClient.transientEvents.collect { event ->
                if (event is ServerEvent.ErrorMessage) {
                    if (event.message.contains("Host ha abandonado")) {
                        _error.value = event.message
                        onBack()
                    } else {
                        _error.value = event.message
                    }
                }
            }
        }
    }

    /**
     * Limpia el mensaje de error actual.
     */
    override fun clearError() {
        _error.value = null
    }

    /**
     * Envía una acción al servidor para iniciar el juego.
     */
    override fun onStartGameClicked() {
        if (!isHost) return
        scope.launch {
            NetworkClient.sendAction(ClientAction.StartGame)
        }
    }

    /**
     * Envía una acción al servidor para actualizar la configuración del juego.
     */
    override fun onConfigChanged(playerCount: Int, winScore: Int) {
        if (!isHost) return
        if (_config.value == null) return

        val newConfig = GameConfig(playerCount, winScore)
        if (newConfig == _config.value) return

        // Actualiza la UI del host de forma optimista. El servidor enviará un LobbyUpdate para confirmar.
        _config.value = newConfig

        scope.launch {
            NetworkClient.sendAction(ClientAction.UpdateConfig(newConfig))
        }
    }

    /**
     * Maneja el clic en el botón "Atrás", llamando al callback proporcionado por el RootComponent.
     */
    override fun onBackClicked() {
        onBack()
    }
}
