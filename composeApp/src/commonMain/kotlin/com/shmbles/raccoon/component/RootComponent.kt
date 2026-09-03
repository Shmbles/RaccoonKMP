package com.shmbles.raccoon.component

import com.arkivanov.decompose.*
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.*
import com.arkivanov.essenty.lifecycle.coroutines.*
import com.shmbles.raccoon.network.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*

/**
 * Representa las configuraciones de navegación para la pila de pantallas gestionada por Decompose.
 * Cada elemento define el estado y los parámetros requeridos para instanciar su componente correspondiente.
 */
@Serializable
sealed interface NavigationConfig {

    /**
     * Configuración de la pantalla de bienvenida (Splash).
     */
    @Serializable
    data object Splash : NavigationConfig

    /**
     * Configuración del menú principal de selección de modo de juego.
     */
    @Serializable
    data object MainMenu : NavigationConfig

    /**
     * Configuración para la personalización de partidas locales ("Pasar y Jugar").
     */
    @Serializable
    data object LocalGameSetup : NavigationConfig

    /**
     * Configuración para la sala de espera multijugador online.
     *
     * @property roomCode Código alfanumérico único que identifica la sala.
     * @property isHost `true` si el cliente actual es el creador de la sala.
     */
    @Serializable
    data class Lobby(val roomCode: String, val isHost: Boolean) : NavigationConfig

    /**
     * Configuración para la pantalla activa de juego.
     *
     * @property gameMode Modalidad de la partida (Online o Pass and Play).
     * @property isHost `true` si el cliente actual es el anfitrión de la sala online.
     * @property localGameConfig Configuración de jugadores y reglas si se juega en modo local.
     */
    @Serializable
    data class Game(
        val gameMode: GameMode,
        val isHost: Boolean,
        val localGameConfig: LocalGameConfig? = null
    ) : NavigationConfig
}

/**
 * Componente raíz del árbol de componentes de la aplicación.
 * Orquesta la navegación entre pantallas y mantiene la pila jerárquica de hijos activos.
 */
interface RootComponent {

    /**
     * Pila observable de componentes hijos activos expuesta como un [Value] reactivo de Decompose.
     */
    val childStack: Value<ChildStack<*, Child>>

    /**
     * Jerarquía sellada de componentes hijos navegables dentro de la aplicación.
     */
    sealed class Child {
        data class SplashChild(val component: SplashComponent) : Child()
        data class MainMenuChild(val component: MainMenuComponent) : Child()
        data class LocalGameSetupChild(val component: LocalGameSetupComponent) : Child()
        data class LobbyChild(val component: LobbyComponent) : Child()
        data class GameChild(val component: GameComponent) : Child()
    }
}

/**
 * Implementación por defecto de [RootComponent].
 * Gestiona el enrutamiento con [StackNavigation], la escucha reactiva de eventos de red
 * globales y el ciclo de vida de los componentes hijos.
 *
 * @param componentContext Contexto de componente provisto por el contenedor de Decompose.
 */
class DefaultRootComponent(
    componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<NavigationConfig>()
    private val scope = coroutineScope()

    private val _myPlayerId = MutableStateFlow<String?>(null)

    init {
        NetworkClient.connectAndListen(scope)

        scope.launch {
            NetworkClient.transientEvents.collect { event ->
                when (event) {
                    is ServerEvent.YouAre -> {
                        _myPlayerId.value = event.yourPlayerId
                    }

                    is ServerEvent.GameStarted -> {
                        val currentConfig = childStack.value.active.configuration
                        if (currentConfig is NavigationConfig.Lobby) {
                            navigation.push(NavigationConfig.Game(GameMode.ONLINE, currentConfig.isHost))
                        }
                    }

                    is ServerEvent.NavigateToLobby -> {
                        if (childStack.value.active.configuration is NavigationConfig.Game) {
                            navigation.pop()
                        }
                    }

                    else -> {
                        // Eventos adicionales delegados a los componentes hijos
                    }
                }
            }
        }
    }

    /**
     * Notifica al servidor el abandono de la sala actual y extrae la pantalla del lobby de la pila.
     */
    private fun onBackFromLobby() {
        scope.launch {
            NetworkClient.sendAction(ClientAction.LeaveRoom)
            navigation.pop()
        }
    }

    override val childStack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = NavigationConfig.serializer(),
        initialConfiguration = NavigationConfig.Splash,
        handleBackButton = true,
        childFactory = ::createChild
    )

    /**
     * Factoría de componentes hijos basada en la configuración de navegación activa.
     */
    private fun createChild(config: NavigationConfig, context: ComponentContext): RootComponent.Child {
        return when (config) {
            is NavigationConfig.Splash -> RootComponent.Child.SplashChild(
                DefaultSplashComponent(
                    componentContext = context,
                    onSplashFinished = { navigation.replaceCurrent(NavigationConfig.MainMenu) }
                )
            )

            is NavigationConfig.MainMenu -> RootComponent.Child.MainMenuChild(
                DefaultMainMenuComponent(
                    componentContext = context,
                    onNavigateToLobby = { roomCode, isHost ->
                        navigation.push(NavigationConfig.Lobby(roomCode, isHost))
                    },
                    onNavigateToLocalGame = {
                        navigation.push(NavigationConfig.LocalGameSetup)
                    }
                )
            )

            is NavigationConfig.LocalGameSetup -> RootComponent.Child.LocalGameSetupChild(
                DefaultLocalGameSetupComponent(
                    componentContext = context,
                    onStartGame = { localConfig ->
                        navigation.push(NavigationConfig.Game(GameMode.PASS_AND_PLAY, true, localConfig))
                    }
                )
            )

            is NavigationConfig.Lobby -> RootComponent.Child.LobbyChild(
                DefaultLobbyComponent(
                    componentContext = context,
                    roomCode = config.roomCode,
                    isHost = config.isHost,
                    onGameStarted = { /* Sincronizado reactivamente por ServerEvent.GameStarted */ },
                    onBack = ::onBackFromLobby
                )
            )

            is NavigationConfig.Game -> RootComponent.Child.GameChild(
                DefaultGameComponent(
                    componentContext = context,
                    gameMode = config.gameMode,
                    isHost = config.isHost,
                    localGameConfig = config.localGameConfig,
                    onlinePlayerId = _myPlayerId.value,
                    onReturnToLobby = {
                        if (config.gameMode == GameMode.PASS_AND_PLAY) {
                            navigation.pop()
                        } else {
                            scope.launch { NetworkClient.sendAction(ClientAction.ReturnToLobby) }
                        }
                    }
                )
            )
        }
    }
}
