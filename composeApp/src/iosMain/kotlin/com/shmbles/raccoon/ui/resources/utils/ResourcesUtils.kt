package com.shmbles.raccoon.ui.resources.utils

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.*
import kotlinx.cinterop.*
import org.jetbrains.skia.*
import platform.UIKit.*
import platform.posix.*

/**
 * Carga un recurso de imagen desde el bundle nativo de iOS (Assets.xcassets) y lo expone como [Painter].
 * Soporta tanto Data Assets (.webp) como Image Sets tradicionales (.png, .jpg).
 *
 * @param name Nombre del recurso en Assets.xcassets.
 * @return Instancia de [Painter] lista para renderizar en Compose Multiplatform.
 */
@Composable
internal fun imageFromBundle(name: String): Painter {
    val image = remember(name) {
        val dataAsset = NSDataAsset(name = name)
        if (dataAsset != null) {
            UIImage(data = dataAsset.data)
        } else {
            UIImage.imageNamed(name)
        }
    }

    return image?.toImageBitmap()?.let { BitmapPainter(it) }
        ?: error("Image '$name' not found in bundle. Ensure it's in Assets.xcassets as either an Image Set or a Data Asset.")
}

/**
 * Convierte un [UIImage] nativo de iOS a un [ImageBitmap] consumible por Compose Multiplatform a través de Skia.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun UIImage.toImageBitmap(): ImageBitmap {
    val data = UIImagePNGRepresentation(this) ?: error("Could not get PNG representation of UIImage")
    val byteArray = ByteArray(data.length.toInt())

    if (data.length > 0u) {
        val bytes = data.bytes ?: error("Could not get bytes from NSData")
        byteArray.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, data.length)
        }
    }

    return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
}
