package com.shmbles.raccoon.component

import com.arkivanov.decompose.ComponentContext
import com.shmbles.raccoon.model.GameConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Interfaz para el componente de configuración de una partida local.
 * Define el estado observable y las acciones que se pueden realizar para configurar una partida local.
 */
interface LocalGameSetupComponent {
    /** Flujo de estado que contiene la lista de nombres de los jugadores. */
    val players: StateFlow<List<String>>
    /** Flujo de estado que contiene la configuración actual del juego local. */
    val config: StateFlow<GameConfig>

    /**
     * Actualiza el nombre de un jugador en una posición específica.
     * @param index El índice del jugador cuyo nombre se va a cambiar.
     * @param name El nuevo nombre del jugador.
     */
    fun onPlayerNameChanged(index: Int, name: String)
    /**
     * Añade un nuevo jugador a la lista.
     */
    fun onAddPlayerClicked()
    /**
     * Elimina un jugador de la lista en una posición específica.
     * @param index El índice del jugador a eliminar.
     */
    fun onRemovePlayerClicked(index: Int)
    /**
     * Actualiza la configuración del juego local.
     * @param playerCount El número de jugadores (se ignora en este componente ya que se deriva de la lista de jugadores).
     * @param winScore La nueva puntuación para ganar.
     */
    fun onConfigChanged(playerCount: Int, winScore: Int)
    /**
     * Inicia la partida local con la configuración actual.
     */
    fun onStartGameClicked()
}

/**
 * Implementación por defecto de [LocalGameSetupComponent].
 * Gestiona la adición, eliminación y edición de jugadores, así como la configuración de los puntos para ganar
 * en una partida local.
 *
 * @param componentContext El contexto del componente proporcionado por Decompose.
 * @param onStartGame Función lambda que se invoca cuando se solicita iniciar la partida local,
 *                    pasando la [LocalGameConfig] final.
 */
class DefaultLocalGameSetupComponent(
    componentContext: ComponentContext,
    private val onStartGame: (LocalGameConfig) -> Unit
) : LocalGameSetupComponent, ComponentContext by componentContext {

    private val _players = MutableStateFlow(listOf("Jugador 1", "Jugador 2"))
    override val players: StateFlow<List<String>> = _players.asStateFlow()

    private val _config = MutableStateFlow(GameConfig(playerCount = 2, winScore = 8))
    override val config: StateFlow<GameConfig> = _config.asStateFlow()

    /**
     * Actualiza el nombre de un jugador en la lista.
     * @param index El índice del jugador a actualizar.
     * @param name El nuevo nombre del jugador.
     */
    override fun onPlayerNameChanged(index: Int, name: String) {
        _players.value = _players.value.toMutableList().also { it[index] = name }
    }

    /**
     * Añade un nuevo jugador a la lista, hasta un máximo de 6.
     */
    override fun onAddPlayerClicked() {
        if (_players.value.size < 6) {
            _players.value = _players.value + "Jugador ${_players.value.size + 1}"
            updatePlayerCount()
        }
    }

    /**
     * Elimina un jugador de la lista, manteniendo un mínimo de 2 jugadores.
     * @param index El índice del jugador a eliminar.
     */
    override fun onRemovePlayerClicked(index: Int) {
        if (_players.value.size > 2) {
            _players.value = _players.value.toMutableList().also { it.removeAt(index) }
            updatePlayerCount()
        }
    }

    /**
     * Actualiza la configuración de los puntos para ganar.
     * El `playerCount` se ignora ya que se gestiona automáticamente por la lista de jugadores.
     * @param playerCount El número de jugadores (ignorado).
     * @param winScore La nueva puntuación para ganar.
     */
    override fun onConfigChanged(playerCount: Int, winScore: Int) {
        _config.value = _config.value.copy(winScore = winScore)
    }

    /**
     * Construye la configuración final de la partida local y la pasa al callback `onStartGame`.
     */
    override fun onStartGameClicked() {
        val finalPlayers = _players.value.mapIndexed { index, nickname ->
            "p${index + 1}" to nickname
        }
        val finalConfig = LocalGameConfig(
            config = _config.value,
            players = finalPlayers
        )
        onStartGame(finalConfig)
    }

    /**
     * Actualiza el `playerCount` en la configuración del juego para que coincida con el número actual de jugadores.
     */
    private fun updatePlayerCount() {
        _config.value = _config.value.copy(playerCount = _players.value.size)
    }
}