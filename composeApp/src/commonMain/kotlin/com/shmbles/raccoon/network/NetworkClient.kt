package com.shmbles.raccoon.network

import com.shmbles.raccoon.getPlatform
import com.shmbles.raccoon.model.Card
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Objeto singleton que gestiona la conexión de red y la comunicación con el servidor del juego.
 * Utiliza WebSockets para enviar [ClientAction] y recibir [ServerEvent].
 */
object NetworkClient {

    internal val AppJson = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphic(ServerEvent::class) {
                subclass(ServerEvent.FullGameState::class)
                subclass(ServerEvent.ErrorMessage::class)
                subclass(ServerEvent.YouAre::class)
                subclass(ServerEvent.RoomCreated::class)
                subclass(ServerEvent.LobbyUpdate::class)
                subclass(ServerEvent.GameStarted::class)
                subclass(ServerEvent.NavigateToLobby::class)
                subclass(ServerEvent.ConfigUpdate::class)
            }
            polymorphic(ClientAction::class) {
                subclass(ClientAction.PlayFood::class)
                subclass(ClientAction.SkipPlayFood::class)
                subclass(ClientAction.UseBear::class)
                subclass(ClientAction.SkipBear::class)
                subclass(ClientAction.DrawAndScore::class)
                subclass(ClientAction.CreateRoom::class)
                subclass(ClientAction.JoinRoom::class)
                subclass(ClientAction.StartGame::class)
                subclass(ClientAction.ReturnToLobby::class)
                subclass(ClientAction.UpdateConfig::class)
                subclass(ClientAction.LeaveRoom::class)
            }
            polymorphic(Card::class) {
                subclass(Card.FoodCard::class)
                subclass(Card.BearCard::class)
            }
        }
    }

    private val client = HttpClient {
        install(WebSockets)
        install(ContentNegotiation) {
            json(AppJson)
        }
    }

    private var session: WebSocketSession? = null

    /**
     * [SharedFlow] para eventos de estado que deben ser cacheados (como el estado del juego o del lobby).
     * Tiene un `replay` de 1 para que los nuevos suscriptores reciban el último evento emitido.
     */
    private val _statefulEvents = MutableSharedFlow<ServerEvent>(replay = 1)
    val statefulEvents: SharedFlow<ServerEvent> = _statefulEvents.asSharedFlow()

    /**
     * [SharedFlow] para eventos de una sola vez que NO deben ser cacheados (errores, confirmaciones).
     * No tiene `replay`, por lo que los suscriptores solo reciben eventos emitidos después de su suscripción.
     */
    private val _transientEvents = MutableSharedFlow<ServerEvent>()
    val transientEvents: SharedFlow<ServerEvent> = _transientEvents.asSharedFlow()


    /**
     * Obtiene la dirección IP del host del servidor, adaptada para diferentes plataformas.
     * @return La dirección IP del host.
     */
    private fun getHostIp(): String {
        val platform = getPlatform()
        return when {
            platform.name.contains("Android") -> "10.0.2.2"
            else -> "127.0.0.1"
        }
    }

    /**
     * Establece una conexión WebSocket con el servidor y comienza a escuchar los eventos entrantes.
     * Los eventos recibidos se emiten a través de [statefulEvents] o [transientEvents] según su tipo.
     *
     * @param scope El [CoroutineScope] en el que se lanzará la corrutina de conexión y escucha.
     */
    fun connectAndListen(scope: CoroutineScope) {
        scope.launch {
            try {
                client.webSocket(
                    method = io.ktor.http.HttpMethod.Get,
                    host = getHostIp(),
                    port = 8080,
                    path = "/game"
                ) {
                    session = this
                    println("¡Conectado exitosamente al servidor en ${getHostIp()}!")

                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            try {
                                val event = AppJson.decodeFromString<ServerEvent>(text)
                                when (event) {
                                    is ServerEvent.ErrorMessage,
                                    is ServerEvent.YouAre,
                                    is ServerEvent.RoomCreated,
                                    is ServerEvent.GameStarted,
                                    is ServerEvent.NavigateToLobby -> _transientEvents.emit(event)

                                    is ServerEvent.FullGameState,
                                    is ServerEvent.LobbyUpdate,
                                    is ServerEvent.ConfigUpdate -> _statefulEvents.emit(event)
                                }
                            } catch (e: Exception) {
                                println("Error al deserializar ServerEvent: ${e.message}. Payload: $text")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error en la conexión WebSocket: ${e.message}")
                _transientEvents.emit(ServerEvent.ErrorMessage("Error de conexión: ${e.message}"))
            } finally {
                session = null
                println("Conexión WebSocket cerrada.")
            }
        }
    }

    /**
     * Envía una [ClientAction] al servidor a través de la conexión WebSocket.
     *
     * @param action La [ClientAction] a enviar serializada en JSON.
     */
    suspend fun sendAction(action: ClientAction) {
        val currentSession = session
        if (currentSession == null) {
            println("Error: No conectado al servidor. No se pudo enviar la acción: $action")
            _transientEvents.emit(ServerEvent.ErrorMessage("No conectado al servidor."))
            return
        }
        try {
            val jsonAction = AppJson.encodeToString(action)
            currentSession.send(Frame.Text(jsonAction))
        } catch (e: Exception) {
            println("Error al enviar acción $action: ${e.message}")
        }
    }
}
