package com.shmbles.raccoon

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.shmbles.raccoon.component.RootComponent
import com.shmbles.raccoon.ui.GameScreen
import com.shmbles.raccoon.ui.LobbyScreen
import com.shmbles.raccoon.ui.LocalGameSetupScreen
import com.shmbles.raccoon.ui.MainMenuScreen
import com.shmbles.raccoon.ui.SplashScreen
import com.shmbles.raccoon.ui.theme.RaccoonTheme

/**
 * Root Compose entrypoint for the Raccoon application.
 *
 * Renders the active Decompose [RootComponent.Child] destination within the theme,
 * animating transitions with cross-platform stack fade effects.
 *
 * @param root The root Decompose component managing navigation and child stack state.
 */
@Composable
internal fun App(root: RootComponent) {
    RaccoonTheme {
        Children(
            stack = root.childStack,
            animation = stackAnimation(fade()),
        ) {
            when (val child = it.instance) {
                is RootComponent.Child.SplashChild -> SplashScreen(child.component)
                is RootComponent.Child.MainMenuChild -> MainMenuScreen(child.component)
                is RootComponent.Child.LocalGameSetupChild -> LocalGameSetupScreen(child.component)
                is RootComponent.Child.LobbyChild -> LobbyScreen(child.component)
                is RootComponent.Child.GameChild -> GameScreen(child.component)
            }
        }
    }
}