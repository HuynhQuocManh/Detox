package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DetoxScheduleCalculator
import com.example.model.InstalledApp
import com.example.ui.LauncherUiState
import com.example.ui.components.MonochromeAppIcon

@Composable
fun HomeScreen(
    uiState: LauncherUiState,
    onLaunchApp: (InstalledApp) -> Unit,
    onOpenControlCenter: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAllowedApps: () -> Unit,
    onOpenDialer: () -> Unit
) {
    var verticalDragTotal by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { verticalDragTotal = 0f },
                    onDragEnd = {
                        when {
                            verticalDragTotal > 100f -> onOpenControlCenter()
                            verticalDragTotal < -100f -> onOpenAppDrawer()
                        }
                        verticalDragTotal = 0f
                    },
                    onDragCancel = { verticalDragTotal = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        verticalDragTotal += dragAmount
                    }
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // Top bar: Subtle Control Center pull trigger + Date + Battery
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenControlCenter)
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("open_control_center_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = uiState.dateText.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (uiState.controlState.isCharging) {
                            Icons.Filled.BatteryChargingFull
                        } else {
                            Icons.Filled.BatteryStd
                        },
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${uiState.controlState.batteryLevel}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = stringResource(R.string.cd_open_control_center),
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Editorial Giant Clock
            Text(
                text = uiState.clockText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                modifier = Modifier.testTag("home_clock_text")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Detox Schedule Live Status Banner
            DetoxStatusBanner(
                uiState = uiState,
                onClick = onOpenSettings
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section caption
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.scheduleStatus.isDetoxActive) {
                        "ỨNG DỤNG ĐƯỢC PHÉP (${uiState.visibleApps.size})"
                    } else {
                        "ỨNG DỤNG CHÍNH"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.scheduleStatus.isDetoxActive && uiState.hiddenAppsCount > 0) {
                    Text(
                        text = "Đã ẩn ${uiState.hiddenAppsCount} app",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.75.dp)

            // Main app list area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    uiState.isLoadingApps -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Đang tải ứng dụng...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    uiState.homeApps.isEmpty() -> {
                        EmptyDetoxHomeState(
                            isDetoxActive = uiState.scheduleStatus.isDetoxActive,
                            onChooseApps = onOpenAllowedApps
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("home_apps_list"),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(
                                items = uiState.homeApps,
                                key = { it.packageName }
                            ) { app ->
                                HomeAppRow(
                                    app = app,
                                    showIcon = uiState.preferences.showMonochromeIcons,
                                    onClick = { onLaunchApp(app) }
                                )
                            }
                        }
                    }
                }
            }

            // Minimalist Bottom Navigation Dock
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.75.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quick Phone Shortcut
                IconButton(
                    onClick = onOpenDialer,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("home_phone_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = stringResource(R.string.cd_phone_shortcut),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Center Pill: App Drawer trigger
                Surface(
                    onClick = onOpenAppDrawer,
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("open_app_drawer_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Apps,
                            contentDescription = stringResource(R.string.cd_open_app_drawer),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.scheduleStatus.isDetoxActive) {
                                "Cho phép (${uiState.visibleApps.size})"
                            } else {
                                "Tất cả (${uiState.visibleApps.size})"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Detox Settings Shortcut
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.cd_open_settings),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun DetoxStatusBanner(
    uiState: LauncherUiState,
    onClick: () -> Unit
) {
    val status = uiState.scheduleStatus
    val isActive = status.isDetoxActive

    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val secondaryColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val titleText = when {
        status.isScheduledWindowActive -> "ĐANG DETOX · ${status.formattedRange}"
        status.isManualOverride -> "DETOX THỦ CÔNG ĐANG BẬT"
        status.scheduleEnabled -> "BÌNH THƯỜNG · LỊCH ${status.formattedRange}"
        else -> "LỊCH DETOX ĐANG TẮT"
    }

    val subtitleText = when {
        status.isScheduledWindowActive && status.minutesUntilNextTransition > 0 ->
            "Hiện lại tất cả sau ${DetoxScheduleCalculator.formatDuration(status.minutesUntilNextTransition)}"
        status.isManualOverride ->
            "Chỉ hiện ${uiState.visibleApps.size} ứng dụng được chọn"
        status.scheduleEnabled && status.minutesUntilNextTransition > 0 ->
            "Tự động ẩn app sau ${DetoxScheduleCalculator.formatDuration(status.minutesUntilNextTransition)}"
        else ->
            "Đang hiển thị toàn bộ ${uiState.allInstalledApps.size} ứng dụng"
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_detox_status_card"),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Filled.NightsStay else Icons.Outlined.LockClock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = secondaryColor
            )
        }
    }
}

@Composable
private fun HomeAppRow(
    app: InstalledApp,
    showIcon: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showIcon) {
            MonochromeAppIcon(
                drawable = app.icon,
                label = app.label,
                size = 32.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            text = app.label,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EmptyDetoxHomeState(
    isDetoxActive: Boolean,
    onChooseApps: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = if (isDetoxActive) {
                "Không có ứng dụng nào được chọn"
            } else {
                "Chưa tìm thấy ứng dụng"
            },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isDetoxActive) {
                "Trong giờ Detox, mọi ứng dụng không nằm trong danh sách cho phép đều đã được ẩn hoàn toàn."
            } else {
                "Hãy chọn các ứng dụng thiết yếu để ghim lên màn hình chính."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(18.dp))
        Surface(
            onClick = onChooseApps,
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .height(48.dp)
                .testTag("empty_state_choose_apps_button")
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chọn ứng dụng được phép",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
