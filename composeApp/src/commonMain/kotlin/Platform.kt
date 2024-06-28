import androidx.compose.ui.graphics.ImageBitmap

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform


expect fun createUUID(): String
expect fun ByteArray.toImageBitmap(): ImageBitmap

expect class PlatformStorableImage
