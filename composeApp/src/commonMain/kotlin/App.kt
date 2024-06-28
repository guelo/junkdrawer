import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import miguel.junkdrawer.CameraView
import miguel.junkdrawer.ImageViewer
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val imageState = remember { mutableStateOf<PlatformStorableImage?>(null) }
    MaterialTheme {
        Box(Modifier.fillMaxWidth()) {
            if (imageState.value == null) {
                CameraView(Modifier.fillMaxSize(), onCapture = { picture ->
                    imageState.value = picture.platformStorableImage
                })
            } else {
                ImageViewer(Modifier.fillMaxSize(), imageState.value!!)
            }
        }
    }
}
