package com.shmbles.raccoon.component

import com.arkivanov.decompose.ComponentContext

/**
 * Interfaz para el componente de la Splash Screen.
 * Este componente gestiona la lógica de la pantalla de bienvenida.
 */
interface SplashComponent {
    /**
     * Notifica que el usuario ha decidido navegar al menú principal.
     */
    fun onNavigateToMainMenu()
}

/**
 * Implementación por defecto de [SplashComponent].
 * Invoca el callback `onSplashFinished` cuando se le notifica que debe navegar.
 *
 * @param componentContext El contexto del componente proporcionado por Decompose.
 * @param onSplashFinished La función lambda que se invoca para navegar a la siguiente pantalla.
 */
class DefaultSplashComponent(
    componentContext: ComponentContext,
    private val onSplashFinished: () -> Unit
) : SplashComponent, ComponentContext by componentContext {

    override fun onNavigateToMainMenu() {
        onSplashFinished()
    }
}