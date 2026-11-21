package com.example.ui.components

import android.media.AudioManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.BluetoothDisabled
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DeviceControlState
import com.example.model.DetoxScheduleStatus
import kotlin.math.roundToInt

@Composable
fun ControlCenterOverlay(
    visible: Boolean,
    clockText: String,
    dateText: String,
    controlState: DeviceControlState,
    scheduleStatus: DetoxScheduleStatus,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onOpenInternet: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onCycleRingerMode: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleManualDetox: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onOpenDisplaySettings: () -> Unit,
    onMediaVolumeChange: (Int) -> Unit,
    onOpenCamera: () -> Unit,
    onOpenAlarms: () -> Unit,
    onOpenDetoxSettings: () -> Unit,
    onOpenSystemSettings: () -> Unit
) {
    if (visible) {
        BackHandler(onBack = onDismiss)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Monochrome dim scrim
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.84f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }

        // Top sliding minimalist monochrome Control Center sheet
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            var accumulatedDrag by remember { mutableFloatStateOf(0f) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (accumulatedDrag < -80f) {
                                    onDismiss()
                                }
                                accumulatedDrag = 0f
                            },
                            onDragCancel = { accumulatedDrag = 0f },
                            onVerticalDrag = { _, dragAmount ->
                                accumulatedDrag += dragAmount
                            }
                        )
                    },
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    // Header row: Clock, Battery, Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "CONTROL CENTER",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = clockText,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Live battery pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (controlState.isCharging) {
                                        Icons.Filled.BatteryChargingFull
                                    } else {
                                        Icons.Filled.BatteryStd
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${controlState.batteryLevel}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("close_control_center_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.cd_close_control_center),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row 1 of monochrome tiles: Wi-Fi & Bluetooth
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val wifiActive = controlState.isWifiConnected || controlState.isWifiEnabled
                        ControlTile(
                            title = "Wi-Fi",
                            subtitle = when {
                                controlState.isWifiConnected -> "Đã kết nối"
                                controlState.isWifiEnabled -> "Đang bật"
                                else -> "Đã tắt"
                            },
                            icon = if (wifiActive) Icons.Filled.Wifi else Icons.Outlined.WifiOff,
                            isActive = wifiActive,
                            onClick = onOpenInternet,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_wifi_tile")
                        )

                        ControlTile(
                            title = "Bluetooth",
                            subtitle = if (controlState.isBluetoothEnabled) "Đang bật" else "Đã tắt",
                            icon = if (controlState.isBluetoothEnabled) {
                                Icons.Filled.Bluetooth
                            } else {
                                Icons.Outlined.BluetoothDisabled
                            },
                            isActive = controlState.isBluetoothEnabled,
                            onClick = onOpenBluetooth,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_bluetooth_tile")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2 of monochrome tiles: Flashlight & Ringer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ControlTile(
                            title = "Đèn pin",
                            subtitle = when {
                                !controlState.hasFlashlight -> "Không hỗ trợ"
                                controlState.isFlashlightOn -> "Đang bật"
                                else -> "Đã tắt"
                            },
                            icon = if (controlState.isFlashlightOn) {
                                Icons.Filled.FlashlightOn
                            } else {
                                Icons.Outlined.FlashlightOff
                            },
                            isActive = controlState.isFlashlightOn,
                            onClick = onToggleFlashlight,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_flashlight_tile")
                        )

                        val (soundLabel, soundIcon, soundActive) = when (controlState.ringerMode) {
                            AudioManager.RINGER_MODE_SILENT -> Triple(
                                "Im lặng",
                                Icons.Filled.NotificationsOff,
                                false
                            )
                            AudioManager.RINGER_MODE_VIBRATE -> Triple(
                                "Chế độ rung",
                                Icons.Filled.Vibration,
                                false
                            )
                            else -> Triple(
                                "Đổ chuông",
                                Icons.Filled.NotificationsActive,
                                true
                            )
                        }
                        ControlTile(
                            title = "Âm thanh",
                            subtitle = soundLabel,
                            icon = soundIcon,
                            isActive = soundActive,
                            onClick = onCycleRingerMode,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_sound_tile")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 3 of monochrome tiles: Monochrome Theme & Quick Detox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ControlTile(
                            title = "Màu nền",
                            subtitle = if (isDarkMode) "Đen thuần" else "Trắng giấy",
                            icon = Icons.Filled.Contrast,
                            isActive = !isDarkMode,
                            onClick = onToggleDarkMode,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_theme_tile")
                        )

                        ControlTile(
                            title = "Detox",
                            subtitle = when {
                                scheduleStatus.isScheduledWindowActive -> "Theo lịch"
                                scheduleStatus.isManualOverride -> "Bật thủ công"
                                else -> "Đang tắt"
                            },
                            icon = if (scheduleStatus.isDetoxActive) {
                                Icons.Filled.NightsStay
                            } else {
                                Icons.Outlined.NightsStay
                            },
                            isActive = scheduleStatus.isDetoxActive,
                            onClick = onToggleManualDetox,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_detox_tile")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Minimal monochrome sliders container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Brightness slider
                        MonochromeSliderRow(
                            icon = Icons.Filled.BrightnessHigh,
                            label = "Độ sáng",
                            valueText = "${(controlState.appBrightness * 100).roundToInt()}%",
                            value = controlState.appBrightness,
                            valueRange = 0.08f..1f,
                            onValueChange = onBrightnessChange,
                            onIconClick = onOpenDisplaySettings,
                            sliderTag = "cc_brightness_slider"
                        )

                        // Media volume slider
                        val volumeFraction = if (controlState.maxMediaVolume > 0) {
                            controlState.mediaVolume.toFloat() / controlState.maxMediaVolume.toFloat()
                        } else {
                            0f
                        }
                        MonochromeSliderRow(
                            icon = Icons.Filled.VolumeUp,
                            label = "Âm lượng",
                            valueText = "${(volumeFraction * 100).roundToInt()}%",
                            value = controlState.mediaVolume.toFloat(),
                            valueRange = 0f..controlState.maxMediaVolume.toFloat(),
                            onValueChange = { onMediaVolumeChange(it.roundToInt()) },
                            onIconClick = onCycleRingerMode,
                            sliderTag = "cc_volume_slider"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom quick utility actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UtilityChip(
                            icon = Icons.Filled.CameraAlt,
                            label = "Máy ảnh",
                            onClick = {
                                onDismiss()
                                onOpenCamera()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_camera_button")
                        )
                        UtilityChip(
                            icon = Icons.Filled.Alarm,
                            label = "Báo thức",
                            onClick = {
                                onDismiss()
                                onOpenAlarms()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_alarm_button")
                        )
                        UtilityChip(
                            icon = Icons.Filled.Tune,
                            label = "Lịch Detox",
                            onClick = {
                                onDismiss()
                                onOpenDetoxSettings()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_detox_settings_button")
                        )
                        UtilityChip(
                            icon = Icons.Filled.Settings,
                            label = "Hệ thống",
                            onClick = {
                                onDismiss()
                                onOpenSystemSettings()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_system_settings_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Swipe-up handle indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(42.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val subtitleColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(66.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(22.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MonochromeSliderRow(
    icon: ImageVector,
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onIconClick: () -> Unit,
    sliderTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onIconClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .weight(1f)
                .testTag(sliderTag),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onSurface,
                activeTrackColor = MaterialTheme.colorScheme.onSurface,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp)
        )
    }
}

@Composable
private fun UtilityChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
