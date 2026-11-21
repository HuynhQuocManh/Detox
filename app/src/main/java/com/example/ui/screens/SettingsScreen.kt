package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DetoxScheduleCalculator
import com.example.ui.LauncherUiState
import com.example.ui.components.ScheduleTimelineBar

private enum class TimePickTarget { START, END }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: LauncherUiState,
    onBack: () -> Unit,
    onSetScheduleEnabled: (Boolean) -> Unit,
    onSetStartTime: (Int, Int) -> Unit,
    onSetEndTime: (Int, Int) -> Unit,
    onApplyPreset: (Int, Int, Int, Int) -> Unit,
    onOpenAllowedApps: () -> Unit,
    onOpenHomeLauncherSettings: () -> Unit,
    onSetDarkMode: (Boolean) -> Unit,
    onSetShowMonochromeIcons: (Boolean) -> Unit,
    onSetUse24HourClock: (Boolean) -> Unit
) {
    var activePicker by remember { mutableStateOf<TimePickTarget?>(null) }
    val prefs = uiState.preferences
    val status = uiState.scheduleStatus

    val allowedCount = uiState.allInstalledApps.count { it.packageName in prefs.allowedPackages }
    val hiddenDuringDetoxCount = (uiState.allInstalledApps.size - allowedCount).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigate_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cài đặt Detox",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // SECTION 1: DETOX SCHEDULE
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("LỊCH TỰ ĐỘNG ẨN ỨNG DỤNG")

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Master Schedule Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Bật lịch Detox tự động",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (prefs.scheduleEnabled) {
                                                if (status.isScheduledWindowActive) {
                                                    "Đang trong khung giờ ẩn ứng dụng"
                                                } else {
                                                    "Ngoài khung giờ · Đang hiện tất cả"
                                                }
                                            } else {
                                                "Tạm tắt · Luôn hiện tất cả ứng dụng"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                MonochromeSwitch(
                                    checked = prefs.scheduleEnabled,
                                    onCheckedChange = onSetScheduleEnabled,
                                    modifier = Modifier.testTag("settings_schedule_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Start & End Time Picker Cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TimeSlotCard(
                                    label = "BẮT ĐẦU ẨN",
                                    timeText = DetoxScheduleCalculator.formatTime(
                                        prefs.startHour,
                                        prefs.startMinute
                                    ),
                                    enabled = prefs.scheduleEnabled,
                                    onClick = { activePicker = TimePickTarget.START },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_start_time_button")
                                )

                                TimeSlotCard(
                                    label = "HIỆN LẠI LÚC",
                                    timeText = DetoxScheduleCalculator.formatTime(
                                        prefs.endHour,
                                        prefs.endMinute
                                    ),
                                    enabled = prefs.scheduleEnabled,
                                    onClick = { activePicker = TimePickTarget.END },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_end_time_button")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 24h Monochrome Timeline Visual
                            ScheduleTimelineBar(status = status)

                            Spacer(modifier = Modifier.height(10.dp))

                            val windowDescription = buildString {
                                if (status.isOvernight) {
                                    append("Qua đêm · ")
                                } else if (status.startMinutes == status.endMinutes) {
                                    append("Cả ngày · ")
                                } else {
                                    append("Trong ngày · ")
                                }
                                append(DetoxScheduleCalculator.formatDuration(status.durationMinutes))
                                append(" mỗi ngày")
                            }

                            Text(
                                text = windowDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "MẪU NHANH",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PresetChip(
                                    label = "19:00–07:00",
                                    selected = prefs.startHour == 19 && prefs.startMinute == 0 &&
                                        prefs.endHour == 7 && prefs.endMinute == 0,
                                    onClick = { onApplyPreset(19, 0, 7, 0) },
                                    modifier = Modifier.weight(1f)
                                )
                                PresetChip(
                                    label = "22:00–06:30",
                                    selected = prefs.startHour == 22 && prefs.startMinute == 0 &&
                                        prefs.endHour == 6 && prefs.endMinute == 30,
                                    onClick = { onApplyPreset(22, 0, 6, 30) },
                                    modifier = Modifier.weight(1f)
                                )
                                PresetChip(
                                    label = "08:30–17:30",
                                    selected = prefs.startHour == 8 && prefs.startMinute == 30 &&
                                        prefs.endHour == 17 && prefs.endMinute == 30,
                                    onClick = { onApplyPreset(8, 30, 17, 30) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // SECTION 2: ALLOWED APPS PICKER
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("ỨNG DỤNG ĐƯỢC PHÉP HIỆN")

                    Surface(
                        onClick = onOpenAllowedApps,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_allowed_apps_button"),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Apps,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Chọn ứng dụng được hiện",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Đang chọn $allowedCount ứng dụng · Tự động ẩn $hiddenDuringDetoxCount ứng dụng trong giờ Detox",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // SECTION 3: APPEARANCE & LAUNCHER
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("GIAO DIỆN & HỆ THỐNG")

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                            // Default Launcher Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = onOpenHomeLauncherSettings)
                                    .padding(vertical = 12.dp)
                                    .testTag("settings_default_launcher_button"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (uiState.isDefaultLauncher) {
                                        Icons.Filled.CheckCircle
                                    } else {
                                        Icons.Filled.Home
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Launcher mặc định",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (uiState.isDefaultLauncher) {
                                            "Tĩnh Detox đang là màn hình chính mặc định"
                                        } else {
                                            "Nhấn để đặt Tĩnh Detox làm màn hình chính"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Monochrome Theme Polarity Toggle (Pure Black vs Paper White)
                            Column(modifier = Modifier.padding(vertical = 14.dp)) {
                                Text(
                                    text = "Tông màu trắng đen",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    PolarityButton(
                                        label = "Nền đen thuần",
                                        selected = prefs.isDarkMode,
                                        onClick = { onSetDarkMode(true) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("settings_theme_dark")
                                    )
                                    PolarityButton(
                                        label = "Nền trắng giấy",
                                        selected = !prefs.isDarkMode,
                                        onClick = { onSetDarkMode(false) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("settings_theme_light")
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Grayscale Icons toggle
                            SettingSwitchRow(
                                title = "Hiện biểu tượng đen trắng",
                                subtitle = "Tắt để hiển thị danh sách thuần chữ tối giản",
                                checked = prefs.showMonochromeIcons,
                                onCheckedChange = onSetShowMonochromeIcons,
                                testTag = "settings_icons_switch"
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // 24-hour clock toggle
                            SettingSwitchRow(
                                title = "Đồng hồ 24 giờ",
                                subtitle = "Hiển thị giờ theo chuẩn 00:00 – 23:59",
                                checked = prefs.use24HourClock,
                                onCheckedChange = onSetUse24HourClock,
                                testTag = "settings_24h_switch"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Monochrome Time Picker Dialog
    activePicker?.let { target ->
        val initialHour = if (target == TimePickTarget.START) prefs.startHour else prefs.endHour
        val initialMinute = if (target == TimePickTarget.START) prefs.startMinute else prefs.endMinute
        val pickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { activePicker = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = {
                Text(
                    text = if (target == TimePickTarget.START) {
                        "Giờ bắt đầu ẩn ứng dụng"
                    } else {
                        "Giờ hiện lại tất cả ứng dụng"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(
                        state = pickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                            clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            selectorColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                            timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (target == TimePickTarget.START) {
                            onSetStartTime(pickerState.hour, pickerState.minute)
                        } else {
                            onSetEndTime(pickerState.hour, pickerState.minute)
                        }
                        activePicker = null
                    },
                    modifier = Modifier.testTag("time_picker_confirm")
                ) {
                    Text(
                        text = "Lưu",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { activePicker = null }) {
                    Text(
                        text = "Hủy",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun TimeSlotCard(
    label: String,
    timeText: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun PolarityButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        MonochromeSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun MonochromeSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}
