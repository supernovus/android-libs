package com.luminaryn.common

import android.app.Application
import android.content.Intent
import android.net.Uri

/**
 * A wrapper around the Application class that adds methods for everything in the AppInfo class.
 */
abstract class AppBase : Application() {
    val id: String
        get() = AppInfo.getAppId(this)

    val versionInfo: AppInfo.VersionInfo
        get() = AppInfo.getVersionInfo(this)
    val versionCode: Int
        get() = versionInfo.code
    val versionName: String
        get() = versionInfo.name

    val permissions: Array<String>
        get() = AppInfo.getPermissions(this)

    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        return AppInfo.isServiceRunning(this, serviceClass)
    }

    @JvmOverloads
    fun getActivityIntent(activityClass: Class<*>, flags: Int? = null, data: Uri? = null): Intent {
        val activityIntent = Intent(this, activityClass)
        if (flags != null) {
            activityIntent.flags = flags
        }
        if (data != null) {
            activityIntent.data = data
        }
        return activityIntent;
    }

    @JvmOverloads
    fun switchActivity(activityClass: Class<*>, flags: Int? = Intent.FLAG_ACTIVITY_NEW_TASK, data: Uri? = null): Intent {
        val activityIntent = getActivityIntent(activityClass, flags, data)
        startActivity(activityIntent)
        return activityIntent
    }

    fun launchApp(packageName: String): Intent? {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        }
        return launchIntent
    }
}