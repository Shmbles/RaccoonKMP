package com.shmbles.raccoon.ui.values

/**
 * Repositorio centralizado de constantes de texto de la interfaz de usuario.
 */
object R {
    /** Cadenas de caracteres localizadas utilizadas en las pantallas del juego. */
    object string {
        // LobbyScreen
        const val lobby_title = "Sala de Espera"
        const val lobby_code_prefix = "Código: "
        const val lobby_players_title = "Jugadores"
        const val lobby_error_unknown = "Error Desconocido"
        const val lobby_close_error = "Cerrar Error"

        // LobbyConfigView
        const val lobby_config_title = "Configuración"
        const val lobby_config_max_players = "Jugadores Máximos"
        const val lobby_config_win_score = "Puntos para Ganar"
        const val lobby_config_start_game = "¡Iniciar Partida!"
        const val lobby_config_waiting_for_host = "Esperando al Anfitrión..."
        const val lobby_config_waiting_for_players = "Esperando a más Jugadores (mín. 2)..."

        // GameScreen
        const val game_connecting_message = "Conectando y Obteniendo ID..."
        const val game_waiting_for_state_message = "¡Conectado! Esperando Estado del Juego..."
        const val game_turn_action_skip = "Saltar"
        const val game_turn_action_draw_and_finish = "Robar y Terminar"
        const val game_over_title = "¡Fin Del Juego!"
        const val game_over_winner = "Ganador"
        const val game_over_winner_unknown = "Nadie"
        const val game_over_return_to_lobby = "Volver a la Sala"
        const val game_turn_transition_title = "Turno De:"
        const val game_turn_transition_continue = "Continuar"

        // SplashScreen
        const val splash_background_content_description = "Fondo Nocturno"
        const val splash_title_content_description = "Título Guerra de Comida"
        const val splash_characters_content_description = "Personajes Corriendo"
        const val splash_button_text = "¡A Buscar Comida!"

        // MainMenuScreen
        const val main_menu_title = "Guerra de Comida 🍕🍔"
        const val main_menu_local_game_title = "Juego Local"
        const val main_menu_local_game_button = "Pasar y Jugar"
        const val main_menu_online_game_title = "Juego Online"
        const val main_menu_nickname_label = "Tu Nickname"
        const val main_menu_create_room_button = "Crear Sala"
        const val main_menu_room_code_label = "Código"
        const val main_menu_join_button = "Unirse"

        // LocalGameSetupScreen
        const val local_game_setup_title = "Configuración de Partida Local"
        const val local_game_setup_players_title = "Jugadores"
        const val local_game_setup_player_label = "Jugador"
        const val local_game_setup_remove_player_description = "Eliminar Jugador"
        const val local_game_setup_add_player_button = "Añadir Jugador"
        const val local_game_setup_config_title = "Configuración"
        const val local_game_setup_win_score_label = "Puntos para Ganar"
        const val local_game_setup_start_game_button = "¡Iniciar Partida!"
    }
}
