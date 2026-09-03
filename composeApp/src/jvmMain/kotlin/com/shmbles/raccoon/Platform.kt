package com.shmbles.raccoon

/**
 * Implementación de [Platform] para la JVM de escritorio.
 */
actual class Platform actual constructor() {
    actual val name: String = "Desktop (JVM) ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = Platform()