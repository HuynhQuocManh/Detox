package com.example.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.model.InstalledApp
import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun loadInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }

        val ownPackage = context.packageName
        val seenPackages = HashSet<String>()
        val collator = Collator.getInstance(Locale("vi", "VN")).apply {
            strength = Collator.PRIMARY
        }

        resolveInfos
            .asSequence()
            .filter { info ->
                val pkg = info.activityInfo?.packageName
                pkg != null && pkg != ownPackage && seenPackages.add(pkg)
            }
            .map { info ->
                val label = info.loadLabel(packageManager)?.toString()?.trim()
                    .takeUnless { it.isNullOrEmpty() }
                    ?: info.activityInfo.packageName
                val icon = runCatching { info.loadIcon(packageManager) }.getOrNull()
                InstalledApp(
                    label = label,
                    packageName = info.activityInfo.packageName,
                    activityName = info.activityInfo.name,
                    icon = icon
                )
            }
            .sortedWith { a, b -> collator.compare(a.label, b.label) }
            .toList()
    }

    fun detectEssentialPackages(apps: List<InstalledApp>): Set<String> {
        val keywords = listOf(
            "dialer",
            "phone",
            "telecom",
            "messaging",
            "mms",
            "contacts",
            "deskclock",
            "clock",
            "calendar",
            "settings"
        )
        return apps
            .filter { app ->
                val pkgLower = app.packageName.lowercase(Locale.US)
                keywords.any { kw -> pkgLower.contains(kw) }
            }
            .take(5)
            .map { it.packageName }
            .toSet()
    }

    fun launchApp(app: InstalledApp): Boolean {
        return runCatching {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
                ?: Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    component = ComponentName(app.packageName, app.activityName)
                }
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        }.getOrDefault(false)
    }

    fun isDefaultHomeLauncher(): Boolean {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(
                homeIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return resolved?.activityInfo?.packageName == context.packageName
    }
}
