package com.luminaryn.common;

import android.app.Application;
import android.content.pm.PackageManager;
import android.provider.Settings;

/**
 * A wrapper around the Application class that adds methods for everything in the AppInfo class.
 */
public abstract class AppBase extends Application {
    public String getId() {
        return AppInfo.getAppId(this);
    }

    public int getVersionCode() {
        return AppInfo.getVersionCode(this);
    }

    public String getVersionName() {
        return AppInfo.getVersionName(this);
    }

    public String[] getPermissions() {
        return AppInfo.getPermissions(this);
    }
}
