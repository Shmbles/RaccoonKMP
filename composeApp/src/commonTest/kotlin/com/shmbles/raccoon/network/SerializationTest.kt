package com.shmbles.raccoon.network

import com.shmbles.raccoon.model.CardColor
import com.shmbles.raccoon.model.GameConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Batería de pruebas para verificar que el discriminador de tipos y los módulos
 * polimórficos de Kotlinx Serialization en [NetworkClient.AppJson] funcionen
 * idénticamente en cliente y servidor.
 */
class SerializationTest {

    private val json = NetworkClient.AppJson

    @Test
    fun `serialize and deserialize ClientAction CreateRoom`() {
        val action: ClientAction = ClientAction.CreateRoom(
            config = GameConfig(playerCount = 2, winScore = 21),
            nickname = "Player1"
        )
        val encoded = json.encodeToString(action)
        assertTrue(encoded.contains("\"type\":\"com.shmbles.raccoon.network.ClientAction.CreateRoom\""))

        val decoded = json.decodeFromString<ClientAction>(encoded)
        assertEquals(action, decoded)
    }

    @Test
    fun `serialize and deserialize ClientAction JoinRoom`() {
        val action: ClientAction = ClientAction.JoinRoom(roomCode = "ABCDE", nickname = "Player2")
        val encoded = json.encodeToString(action)
        val decoded = json.decodeFromString<ClientAction>(encoded)
        assertEquals(action, decoded)
    }

    @Test
    fun `serialize and deserialize ClientAction PlayFood`() {
        val action: ClientAction = ClientAction.PlayFood(color = CardColor.YELLOW)
        val encoded = json.encodeToString(action)
        val decoded = json.decodeFromString<ClientAction>(encoded)
        assertEquals(action, decoded)
    }

    @Test
    fun `serialize and deserialize ClientAction UseBear`() {
        val action: ClientAction = ClientAction.UseBear(
            bearCardId = 99,
            targetPlayerId = "player-target",
            targetColor = CardColor.BLUE
        )
        val encoded = json.encodeToString(action)
        val decoded = json.decodeFromString<ClientAction>(encoded)
        assertEquals(action, decoded)
    }

    @Test
    fun `serialize and deserialize ServerEvent RoomCreated`() {
        val event: ServerEvent = ServerEvent.RoomCreated(roomCode = "KMP01")
        val encoded = json.encodeToString(event)
        val decoded = json.decodeFromString<ServerEvent>(encoded)
        assertEquals(event, decoded)
    }

    @Test
    fun `serialize and deserialize ServerEvent LobbyUpdate`() {
        val event: ServerEvent = ServerEvent.LobbyUpdate(
            playerNicknames = listOf("Andres", "Guest"),
            config = GameConfig(playerCount = 2, winScore = 15)
        )
        val encoded = json.encodeToString(event)
        val decoded = json.decodeFromString<ServerEvent>(encoded)
        assertEquals(event, decoded)
    }

    @Test
    fun `serialize and deserialize ServerEvent ErrorMessage`() {
        val event: ServerEvent = ServerEvent.ErrorMessage("Room does not exist")
        val encoded = json.encodeToString(event)
        val decoded = json.decodeFromString<ServerEvent>(encoded)
        assertEquals(event, decoded)
    }
}
