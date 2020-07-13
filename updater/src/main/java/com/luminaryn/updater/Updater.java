package com.luminaryn.updater;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
 *
 * There is no public constructor, you need to either make a subclass which
 * initializes all necessary data, or use the Builder class!
 */
public class Updater extends JSON {
    private Context context;
    private Class broadcastClass;

    protected int currentVersionCode;
    protected String currentVersionName;

    /**
     * The logging tag for error messages.
     */
    protected String TAG = "com.luminaryn.updater";

    /**
     * The ACTION name for the BroadcastReceiver intent.
     */
    protected String ACTION = TAG + ".ACTION_GET_UPDATE";

    /**
     * The file provider name for the cache file.
     * This provider must have access to the Cache Dir.
     */
    protected String PROVIDER = TAG + ".fileprovider";

    /**
     * The Prefix for Intent extras
     */
    protected String EXTRAPREFIX = TAG;

    /**
     * The name for the 'url' Intent extra parameter.
     */
    protected String URLEXTRA = "URL";

    /**
     * The notification id.
     */
    protected int NOTIFICATION_ID = 1;

    /**
     * The notification priority.
     */
    protected int PRIO = Notification.PRIORITY_DEFAULT;

    /**
     * The filename to save in the cache dir.
     */
    protected String filename = "latest.apk";

    /**
     * The id of the resource string to get the title for the notification.
     *
     * This MUST be specified in order to show the notification.
     */
    protected int notificationTitle;

    /**
     * The id of the resource string to get for the notification message.
     * Must accept two positional parameters, the first being the current app version,
     * and the second being the newest available app version.
     *
     * This MUST be specified in order to show the notification.
     */
    protected int notificationMessage;

    /**
     * The resource id for the drawable to use as the notification icon.
     *
     * This MUST be specified in order to show the notification.
     */
    protected int notificationIcon;

    /**
     * The channel id.
     *
     * In Android API 26 or higher, this is required to show the notification.
     */
    protected String notificationChannelId = TAG + ".CHANNEL";

    /**
     * The channel importance/priority.
     * Only used if we are creating the channel.
     */
    protected int notificationChannelPrio = NotificationManager.IMPORTANCE_DEFAULT;

    /**
     * The id of the resource string to get the channel name from.
     * Only used if we are creating the channel.
     */
    protected int notificationChannelName = 0;

    /**
     * The id of the resource string to get the channel description from.
     * Only used if we are creating the channel.
     */
    protected int notificationChannelDesc = 0;

    /**
     * A URL to download the update information from.
     *
     * This is only required if you are using the version of checkForUpdates() without
     * any parameters.
     */
    protected String updatesUrl;

    /**
     * If using the updatesUrl, this is the name of the property that will have the
     * version name (e.g. '1.2.3') in it. This MUST be in the response for it to be valid.
     */
    protected String updatesNameKey = "versionName";

    /**
     * If using the updatesUrl, this is the name of the property that will have the
     * version code (e.g. 1002003), if the code isn't found in the response, we'll
     * pass the version name to getCodeFromName().
     */
    protected String updatesCodeKey = "versionCode";

    /**
     * If using the updatesUrl, this is the name of the property that will have the
     * URL to the new package. If this isn't included, then you'll need to specify
     * a packageUrl property.
     */
    protected String updatesUrlKey = "packageURL";

    /**
     * If you want to use the updatesUrl and have a specific hard coded URL for
     * updates, set this and it'll be used as the default value if the updatesUrlKey
     * property isn't found in the JSON response.
     */
    protected String packageUrl = null;

    /**
     * Build a new Updater and get the current version from the context.
     * Use of this version is pretty much only useful from sub-classes, as
     * there's no way to set properties. As such, it's protected access.
     *
     * @param context The application context.
     * @param broadcastClass The BroadcastReceiver class.
     */
    protected Updater (Context context, Class broadcastClass) {
        super();
        this.context = context;
        this.broadcastClass = broadcastClass;
        getVersionFromContext();
    }

    /**
     * A constructor used by the Builder.build() method.
     * This is the preferred way to build Updater instances.
     *
     * @param builder
     */
    protected Updater (Builder builder) {
        super();

        context = builder.context;
        broadcastClass = builder.broadcastClass;

        if (builder.currentVersionName != null) {
            currentVersionName = builder.currentVersionName;
            if (builder.currentVersionCode != 0) {
                currentVersionCode = builder.currentVersionCode;
            }
            else
            {
                currentVersionCode = getCodeFromName(currentVersionName);
            }
        }
        else
        {
            getVersionFromContext();
        }

        if (builder.TAG != null) {
            TAG = builder.TAG;
        }
        if (builder.ACTION != null) {
            ACTION = builder.ACTION;
        }
        if (builder.PROVIDER != null) {
            PROVIDER = builder.PROVIDER;
        }
        if (builder.EXTRAPREFIX != null) {
            EXTRAPREFIX = builder.EXTRAPREFIX;
        }
        if (builder.URLEXTRA != null) {
            URLEXTRA = builder.URLEXTRA;
        }
        if (builder.NOTIFICATION_ID != 0) {
            NOTIFICATION_ID = builder.NOTIFICATION_ID;
        }
        if (builder.PRIO != Builder.UNSPECIFIED) {
            PRIO = builder.PRIO;
        }
        if (builder.filename != null) {
            filename = builder.filename;
        }
        if (builder.notificationTitle != 0) {
            notificationTitle = builder.notificationTitle;
        }
        if (builder.notificationMessage != 0) {
            notificationMessage = builder.notificationMessage;
        }
        if (builder.notificationIcon != 0) {
            notificationIcon = builder.notificationIcon;
        }
        if (builder.notificationChannelId != null) {
            notificationChannelId = builder.notificationChannelId;
        }
        if (builder.notificationChannelPrio != Builder.UNSPECIFIED) {
            notificationChannelPrio = builder.notificationChannelPrio;
        }
        if (builder.notificationChannelName != 0) {
            notificationChannelName = builder.notificationChannelName;
        }
        if (builder.notificationChannelDesc != 0) {
            notificationChannelDesc = builder.notificationChannelDesc;
        }
        if (builder.updatesUrl != null) {
            updatesUrl = builder.updatesUrl;
        }
        if (builder.updatesNameKey != null) {
            updatesNameKey = builder.updatesNameKey;
        }
        if (builder.updatesCodeKey != null) {
            updatesCodeKey = builder.updatesCodeKey;
        }
        if (builder.updatesUrlKey != null) {
            updatesUrlKey = builder.updatesUrlKey;
        }
        if (builder.packageUrl != null) {
            packageUrl = builder.packageUrl;
        }
    }

    private void getVersionFromContext ()
    {
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            currentVersionName = pinfo.versionName;
            currentVersionCode = pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            currentVersionName = "0.0.0";
            currentVersionCode = 0;
        }
    }

    /**
     * Return the extra key for the "url" parameter.
     * This is EXTRAPREFIX + "." + URLEXTRA
     */
    public String URL () {
        return EXTRAPREFIX + "." + URLEXTRA;
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Ensure the channel exists.
                if (notifications.getChannel(notificationChannelId) == null) {
                    if (notificationChannelName == 0) {
                        LongLog.e(TAG,"No channel name string id specified, cannot continue.");
                        return;
                    }
                    String name = context.getString(notificationChannelName);
                    if (notificationChannelDesc == 0) {
                        notifications.createChannel(notificationChannelId, name, notificationChannelPrio);
                    }
                    else
                    {
                        String desc = context.getString(notificationChannelDesc);
                        notifications.createChannel(notificationChannelId, name, notificationChannelPrio, desc);
                    }
                }
            }
            String title = context.getString(notificationTitle);
            String message = context.getString(notificationMessage, currentVersionName, newestVersionName);
            LongLog.d(TAG, "Notification title: "+title);
            LongLog.d(TAG, "Notification message: "+message);
            LongLog.d(TAG, "Newest URL: "+newestUrl);
            Bundle bundle = new Bundle();
            bundle.putString(URL(), newestUrl);
            LongLog.d(TAG, "Extras Bundle: "+bundle.toString());
            PendingIntent intent = notifications.createBroadcast(ACTION, bundle);
            Notification.Builder not = notifications.createNotification(notificationChannelId, title, message, PRIO, notificationIcon, intent);
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
    public boolean downloadIntent (Intent intent) {
        LongLog.d(TAG,"downloadIntent()");
        Bundle bundle = intent.getExtras();
        LongLog.d(TAG, "Extras Bundle: "+(bundle != null ? bundle.toString() : "null"));
        if (bundle != null) {
            String url = bundle.getString(URL());
            if (url != null) {
                downloadUpdate(url);
                return true;
            }
            else {
                logError("The 'url' property in Intent extras was null");
            }
        }
        else {
            logError("The Intent extras Bundle was null");
        }
        return false;
    }

    /**
     * A wrapper around checkBroadcast and downloadIntent
     * @param intent
     */
    public boolean downloadIfBroadcast (Intent intent) {
        if (checkBroadcast(intent)) {
            return downloadIntent(intent);
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

    /**
     * A Builder for making Updater instances.
     *
     * This is the preferred way to create Updater instances.
     */
    public static class Builder {
        static final int UNSPECIFIED = -1000;

        Context context;
        Class broadcastClass;

        int currentVersionCode = 0;
        String currentVersionName = null;

        String TAG = null;
        String ACTION = null;
        String PROVIDER = null;
        String EXTRAPREFIX = null;
        String URLEXTRA = null;
        int NOTIFICATION_ID = 0;
        int PRIO = UNSPECIFIED;
        String filename = null;
        int notificationTitle = 0;
        int notificationMessage = 0;
        int notificationIcon = 0;
        String notificationChannelId = null;
        int notificationChannelPrio = UNSPECIFIED;
        int notificationChannelName = 0;
        int notificationChannelDesc = 0;
        String updatesUrl = null;
        String updatesNameKey = null;
        String updatesCodeKey = null;
        String updatesUrlKey = null;
        String packageUrl = null;

        public Builder (Context context, Class broadcastClass) {
            this.context = context;
            this.broadcastClass = broadcastClass;
        }

        public Builder setTag (String tag) {
            TAG = tag;
            return this;
        }

        public Builder setAction (String action) {
            ACTION = action;
            return this;
        }

        public Builder setProvider (String provider) {
            PROVIDER = provider;
            return this;
        }

        public Builder setExtraPrefix (String prefix) {
            EXTRAPREFIX = prefix;
            return this;
        }

        public Builder setUrlExtra (String extra) {
            URLEXTRA = extra;
            return this;
        }

        public Builder setNotificationId (int id) {
            NOTIFICATION_ID = id;
            return this;
        }

        public Builder setPrio (int prio) {
            PRIO = prio;
            return this;
        }

        public Builder setFilename (String filename) {
            this.filename = filename;
            return this;
        }

        public Builder setNotificationTitle (int title) {
            notificationTitle = title;
            return this;
        }

        public Builder setNotificationMessage (int msg) {
            notificationMessage = msg;
            return this;
        }

        public Builder setNotificationIcon (int icon) {
            notificationIcon = icon;
            return this;
        }

        public Builder setChannel (String channel) {
            notificationChannelId = channel;
            return this;
        }

        public Builder setChannelPrio (int prio) {
            notificationChannelPrio = prio;
            return this;
        }

        public Builder setChannelName (int name) {
            notificationChannelName = name;
            return this;
        }

        public Builder setChannelDesc (int desc) {
            notificationChannelDesc = desc;
            return this;
        }

        public Builder setUpdatesUrl (String url) {
            updatesUrl = url;
            return this;
        }

        public Builder setNameKey (String key) {
            updatesNameKey = key;
            return this;
        }

        public Builder setCodeKey (String key) {
            updatesCodeKey = key;
            return this;
        }

        public Builder setUrlKey (String key) {
            updatesUrlKey = key;
            return this;
        }

        public Builder setPackageUrl (String url) {
            packageUrl = url;
            return this;
        }

        public Updater build () {
            return new Updater(this);
        }
    }

}
