package com.shmbles.raccoon

import androidx.compose.ui.window.*
import com.arkivanov.decompose.*
import com.arkivanov.essenty.lifecycle.*
import com.shmbles.raccoon.component.*
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.*

/**
 * Factoría que expone el [UIViewController] raíz de la aplicación para ser consumido por el host de iOS en Xcode.
 */
fun MainViewController(): UIViewController = MainUIViewController()

/**
 * Controlador de vista nativo para iOS que sincroniza el ciclo de vida de UIKit con el [LifecycleRegistry]
 * de Essenty y aloja el [ComposeUIViewController] que renderiza la interfaz compartida.
 */
private class MainUIViewController : UIViewController(nibName = null, bundle = null) {
    private val lifecycle = LifecycleRegistry()

    private val root =
        DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
        )

    private var composeViewController: UIViewController? = null

    override fun viewDidLoad() {
        super.viewDidLoad()
        lifecycle.create()

        val controller = ComposeUIViewController { App(root) }
        addChildViewController(controller)
        view.addSubview(controller.view)
        controller.didMoveToParentViewController(this)
        this.composeViewController = controller
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        composeViewController?.view?.setFrame(view.bounds)
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        lifecycle.start()
    }

    override fun viewDidAppear(animated: Boolean) {
        super.viewDidAppear(animated)
        lifecycle.resume()
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        lifecycle.stop()
    }

    override fun viewDidDisappear(animated: Boolean) {
        super.viewDidDisappear(animated)
        lifecycle.destroy()
    }
}
