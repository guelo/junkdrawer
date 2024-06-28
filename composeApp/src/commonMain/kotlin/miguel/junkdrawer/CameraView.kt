package miguel.junkdrawer

import PictureData
import PlatformStorableImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraView(
    modifier: Modifier,
    onCapture: (picture: PictureData) -> Unit
)

@Composable
expect  fun ImageViewer(
    modifier: Modifier  = Modifier,
    platformStorableImage: PlatformStorableImage
)
