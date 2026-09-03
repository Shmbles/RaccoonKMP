package com.shmbles.raccoon

import android.os.Build

/**
 * Implementación de [Platform] para el sistema operativo Android.
 */
actual class Platform actual constructor() {
    actual val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = Platform()