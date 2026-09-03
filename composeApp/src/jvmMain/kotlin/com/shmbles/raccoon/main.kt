package com.shmbles.raccoon


import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.shmbles.raccoon.component.DefaultRootComponent

/**
 * Punto de entrada para la ejecución de escritorio en la JVM.
 * Inicializa el contexto de Decompose con un [LifecycleRegistry] dedicado y despliega la ventana principal.
 */
fun main() = application {
    val root = DefaultRootComponent(
        componentContext = DefaultComponentContext(LifecycleRegistry())
    )

    Window(onCloseRequest = ::exitApplication, title = "Raccoon KMP") {
        App(root)
    }
}