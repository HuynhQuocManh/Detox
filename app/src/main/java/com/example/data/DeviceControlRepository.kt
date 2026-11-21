package com.example.data

import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DeviceControlState(
    val isWifiEnabled: Boolean = false,
    val isWifiConnected: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
    val hasFlashlight: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val ringerMode: Int = AudioManager.RINGER_MODE_NORMAL,
    val mediaVolume: Int = 5,
    val maxMediaVolume: Int = 15,
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val appBrightness: Float = 0.75f
)

class DeviceControlRepository(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    private val torchCameraId: String? = findTorchCameraId()
    private val _state = MutableStateFlow(DeviceControlState(hasFlashlight = torchCameraId != null))
    val state: StateFlow<DeviceControlState> = _state.asStateFlow()

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == torchCameraId) {
                _state.update { it.copy(isFlashlightOn = enabled) }
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId == torchCameraId) {
                _state.update { it.copy(isFlashlightOn = false) }
            }
        }
    }

    init {
        if (torchCameraId != null) {
            runCatching {
                cameraManager?.registerTorchCallback(torchCallback, Handler(Looper.getMainLooper()))
            }
        }
        refresh()
    }

    fun cleanup() {
        runCatching {
            cameraManager?.unregisterTorchCallback(torchCallback)
        }
    }

    fun refresh() {
        val batteryIntent = runCatching {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }.getOrNull()
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val wifiEnabled = runCatching { wifiManager?.isWifiEnabled == true }.getOrDefault(false)
        val wifiConnected = runCatching {
            val activeNetwork = connectivityManager?.activeNetwork
            val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }.getOrDefault(false)

        val btEnabled = runCatching {
            bluetoothManager?.adapter?.isEnabled == true
        }.getOrDefault(false)

        val ringer = runCatching {
            audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        }.getOrDefault(AudioManager.RINGER_MODE_NORMAL)

        val mediaVol = runCatching {
            audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 5
        }.getOrDefault(5)

        val maxMediaVol = runCatching {
            (audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15).coerceAtLeast(1)
        }.getOrDefault(15)

        _state.update { current ->
            current.copy(
                isWifiEnabled = wifiEnabled,
                isWifiConnected = wifiConnected,
                isBluetoothEnabled = btEnabled,
                hasFlashlight = torchCameraId != null,
                ringerMode = ringer,
                mediaVolume = mediaVol,
                maxMediaVolume = maxMediaVol,
                batteryLevel = batteryPct,
                isCharging = charging
            )
        }
    }

    fun toggleFlashlight(): String? {
        val camId = torchCameraId ?: return "Thiết bị không có đèn pin"
        val targetState = !_state.value.isFlashlightOn
        return runCatching {
            cameraManager?.setTorchMode(camId, targetState)
            _state.update { it.copy(isFlashlightOn = targetState) }
            null
        }.getOrElse {
            "Không thể điều khiển đèn pin lúc này"
        }
    }

    fun cycleRingerMode(): String? {
        val manager = audioManager ?: return "Không thể thay đổi chế độ âm thanh"
        val hasDndAccess = notificationManager?.isNotificationPolicyAccessGranted == true
        val nextMode = when (manager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
            AudioManager.RINGER_MODE_VIBRATE -> {
                if (hasDndAccess) AudioManager.RINGER_MODE_SILENT
                else AudioManager.RINGER_MODE_NORMAL
            }
            else -> AudioManager.RINGER_MODE_NORMAL
        }
        return runCatching {
            manager.ringerMode = nextMode
            _state.update { it.copy(ringerMode = manager.ringerMode) }
            null
        }.getOrElse {
            openSoundSettings()
            "Mở cài đặt âm thanh hệ thống"
        }
    }

    fun setMediaVolume(volume: Int) {
        val manager = audioManager ?: return
        val maxVol = _state.value.maxMediaVolume
        val clamped = volume.coerceIn(0, maxVol)
        runCatching {
            manager.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, 0)
            _state.update { it.copy(mediaVolume = clamped) }
        }
    }

    fun setAppBrightness(brightness: Float) {
        val clamped = brightness.coerceIn(0.08f, 1.0f)
        _state.update { it.copy(appBrightness = clamped) }
    }

    fun openInternetPanel(): Boolean {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS)
        }
        return startSafeIntent(intent) || startSafeIntent(Intent(Settings.ACTION_WIFI_SETTINGS))
    }

    fun openBluetoothSettings(): Boolean = startSafeIntent(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))

    fun openDisplaySettings(): Boolean = startSafeIntent(Intent(Settings.ACTION_DISPLAY_SETTINGS))

    fun openSoundSettings(): Boolean = startSafeIntent(Intent(Settings.ACTION_SOUND_SETTINGS))

    fun openSystemSettings(): Boolean = startSafeIntent(Intent(Settings.ACTION_SETTINGS))

    fun openHomeLauncherSettings(): Boolean {
        return startSafeIntent(Intent(Settings.ACTION_HOME_SETTINGS)) ||
            startSafeIntent(Intent(Settings.ACTION_SETTINGS))
    }

    fun openDialer(): Boolean = startSafeIntent(Intent(Intent.ACTION_DIAL))

    fun openCamera(): Boolean {
        return startSafeIntent(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)) ||
            startSafeIntent(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    fun openAlarms(): Boolean {
        return startSafeIntent(Intent(AlarmClock.ACTION_SHOW_ALARMS)) ||
            startSafeIntent(Intent(AlarmClock.ACTION_SET_ALARM))
    }

    private fun startSafeIntent(intent: Intent): Boolean {
        return runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun findTorchCameraId(): String? {
        val manager = cameraManager ?: return null
        return runCatching {
            manager.cameraIdList.firstOrNull { id ->
                val chars = manager.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK
            } ?: manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()
    }
}
