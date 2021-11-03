package com.luminaryn.common

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
    @Deprecated("getVersionInfo() is a better API", ReplaceWith("getVersionInfo(context).code"))
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
    @Deprecated("getVersionInfo() is a better API", ReplaceWith("getVersionInfo(context).name"))
    fun getVersionName(context: Context): String? {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * Get version info.
     *
     * If this is running on a version of Android older than 9, the longCode will be set to 0.
     *
     * @param context
     * @return A version info object for the current package.
     */
    fun getVersionInfo(context: Context): VersionInfo {
        return try {
            val pi = context.packageManager.getPackageInfo(context.packageName, 0)
            val lc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode else 0L
            VersionInfo(pi.versionName, pi.versionCode, lc)
        } catch (e: PackageManager.NameNotFoundException) {
            VersionInfo("", 0,0L)
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

    /**
     * Version Information
     *
     * @property name The friendly version name shown to end users.
     * @property shortCode The old 'versionCode', may be 0 if on Android 9 or higher.
     * @property longCode The new 'longVersionCode', should be 0 if on less than Android 9.
     * @property code The short versionCode, either returned from shortCode, or the lower 32 bits of longCode.
     * @property majorRevision The majorVersionCode, the higher 32 bits of longCode, or 0.
     *
     * @constructor Build a new VersionInfo object
     * @param shortCode The old 'versionCode', you can set to 0 if on Android 9 or higher.
     * @param longCode The new 'longVersionCode', set this to 0 if on less than Android 9.
     * @param name The friendly version name to show to end users. Must not be null.
     */
    data class VersionInfo (val shortCode: Int, val longCode: Long, val name: String) {

        /**
         * @constructor Build a new VersionInfo object with auto-generated name support.
         * @param name The friendly version name to show to end users. If null or empty, the name property will be auto-generated.
         * @param shortCode The old 'versionCode', you can set to 0 if on Android 9 or higher.
         * @param longCode The new 'longVersionCode', set this to 0 if on less than Android 9.
         */
        constructor(name: String?, shortCode: Int, longCode: Long) : this(shortCode, longCode, getVersionName(name, shortCode, longCode))

        val code: Int = getVerCode(shortCode, longCode)
        val majorRevision: Int = getMajorRevision(longCode)

        companion object {
            private fun getVerCode(shortCode: Int, longCode: Long): Int {
                return if (shortCode > 0) shortCode
                else if (longCode > 0) verCodeFromLong(longCode)
                else 0
            }

            private fun getMajorRevision(longCode: Long): Int {
                return if (longCode > 0) majorRevisionFromLong(longCode)
                else 0
            }

            private fun getVersionName(verName: String?, shortCode: Int, longCode: Long): String {
                return if (verName.isNullOrEmpty())
                    VerInfo.verName(getVerCode(shortCode, longCode))
                else verName
            }

            fun verCodeFromLong(longCode: Long) = (longCode and 0x00000000ffffffff).toInt()
            fun majorRevisionFromLong(longCode: Long) = (longCode shr 32).toInt()
        }

    } // VersionInfo
}