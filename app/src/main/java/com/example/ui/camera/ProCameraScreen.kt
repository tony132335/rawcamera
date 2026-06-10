package com.example.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProCameraScreen(viewModel: CameraViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Viewport with Top Bar overlapping inside a Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF171717)) // neutral-900
        ) {
            // Live Camera2 API Preview
            Camera2Preview()
            
            // Grid Lines Overlay
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().align(Alignment.Center)) {
                val pathColor = Color.White.copy(alpha = 0.2f)
                val strokeWidth = 1.dp.toPx()
                drawLine(pathColor, androidx.compose.ui.geometry.Offset(size.width / 3, 0f), androidx.compose.ui.geometry.Offset(size.width / 3, size.height), strokeWidth)
                drawLine(pathColor, androidx.compose.ui.geometry.Offset(size.width * 2 / 3, 0f), androidx.compose.ui.geometry.Offset(size.width * 2 / 3, size.height), strokeWidth)
                drawLine(pathColor, androidx.compose.ui.geometry.Offset(0f, size.height / 3), androidx.compose.ui.geometry.Offset(size.width, size.height / 3), strokeWidth)
                drawLine(pathColor, androidx.compose.ui.geometry.Offset(0f, size.height * 2 / 3), androidx.compose.ui.geometry.Offset(size.width, size.height * 2 / 3), strokeWidth)
            }

            // Horizon Leveler
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(1.dp)
                    .background(Color(0xFFFACC15).copy(alpha = 0.8f))
                    .align(Alignment.Center)
            ) {
                Box(modifier = Modifier.align(Alignment.CenterStart).offset(y = (-4).dp).size(width = 4.dp, height = 12.dp).background(Color(0xFFFACC15)))
                Box(modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-4).dp).size(width = 4.dp, height = 12.dp).background(Color(0xFFFACC15)))
            }

            // Interactive Focus Box
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(96.dp)
                    .border(1.dp, Color(0xFFFACC15).copy(alpha = 0.8f), androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
            ) {
                Column(
                    modifier = Modifier.align(Alignment.CenterEnd).offset(x = 48.dp, y = (-20).dp).height(128.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("+0.7", color = Color(0xFFFACC15), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).padding(horizontal = 4.dp))
                    Box(modifier = Modifier.width(4.dp).weight(1f).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                        Box(modifier = Modifier.align(Alignment.TopCenter).offset(y = 24.dp).size(12.dp).clip(CircleShape).background(Color(0xFFFACC15)))
                    }
                    Text("☀️", fontSize = 10.sp)
                }
            }

            // Macro Indicator
            if (uiState.selectedMode == CameraMode.MACRO) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 64.dp)
                        .background(Color(0xFFFFD60A).copy(alpha = 0.2f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("微距", color = Color(0xFFFFD60A), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            TopBar(
                uiState = uiState,
                onFlashToggle = { viewModel.toggleFlash() },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
        ) {
            if (uiState.showProParameters) {
                ProParameterBar(uiState, viewModel)
            }
            ModeSelector(
                currentMode = uiState.selectedMode,
                onModeSelect = { viewModel.setMode(it) }
            )
            BottomControlPanel(
                uiState = uiState,
                onShutterClick = { /* Trigger Capture */ },
                onLensSwitch = { viewModel.toggleLens() }
            )
        }
    }
}

@Composable
fun TopBar(uiState: CameraUiState, onFlashToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // 50 icon styled minimal
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Text("50", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            // HDR icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Text("HDR", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Flash icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onFlashToggle() },
                contentAlignment = Alignment.Center
            ) {
                val icon = when (uiState.flashMode) {
                    FlashMode.OFF -> Icons.Filled.FlashOff
                    FlashMode.AUTO -> Icons.Filled.FlashAuto
                    else -> Icons.Filled.FlashOn
                }
                Icon(icon, contentDescription = "Flash Toggle", tint = if (uiState.flashMode == FlashMode.ON) Color(0xFFFFD60A) else Color.White, modifier = Modifier.size(24.dp))
            }
            // Settings icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun ProParameterBar(uiState: CameraUiState, viewModel: CameraViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.Black)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val params = listOf(
            "ISO" to (if (uiState.iso == "Auto") "自动" else uiState.iso), 
            "快门" to "1/250", 
            "EV" to uiState.ev.toString(), 
            "对焦" to "自动", 
            "白平衡" to "5400K", 
            "镜头" to uiState.activeLens.title
        )
        for ((name, value) in params) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(min = 40.dp)
            ) {
                Text(
                    text = name,
                    color = if (name == "快门") Color(0xFFFFD60A) else Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    color = if (name == "快门") Color(0xFFFFD60A) else Color.White,
                    fontSize = 14.sp,
                    fontFamily = if (value.contains(Regex("[0-9]"))) FontFamily.Monospace else FontFamily.Default,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ModeSelector(currentMode: CameraMode, onModeSelect: (CameraMode) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraMode.values().forEach { mode ->
                if (mode == currentMode) {
                    Text(
                        text = mode.title,
                        color = Color(0xFFFFD60A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onModeSelect(mode) }
                    )
                } else {
                    Text(
                        text = mode.title,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onModeSelect(mode) }
                    )
                }
            }
        }
        // Fades
        Box(modifier = Modifier.align(Alignment.CenterStart).width(48.dp).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color.Black, Color.Transparent))))
        Box(modifier = Modifier.align(Alignment.CenterEnd).width(48.dp).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black))))
    }
}

@Composable
fun BottomControlPanel(uiState: CameraUiState, onShutterClick: () -> Unit, onLensSwitch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .background(Color.Black)
            .padding(horizontal = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .background(Color(0xFF262626))
                .clickable { /* Open Gallery */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PhotoLibrary,
                contentDescription = "Gallery",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }

        // Shutter Button
        val isVideo = uiState.selectedMode == CameraMode.VIDEO
        Box(
            modifier = Modifier
                .size(80.dp)
                .border(4.dp, Color.White, CircleShape)
                .clickable(onClick = onShutterClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(if (isVideo) androidx.compose.foundation.shape.RoundedCornerShape(16.dp) else CircleShape)
                    .background(if (isVideo) Color(0xFFFF3B30) else Color.White)
            )
        }

        // Lens Switch - iOS Style
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onLensSwitch),
            contentAlignment = Alignment.Center
        ) {
            Text(uiState.activeLens.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
