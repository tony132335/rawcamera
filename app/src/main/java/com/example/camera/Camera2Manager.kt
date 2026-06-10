package com.example.camera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.*
import android.util.Log
import android.util.Size
import android.view.Surface
import com.example.ui.camera.CameraMode
import kotlin.math.max

/**
 * Core Camera2 API Manager responsible for hardware-level parameters, sensor reading,
 * and format selection (like RAW_SENSOR mapping to TIFF).
 */
class Camera2Manager(private val context: Context) {

    private val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null

    // Find closest focus mapping for Macro mode
    fun findMacroCameraId(): String? {
        try {
            var bestMacroId: String? = null
            var minFocusDistance = 0f
            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    val minFocus = chars.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) ?: 0f
                    if (minFocus > minFocusDistance) { // Higher diopter means closer focus capable
                        minFocusDistance = minFocus
                        bestMacroId = id
                    }
                }
            }
            return bestMacroId ?: cameraManager.cameraIdList.firstOrNull()
        } catch (e: Exception) {
            Log.e("Camera2", "Error finding macro camera: ${e.message}")
            return null
        }
    }

    @SuppressLint("MissingPermission")
    fun openCamera(cameraId: String, onOpened: (CameraDevice) -> Unit) {
        try {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    onOpened(camera)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Set up builder specifically with NO post-processing for PRO mode (RAW output simulation)
     */
    fun createProCaptureRequest(targetSurface: Surface, iso: Int, exposureTimeNs: Long, focusDistance: Float): CaptureRequest.Builder? {
        val device = cameraDevice ?: return null
        val builder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        
        // Target surface could be an ImageReader configured for ImageFormat.RAW_SENSOR
        builder.addTarget(targetSurface)

        // Disable all automatic 3A algorithms (Auto Focus, Auto Exposure, Auto White Balance)
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF)
        builder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF)
        builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF)
        builder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_OFF)

        // Disable Noise Reduction and Edge Enhancement (Sharpness) for pure RAW data
        builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_OFF)
        builder.set(CaptureRequest.EDGE_MODE, CameraMetadata.EDGE_MODE_OFF)

        // Set Manual Parameters
        builder.set(CaptureRequest.SENSOR_SENSITIVITY, iso)
        builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exposureTimeNs)
        builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focusDistance) // 0.0f is infinity

        return builder
    }

    fun checkFullFrameSupport(cameraId: String): Boolean {
        val chars = cameraManager.getCameraCharacteristics(cameraId)
        val pixelArraySize = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
        if (pixelArraySize != null) {
            val totalPixels = pixelArraySize.width * pixelArraySize.height
            // If greater than 12MP (12,000,000 pixels), unlock Full Frame / Full Pixel mode.
            if (totalPixels > 12_000_000) {
                return true
            }
        }
        return false
    }

    /**
     * Special mode for Super Moon: Detects brightest points and modifies capture params for moon.
     */
    fun applySuperMoonAlgorithm(builder: CaptureRequest.Builder) {
        // Drop exposure massively so only the moon is visible
        builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF)
        builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, 500_000L) // 0.5ms exposure
        builder.set(CaptureRequest.SENSOR_SENSITIVITY, 50) // Lowest ISO
        
        // Focus at infinity
        builder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF)
        builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 0f)
        
        // P-图与NASA数据对接将在此层（或ImageReader回调层）后处理执行
        // NASA API mock logic
        fetchMoonPhaseAndSuperimpose()
    }

    private fun fetchMoonPhaseAndSuperimpose() {
        // [NASA API对接逻辑占位]
        // val lat = location.latitude
        // val lon = location.longitude
        // val phase = MoonCalculator.getPhase(System.currentTimeMillis())
        // val highResMoon = downloadFromNasa(phase)
        Log.i("SuperMoon", "Fetching high-res moon texture based on GPS/Time and applying via OpenCV overlay.")
    }

    fun startPreview(surface: Surface) {
        val device = cameraDevice ?: return
        try {
            // Because we switch between Pro and Normal modes, for a standard preview we use TEMPLATE_PREVIEW
            val builder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            builder.addTarget(surface)

            // Auto-focus and auto-exposure for default preview
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            
            device.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    try {
                        session.setRepeatingRequest(builder.build(), null, null)
                    } catch (e: Exception) {
                        Log.e("Camera2", "Failed to start camera preview", e)
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e("Camera2", "Failed to configure capture session")
                }
            }, null)
        } catch (e: Exception) {
            Log.e("Camera2", "Exception starting preview", e)
        }
    }

    fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
    }
}
