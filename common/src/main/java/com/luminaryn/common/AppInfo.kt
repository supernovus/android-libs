package com.luminaryn.common

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

/**
 * Some helpful static functions for various bits of application info.
 *
 * All of these functions take an app Context instance as their first parameter.
 */
object AppInfo {
    /**
     * Get the SSAID for this app with this user on this device.
     *
     * @param context
     * @return The unique SSAID
     */
    @SuppressLint("HardwareIds")
    fun getAppId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver,
                Settings.Secure.ANDROID_ID)
    }

    /**
     * Get the version code.
     *
     * This is using the older now deprecated versionCode property, but as I have existing code
     * and databases using it, I'm not updating everything any time soon.
     *
     * @param context
     * @return The version code
     */
    fun getVersionCode(context: Context): Int {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    /**
     * Get the friendly version name.
     *
     * @param context
     * @return The version name
     */
    fun getVersionName(context: Context): String? {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * Get a list of permissions the app has requested.
     *
     * @param context
     * @return An array of permission strings.
     */
    fun getPermissions(context: Context): Array<String> {
        return try {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException("This should have never happened", e)
        }
    }

    /**
     * Is a specific service running?
     */
    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager;
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className)
            {
                return true;
            }
        }
        return false;
    }
}