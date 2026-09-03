package com.shmbles.raccoon

import platform.UIKit.UIDevice

/**
 * Implementación de [Platform] para iOS mediante UIKit [UIDevice].
 */
actual class Platform {
    actual val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = Platform()