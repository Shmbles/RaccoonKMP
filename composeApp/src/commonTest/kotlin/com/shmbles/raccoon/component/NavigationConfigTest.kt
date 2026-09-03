package com.shmbles.raccoon.component

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Batería de pruebas unitarias para validar la serialización y deserialización
 * polimórfica de [NavigationConfig] requerida por Decompose.
 */
class NavigationConfigTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `serialize and deserialize NavigationConfig Splash`() {
        val config: NavigationConfig = NavigationConfig.Splash
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<NavigationConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `serialize and deserialize NavigationConfig MainMenu`() {
        val config: NavigationConfig = NavigationConfig.MainMenu
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<NavigationConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `serialize and deserialize NavigationConfig Lobby`() {
        val config: NavigationConfig = NavigationConfig.Lobby(roomCode = "ROOM1", isHost = true)
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<NavigationConfig>(encoded)
        assertEquals(config, decoded)
    }

    @Test
    fun `serialize and deserialize NavigationConfig Game online`() {
        val config: NavigationConfig = NavigationConfig.Game(
            gameMode = GameMode.ONLINE,
            isHost = false
        )
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<NavigationConfig>(encoded)
        assertEquals(config, decoded)
    }
}
