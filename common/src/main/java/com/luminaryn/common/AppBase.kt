package com.luminaryn.common

import android.app.Application
import android.content.Intent

/**
 * A wrapper around the Application class that adds methods for everything in the AppInfo class.
 */
abstract class AppBase : Application() {
    val id: String
        get() = AppInfo.getAppId(this)
    val versionCode: Int
        get() = AppInfo.getVersionCode(this)
    val versionName: String?
        get() = AppInfo.getVersionName(this)
    val permissions: Array<String>
        get() = AppInfo.getPermissions(this)

    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        return AppInfo.isServiceRunning(this, serviceClass)
    }

    fun switchActivity(activityClass: Class<*>, flags: Int = Intent.FLAG_ACTIVITY_NEW_TASK): Intent {
        val activityIntent = Intent(this, activityClass)
        activityIntent.setFlags(flags)
        this.startActivity(activityIntent)
        return activityIntent
    }

    fun launchApp(packageName: String): Intent? {
        val launchIntent = this.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            this.startActivity(launchIntent)
        }
        return launchIntent
    }
}