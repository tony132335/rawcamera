package com.example.ui.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraManager
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.camera.Camera2Manager

@Composable
fun Camera2Preview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val cameraManager = remember { Camera2Manager(context) }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.closeCamera()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            TextureView(ctx).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(
                        outSurface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        val surface = Surface(outSurface)
                        
                        // Pick back camera
                        val systemCameraManager = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                        val cameraId = systemCameraManager.cameraIdList.firstOrNull { id ->
                            val chars = systemCameraManager.getCameraCharacteristics(id)
                            chars.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING) == 
                                android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK
                        } ?: systemCameraManager.cameraIdList.firstOrNull()

                        if (cameraId != null) {
                            cameraManager.openCamera(cameraId) { _ ->
                                cameraManager.startPreview(surface)
                            }
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {}

                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                        cameraManager.closeCamera()
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                }
            }
        }
    )
}
