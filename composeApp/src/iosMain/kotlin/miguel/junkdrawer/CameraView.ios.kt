package miguel.junkdrawer

import PictureData
import PlatformStorableImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CameraView(
    modifier: Modifier,
    onCapture: (picture: PictureData) -> Unit
) {

}

@Composable
actual fun ImageViewer(modifier: Modifier, platformStorableImage: PlatformStorableImage) {
}
