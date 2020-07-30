package com.luminaryn.common

import android.app.Application

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
}