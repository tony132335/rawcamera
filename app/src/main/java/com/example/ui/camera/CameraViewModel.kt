package com.example.ui.camera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CameraViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState = _uiState.asStateFlow()

    fun setMode(mode: CameraMode) {
        _uiState.update { it.copy(
            selectedMode = mode,
            showProParameters = mode == CameraMode.PRO
        ) }
    }

    fun toggleFlash() {
        _uiState.update { state ->
            val next = when (state.flashMode) {
                FlashMode.OFF -> FlashMode.AUTO
                FlashMode.AUTO -> FlashMode.ON
                FlashMode.ON -> FlashMode.TORCH
                FlashMode.TORCH -> FlashMode.OFF
            }
            state.copy(flashMode = next)
        }
    }

    fun toggleLens() {
        _uiState.update { state ->
            val next = when (state.activeLens) {
                CameraLens.WIDE -> CameraLens.ULTRA_WIDE
                CameraLens.ULTRA_WIDE -> CameraLens.TELEPHOTO
                CameraLens.TELEPHOTO -> CameraLens.WIDE
            }
            state.copy(activeLens = next)
        }
    }

    fun updateProParam(type: String, value: String) {
        // Handle ISO, EV, focus, WB
    }

    fun toggleRecording() {
        _uiState.update { it.copy(isRecording = !it.isRecording) }
    }
}
