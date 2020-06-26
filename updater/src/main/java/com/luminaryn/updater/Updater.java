package com.luminaryn.updater;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;

import com.luminaryn.common.LongLog;
import com.luminaryn.common.Notifications;
import com.luminaryn.webservice.JSON;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * A class for building a web service which can install updates.
 */
public class Updater extends JSON {
    private Context context;
    private Class broadcastClass;
    private int currentVersionCode;
    private String currentVersionName;

    /**
     * The logging tag for error messages.
     */
    public String TAG = "com.luminaryn.updater";

    /**
     * The ACTION name for the BroadcastReceiver intent.
     */
    public String ACTION = TAG + ".ACTION_GET_UPDATE";

    /**
     * The file provider name for the cache file.
     * This provider must have access to the Cache Dir.
     */
    public String PROVIDER = TAG + ".fileprovider";

    /**
     * The notification id.
     */
    public int NOTIFICATION_ID = 1;

    /**
     * The notification priority.
     */
    public int PRIO = Notification.PRIORITY_HIGH;

    /**
     * The filename to save in the cache dir.
     */
    public String filename = "latest.apk";

    /**
     * The id of the resource string to get the title for the notification.
     *
     * This MUST be specified in order to show the notification.
     */
    public int notificationTitle;

    /**
     * The id of the resource string to get for the notification message.
     * Must accept two positional parameters, the first being the current app version,
     * and the second being the newest available app version.
     *
     * This MUST be specified in order to show the notification.
     */
    public int notificationMessage;

    /**
     * The resource id for the drawable to use as the notification icon.
     *
     * This MUST be specified in order to show the notification.
     */
    public int notificationIcon;

    /**
     * A URL to download the update information from.
     *
     * This is only required if you are using the version of checkForUpdates() without
     * any parameters.
     */
    public String updatesUrl;

    /**
     * If using the updatesUrl, this is the name of the property that will have the
     * version name (e.g. '1.2.3') in it. This MUST be in the response for it to be valid.
     */
    public String updatesNameKey = "versionName";

    /**
     * If using the updatesUrl, this is the name of the property that will have the
     * version code (e.g. 1002003), if the code isn't found in the response, we'll
     * pass the version name to getCodeFromName().
     */
    public String updatesCodeKey = "versionCode";

    /**
     * If using the updatesUrl, this is the name of the property that will have the
     * URL to the new package. If this isn't included, then you'll need to specify
     * a packageUrl property.
     */
    public String updatesUrlKey = "packageURL";

    /**
     * If you want to use the updatesUrl and have a specific hard coded URL for
     * updates, set this and it'll be used as the default value if the updatesUrlKey
     * property isn't found in the JSON response.
     */
    public String packageUrl;

    /**
     * Build a new Updater and get the current version from the context.
     *
     * @param context The application context.
     * @param broadcastClass The BroadcastReceiver class.
     */
    public Updater (Context context, Class broadcastClass) {
        super();
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
     * Build a new Updater and manually specify the version name and code.
     *
     * @param context The application context.
     * @param broadcastClass The BroadcastReceiver class.
     * @param verName The version name (e.g. '1.2.3')
     * @param verCode The version code (e.g. 1002003)
     */
    public Updater (Context context, Class broadcastClass, String verName, int verCode) {
        super();
        this.context = context;
        this.broadcastClass = broadcastClass;
        currentVersionName = verName;
        currentVersionCode = verCode;
    }

    /**
     * Build a new Updater and manually specify the version name, and
     * generate an automatic version code using getCodeFromName()
     *
     * @param context The application context.
     * @param broadcastClass The BroadcastReceiver class.
     * @param verName The version name (e.g. '1.2.3')
     */
    public Updater (Context context, Class broadcastClass, String verName) {
        super();
        this.context = context;
        this.broadcastClass = broadcastClass;
        currentVersionName = verName;
        currentVersionCode = getCodeFromName(verName);
    }

    /**
     * A quick way to assign the notification title, message, and icon.
     *
     * @param title
     * @param message
     * @param icon
     */
    public void setNotification (int title, int message, int icon) {
        notificationTitle = title;
        notificationMessage = message;
        notificationIcon = icon;
    }

    /**
     * Generate a version code from a version name.
     *
     * This requires a three part version name,
     * e.g. '1.2.3' would become 1002003
     * @param verName
     * @return
     */
    public int getCodeFromName (String verName) {
        String[] ver = verName.split(".", 3);
        int major = Integer.parseInt(ver[0]);
        int minor = Integer.parseInt(ver[1]);
        int patch = Integer.parseInt(ver[2]);
        return major * 1000000 + minor * 1000 + patch;
    }

    /**
     * Check for updates based on some passed version information.
     *
     * @param newestVersionCode The newest version code.
     * @param newestVersionName The newest version name.
     * @param newestUrl The URL to download the newest package.
     */
    public void checkForUpdates (int newestVersionCode, String newestVersionName, String newestUrl) {
        if (newestVersionCode > currentVersionCode) {
            Notifications notifications = new Notifications(context, broadcastClass);
            String title = context.getString(notificationTitle);
            String message = context.getString(notificationMessage, currentVersionName, newestVersionName);
            Notification.Builder not = notifications.createNotification(title, message, PRIO, notificationIcon, ACTION);
            Bundle bundle = new Bundle();
            bundle.putString("url", newestUrl);
            not.addExtras(bundle);
            notifications.show(not, NOTIFICATION_ID);
        }
    }

    /**
     * Check for updates based on just a version name (generate a code using getCodeForName()).
     *
     * @param newestVersionName The newest version name.
     * @param newestUrl The URL to download the newest package.
     */
    public void checkForUpdates (String newestVersionName, String newestUrl) {
        checkForUpdates(getCodeFromName(newestVersionName), newestVersionName, newestUrl);
    }

    /**
     * A version of checkForUpdates that will GET the updatesUrl, try to extract
     * the version information from the returned document, and then calls the
     * checkForUpdates(code, name, url) method.
     */
    public void checkForUpdates () {
        final String NO_VER = "0.0.0";
        GET(updatesUrl, data -> {
            String newestVerName = data.optString(updatesNameKey, NO_VER);
            int newestVerCode = data.optInt(updatesCodeKey);
            if (newestVerCode == 0 && !newestVerName.equals(NO_VER)) {
                newestVerCode = getCodeFromName(newestVerName);
            }
            String newestUrl = data.optString(updatesUrlKey, packageUrl);
            if (newestVerCode != 0 && !newestVerName.equals(NO_VER) && !newestUrl.isEmpty()) {
                checkForUpdates(newestVerCode, newestVerName, newestUrl);
            }
        });
    }

    /**
     * Download an update from a URL.
     *
     * @param url The URL to download the update from.
     */
    public void downloadUpdate(String url) {
        if (url == null) {
            logError("Must specify a url to download");
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();
        final Updater parent = this;
        sendRequest(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                parent.logError("Error downloading update APK file", e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    File download = new File(context.getCacheDir(), filename);
                    if (!response.isSuccessful()) throw new IOException("Unexpected code "+ response);
                    BufferedSink sink = Okio.buffer(Okio.sink(download));
                    sink.writeAll(response.body().source());
                    sink.close();
                    if (download.exists()) {
                        parent.installApk(download);
                    }
                } catch (IOException e) { parent.logError("IOException thrown while downloading update", e); }
            }
        });
    }

    /**
     * Log an Error message and an Exception thrown.
     *
     * Override if you want to have custom error handling.
     *
     * @param logMsg An additional error message.
     * @param e The Exception that was thrown.
     */
    public void logError (String logMsg, Exception e) {
        LongLog.e(TAG, logMsg + ": " + e.getMessage());
    }

    /**
     * Log an Error message.
     *
     * Override if you want to have custom error handling.
     *
     * @param logMsg The error message.
     */
    public void logError (String logMsg) {
        LongLog.e(TAG, logMsg);
    }

    /**
     * Log an Exception thrown
     *
     * Override if you want to have custom error handling.
     *
     * @param e The Exception that was thrown.
     */
    public void logError (Exception e) {
        LongLog.e(TAG, e.getMessage());
    }

    /**
     * This can be called from your BroadcastReceiver's onReceive() method.
     *
     * @param intent The intent passed to the onReceive() method.
     * @return If the broadcast that was received is for our action.
     */
    public boolean checkBroadcast (Intent intent) {
        final String intentAction = intent.getAction();
        return intentAction.equals(ACTION);
    }

    /**
     * This can be called from your BroadcastReceiver's onReceive() method,
     * assuming the checkBroadcast() method returned true.
     *
     * @param intent The intent passed to the onReceive() method.
     */
    public void downloadIntent (Intent intent) {
        Bundle bundle = intent.getExtras();
        String url = bundle.getString("url");
        downloadUpdate(url);
    }

    /**
     * A wrapper around
     * @param intent
     */
    public boolean downloadIfBroadcast (Intent intent) {
        if (checkBroadcast(intent)) {
            downloadIntent(intent);
            return true;
        }
        return false;
    }

    // TODO: move to PackageInstaller API.
    public void installApk(File file) {
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), PROVIDER, file);
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

}
