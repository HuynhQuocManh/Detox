package com.example.model

import android.graphics.drawable.Drawable

data class InstalledApp(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable? = null
)
