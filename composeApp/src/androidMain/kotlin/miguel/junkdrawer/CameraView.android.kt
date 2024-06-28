package miguel.junkdrawer

import AndroidStorableImage
import PictureData
import PlatformStorableImage
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import miguel.junkdrawer.icon.IconPhotoCamera
import miguel.junkdrawer.network.vision
import miguel.junkdrawer.view.CircularButton
import miguel.junkdrawer.view.ScalableImage
import miguel.junkdrawer.view.ScalableState
import toAndroidBitmap
import toImageBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue


private val executor = Executors.newSingleThreadExecutor()

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun CameraView(
    modifier: Modifier,
    onCapture: (picture: PictureData) -> Unit
) {
    println("android "+File(".").absolutePath)
    val cameraPermissionState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.CAMERA,
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        Text(
            "${cameraPermissionState.allPermissionsGranted}",
            modifier = Modifier.align(Alignment.Center)
        )
    }


    if (cameraPermissionState.allPermissionsGranted) {
        CameraWithGrantedPermission(modifier, onCapture)
    } else {
        LaunchedEffect(Unit) {
            cameraPermissionState.launchMultiplePermissionRequest()
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun CameraWithGrantedPermission(
    modifier: Modifier,
    onCapture: (picture: PictureData) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build() }
    var isFrontCamera by rememberSaveable { mutableStateOf(false) }
    val cameraSelector = remember(isFrontCamera) {
        val lensFacing =
            if (isFrontCamera) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
        CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    LaunchedEffect(isFrontCamera) {
        cameraProvider = suspendCoroutine<ProcessCameraProvider> { continuation ->
            ProcessCameraProvider.getInstance(context).also { cameraProvider ->
                cameraProvider.addListener({
                    continuation.resume(cameraProvider.get())
                }, executor)
            }
        }
        cameraProvider?.unbindAll()
        cameraProvider?.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    var capturePhotoStarted by remember { mutableStateOf(false) }

    Box(modifier = modifier.pointerInput(isFrontCamera) {
        detectHorizontalDragGestures { change, dragAmount ->
            if (dragAmount.absoluteValue > 50.0) {
                isFrontCamera = !isFrontCamera
            }
        }
    }) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        CircularButton(
            imageVector = IconPhotoCamera,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(36.dp),
            enabled = !capturePhotoStarted,
        ) {
            capturePhotoStarted = true
            imageCapture.takePicture(executor, object : OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val byteArray: ByteArray = getScaledImage(image.planes[0].buffer.toByteArray(), 512, 512)

                    image.close()
                    onCapture(
                        PictureData(
                            "name",
                            System.currentTimeMillis(),
                            AndroidStorableImage(byteArray)
                        )
                    )
                    capturePhotoStarted = false
                }
            })
        }
        if (capturePhotoStarted) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center),
                color = Color.White.copy(alpha = 0.7f),
                strokeWidth = 8.dp,
            )
        }
    }
}

private fun getScaledImage(originalImage: ByteArray, newWidth: Int, newHeight: Int): ByteArray {

    // Get the bitmap from byte array since, the bitmap has the the resize function
    val bitmapImage = originalImage.toAndroidBitmap()

    // New bitmap with the correct size, may not return a null object
    val mutableBitmapImage = Bitmap.createScaledBitmap(bitmapImage, newWidth, newHeight, false)

    // Get the byte array from tbe bitmap to be returned
    val outputStream = ByteArrayOutputStream()
    mutableBitmapImage.compress(Bitmap.CompressFormat.PNG, 0, outputStream)

    if (mutableBitmapImage != bitmapImage) {
        mutableBitmapImage.recycle()
    } // else they are the same, just recycle once


    bitmapImage.recycle()
    return outputStream.toByteArray()
}

private fun ByteBuffer.toByteArray(): ByteArray {
    rewind()    // Rewind the buffer to zero
    val data = ByteArray(remaining())
    get(data)   // Copy the buffer into a byte array
    return data // Return the byte array
}

sealed interface LoadingState {
    data object IDLE : LoadingState
    data object LOADING : LoadingState

    class LOADED(val result: String) : LoadingState
}

@Composable
actual fun ImageViewer(modifier: Modifier, platformStorableImage: PlatformStorableImage) {
    val scalableState = remember { ScalableState() }
    val coroutineScope = rememberCoroutineScope()
    var loaded: LoadingState by remember { mutableStateOf(LoadingState.IDLE) }
    Box(modifier = modifier) {
        ScalableImage(
            scalableState,
            platformStorableImage.byteArray.toImageBitmap(),
            modifier = modifier
                .fillMaxSize()
                .clipToBounds(),
        )
        when (loaded) {
            LoadingState.IDLE -> {
                Button(onClick = {
                    loaded = LoadingState.LOADING
                    coroutineScope.launch(Dispatchers.IO) {
                        loaded = LoadingState.LOADED(vision(Base64.encodeToString(platformStorableImage.byteArray, Base64.NO_WRAP)))
                    }
                }) {
                    Text("Analyze")
                }
            }

            LoadingState.LOADING -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center),
                    color = Color.White.copy(alpha = 0.7f),
                    strokeWidth = 8.dp,
                )
            }
            is LoadingState.LOADED -> {
                Text(
                    (loaded as LoadingState.LOADED).result,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    style = TextStyle(background = Color.White)
                )
            }
        }

    }
}
