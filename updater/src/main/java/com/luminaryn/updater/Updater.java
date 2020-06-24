package com.luminaryn.updater;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.luminaryn.common.Notifications;

public class Updater {
    private Context context;
    private Class broadcastClass;
    private int currentVersionCode;
    private String currentVersionName;

    public Updater (Context context, Class broadcastClass) {
        this.context = context;
        this.broadcastClass = broadcastClass;
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            this.currentVersionName = pinfo.versionName;
            this.currentVersionCode = pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            this.currentVersionName = "0.0.0";
            this.currentVersionCode = 0;
        }
    }

    /**
     * Set the current version code and name.
     * @param verName The friendly name of the version (e.g. '1.2.3')
     * @param verCode The internal code of the version (e.g. 1002003)
     */
    public void setCurrentVersion (String verName, int verCode) {
        currentVersionCode = verCode;
        currentVersionName = verName;
    }

    /**
     * Set the current version name, and generate a version code.
     * @param verName
     */
    public void setCurrentVersion (String verName) {
        String[] ver = verName.split(".", 3);
        int major = Integer.parseInt(ver[0]);
        int minor = Integer.parseInt(ver[1]);
        int patch = Integer.parseInt(ver[2]);
        int verCode = major * 1000000 + minor * 1000 + patch;
        setCurrentVersion(verName, verCode);
    }

    public void checkForUpdates (int newestVersionCode, String newestVersionName, String newestUrl) {
        if (newestVersionCode > currentVersionCode) {
            Notifications not = new Notifications(context, broadcastClass);
        }
    }


}
