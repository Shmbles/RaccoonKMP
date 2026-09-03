package com.shmbles.raccoon.component

import com.arkivanov.decompose.ComponentContext
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
 * Interfaz para el componente del menú principal.
 * Define el estado observable y las acciones que se pueden realizar en el menú principal.
 */
interface MainMenuComponent {
    /** Flujo de estado que contiene un mensaje de error, si lo hay. */
    val error: StateFlow<String?>
    /** Flujo de estado que indica si la aplicación está intentando conectarse a una sala. */
    val isConnecting: StateFlow<Boolean>

    /**
     * Inicia el proceso de creación de una nueva sala online con el apodo especificado.
     *
     * @param nickname El apodo del jugador que crea la sala.
     */
    fun onCreateRoomClicked(nickname: String)

    /**
     * Inicia el proceso de unión a una sala online existente mediante su código de 5 letras.
     *
     * @param roomCode El código de la sala objetivo.
     * @param nickname El apodo del jugador que ingresa a la sala.
     */
    fun onJoinRoomClicked(roomCode: String, nickname: String)

    /**
     * Navega a la configuración de la partida local "Pasar y Jugar".
     */
    fun onLocalGameClicked()
}

/**
 * Implementación por defecto de [MainMenuComponent].
 * Gestiona el estado de la UI del menú principal y orquesta la transición al lobby
 * escuchando los eventos de la red.
 *
 * @param componentContext El contexto del componente proporcionado por Decompose.
 * @param onNavigateToLobby Función lambda que se invoca para navegar a la pantalla del lobby.
 * @param onNavigateToLocalGame Función lambda que se invoca para navegar a la pantalla de configuración de partida local.
 */
class DefaultMainMenuComponent(
    componentContext: ComponentContext,
    private val onNavigateToLobby: (roomCode: String, isHost: Boolean) -> Unit,
    private val onNavigateToLocalGame: () -> Unit
) : MainMenuComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    override val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private var joiningRoomCode: String? = null

    init {
        // Escucha los eventos de la red para manejar el resultado de las acciones de unirse/crear sala.
        scope.launch {
            NetworkClient.transientEvents.collect { event ->
                when (event) {
                    is ServerEvent.RoomCreated -> {
                        if (_isConnecting.value) {
                            _error.value = null
                            _isConnecting.value = false
                            onNavigateToLobby(event.roomCode, true)
                        }
                    }
                    is ServerEvent.YouAre -> {
                        val roomCode = joiningRoomCode
                        if (roomCode != null) {
                            _error.value = null
                            _isConnecting.value = false
                            joiningRoomCode = null
                            onNavigateToLobby(roomCode, false)
                        }
                    }
                    is ServerEvent.ErrorMessage -> {
                        _error.value = event.message
                        _isConnecting.value = false
                        joiningRoomCode = null
                    }
                    else -> { /* Ignora otros eventos transitorios */ }
                }
            }
        }
    }

    /**
     * Maneja el clic en el botón "Crear Sala".
     * Envía una acción [ClientAction.CreateRoom] al servidor.
     * @param nickname El apodo del jugador.
     */
    override fun onCreateRoomClicked(nickname: String) {
        if (nickname.isBlank()) {
            _error.value = "El nickname no puede estar vacío."
            return
        }
        _error.value = null
        _isConnecting.value = true
        scope.launch {
            val defaultConfig = GameConfig(playerCount = 2, winScore = 8)
            NetworkClient.sendAction(ClientAction.CreateRoom(defaultConfig, nickname))
        }
    }

    /**
     * Maneja el clic en el botón "Unirse".
     * Envía una acción [ClientAction.JoinRoom] al servidor.
     * @param roomCode El código de la sala.
     * @param nickname El apodo del jugador.
     */
    override fun onJoinRoomClicked(roomCode: String, nickname: String) {
        if (nickname.isBlank()) {
            _error.value = "El nickname no puede estar vacío."
            return
        }
        if (roomCode.isBlank() || roomCode.length < 5) {
            _error.value = "El código debe tener 5 letras."
            return
        }
        _error.value = null
        _isConnecting.value = true
        joiningRoomCode = roomCode.uppercase()

        scope.launch {
            NetworkClient.sendAction(ClientAction.JoinRoom(roomCode.uppercase(), nickname))
        }
    }

    /**
     * Maneja el clic en el botón "Pasar y Jugar (Local)".
     * Navega a la pantalla de configuración de partida local.
     */
    override fun onLocalGameClicked() {
        onNavigateToLocalGame()
    }
}
