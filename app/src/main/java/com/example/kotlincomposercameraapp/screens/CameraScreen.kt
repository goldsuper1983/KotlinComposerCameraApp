package com.example.kotlincomposercameraapp.screens

import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.kotlincomposercameraapp.R
import java.io.File
//////////////////////////////////////////////////////////////////////////////////////////
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var isCameraPermissionGranted by remember { mutableStateOf(false) }
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            isCameraPermissionGranted = isGranted
            if (!isGranted) {
                Toast.makeText(context, "Camera Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
    /// A surface content any thing in main Screen
    Surface(
        modifier = Modifier.padding(5.dp), color = MaterialTheme.colorScheme.background
    ) {
        if (isCameraPermissionGranted) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                ) {
                    // Display camera preview
                    CameraPreview(context)
                    // Display camera preview end
                }
            }
        } else {
            Text(text = "camera is not granted")
        }
    }
}
//////////////////////////////////////////////////////////////////////////////////////////
@Composable
fun CameraPreview(
    context: Context,
    imageCapture: (ImageCapture?) -> Unit = { }
) {
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    /// PreviewView  is stored in a remember block to make sure it only create one but the camera restart when the cameraSelector changes
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    /// launched effect to make sure that the camera is restart when the cameraSelector change
    LaunchedEffect(cameraSelector) {
        previewView?.let { startCamera(context = context, it, cameraSelector = cameraSelector,imageCapture) }
    }
    /// A box to make the camera Flip Button stay on top of the AndroidView, which come last will stay on top
    Box(){
        /// Start AndroidView
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also {
                    previewView = it
                    startCamera(ctx, it, cameraSelector, imageCapture)
                }
            }
        )
        //////// End AndroidView
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            /// Camera Flip Button
            Button(
                onClick = {
                    cameraSelector =
                        if(cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                        else CameraSelector.DEFAULT_BACK_CAMERA
                },
                enabled = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(100.dp),
                colors = ButtonDefaults.buttonColors(Color.Transparent)
            ) {
                Icon(
                    painter = painterResource(R.drawable.camera_flip),
                    contentDescription = "Camera Flip Button icon",
                    tint = Color.White
                )
            }
            /// Camera Flip Button end
            ///Take Picture Button
            Button(
                onClick = {},
                enabled = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(100.dp),
                colors = ButtonDefaults.buttonColors(Color.Transparent)
            ) {
                Icon(
                painter = painterResource(R.drawable.camera_take_picture),
                contentDescription = "Camera Flip Button icon",
                tint = Color.White
            )
            }
            /// End Take Picture Button
        }

    }
}
///////////////////////////////////////////////////////////////////////
private fun startCamera(
    context: Context,
    previewView: PreviewView,

    cameraSelector: CameraSelector,
    imageCapture: (ImageCapture?) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageCaptureUseCase = ImageCapture.Builder().build()
        try {
            /// unbind before rebinding to prevent crashes
            cameraProvider.unbindAll()
            /// bind the select camera
            cameraProvider.bindToLifecycle(
                context as LifecycleOwner, cameraSelector, preview, imageCaptureUseCase
            )
            imageCapture(imageCaptureUseCase)
        } catch (e: Exception) {
            Log.e("CameraApp", "Use case binding failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}

//////////////////////////////////////////////////////////////////////////////////////////
@Composable
fun CaptureButton(imageCapture: ImageCapture?) {
    val context = LocalContext.current
    Button(
        onClick = {
            val outputDirectory = context.getExternalFilesDir(null)
            val photoFile = File(outputDirectory, "${System.currentTimeMillis()}.jpg")

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture?.takePicture(outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(
                            context, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Toast.makeText(
                            context, "Image saved: ${photoFile.absolutePath}", Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        },
        modifier = Modifier
            .padding(16.dp)
            .size(64.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(Color.Red)
    ) {
        Text(text = "Capture", color = Color.White)
    }
}