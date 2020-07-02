package com.luminaryn.common;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

/**
 * Some helpful static functions for various bits of application info.
 *
 * All of these functions take an app Context instance as their first parameter.
 */
public class AppInfo {

    /**
     * Get the SSAID for this app with this user on this device.
     *
     * @param context
     * @return The unique SSAID
     */
    public static String getAppId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
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
    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) { return 0; }
    }

    /**
     * Get the friendly version name.
     *
     * @param context
     * @return The version name
     */
    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) { return null; }
    }

    /**
     * Get a list of permissions the app has requested.
     *
     * @param context
     * @return An array of permission strings.
     */
    public static String[] getPermissions(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("This should have never happened", e);
        }
    }
}
