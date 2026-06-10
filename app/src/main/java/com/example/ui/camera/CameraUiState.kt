package com.example.ui.camera

data class CameraUiState(
    val selectedMode: CameraMode = CameraMode.NORMAL,
    val showProParameters: Boolean = false,
    val flashMode: FlashMode = FlashMode.AUTO,
    val countdown: Int = 0,
    val showGridLine: Boolean = false,
    val showLeveler: Boolean = false,
    val isRecording: Boolean = false,
    // Pro Params
    val iso: String = "Auto",
    val ev: Float = 0f,
    val focusDistance: Float = 0f,
    val whiteBalance: String = "Auto",
    val activeLens: CameraLens = CameraLens.WIDE
)

enum class CameraMode(val title: String) {
    NORMAL("照片"),
    PRO("专业"),
    MACRO("微距"),
    NIGHT("夜景"),
    VIDEO("视频"),
    SUPER_MOON("超级月亮")
}

enum class FlashMode(val iconName: String) {
    OFF("Off"),
    AUTO("Auto"),
    ON("On"),
    TORCH("Torch")
}

enum class CameraLens(val title: String) {
    WIDE("1x"),
    ULTRA_WIDE("0.5x"),
    TELEPHOTO("3x")
}
