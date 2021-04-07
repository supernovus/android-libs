package com.luminaryn.updater

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.luminaryn.common.LongLog.d
import com.luminaryn.common.LongLog.e
import com.luminaryn.common.Notifications
import org.json.JSONObject

/**
 * A class for building a web service which can install updates.
 *
 * There is no public constructor, you need to either make a subclass which
 * initializes all necessary data, or use the Builder class!
 */
open class Updater : Installer {
    private var broadcastClass: Class<*>
    protected var currentVersionCode = 0
    protected var currentVersionName: String? = null

    /**
     * The logging tag for error messages.
     */
    override var TAG: String = "com.luminaryn.updater"

    /**
     * The ACTION name for the BroadcastReceiver intent.
     */
    protected var ACTION: String? = null

    /**
     * The Prefix for Intent extras
     */
    protected var EXTRAPREFIX: String? = null

    /**
     * The name for the 'url' Intent extra parameter.
     */
    protected var URLEXTRA: String? = "URL"

    /**
     * The notification id.
     */
    protected var NOTIFICATION_ID = 1

    /**
     * The notification priority.
     */
    protected var notificationPrio = Notification.PRIORITY_HIGH

    /**
     * The filename to save in the cache dir.
     */
    protected var filename: String = "latest.apk"

    /**
     * The id of the resource string to get the title for the notification.
     *
     * This MUST be specified in order to show the notification.
     */
    protected var notificationTitle = 0

    /**
     * The id of the resource string to get for the notification message.
     * Must accept two positional parameters, the first being the current app version,
     * and the second being the newest available app version.
     *
     * This MUST be specified in order to show the notification.
     */
    protected var notificationMessage = 0

    /**
     * The resource id for the drawable to use as the notification icon.
     *
     * This MUST be specified in order to show the notification.
     */
    protected var notificationIcon = 0

    /**
     * The channel id.
     *
     * In Android API 26 or higher, this is required to show the notification.
     */
    protected var notificationChannelId: String? = null

    /**
     * The channel importance/priority.
     * Only used if we are creating the channel.
     */
    protected var notificationChannelPrio: Int = 0

    /**
     * The id of the resource string to get the channel name from.
     * Only used if we are creating the channel.
     */
    protected var notificationChannelName = 0

    /**
     * The id of the resource string to get the channel description from.
     * Only used if we are creating the channel.
     */
    protected var notificationChannelDesc = 0

    /**
     * A URL to download the update information from.
     *
     * This is only required if you are using the version of checkForUpdates() without
     * any parameters.
     */
    protected var updatesUrl: String? = null

    /**
     * If using the updatesUrl, this is the name of the property that will have the
     * version name (e.g. '1.2.3') in it. This MUST be in the response for it to be valid.
     */
    protected var updatesNameKey: String? = "versionName"

    /**
     * If using the updatesUrl, this is the name of the property that will have the
     * version code (e.g. 1002003), if the code isn't found in the response, we'll
     * pass the version name to getCodeFromName().
     */
    protected var updatesCodeKey: String? = "versionCode"

    /**
     * If using the updatesUrl, this is the name of the property that will have the
     * URL to the new package. If this isn't included, then you'll need to specify
     * a packageUrl property.
     */
    protected var updatesUrlKey: String? = "packageURL"

    /**
     * If you want to use the updatesUrl and have a specific hard coded URL for
     * updates, set this and it'll be used as the default value if the updatesUrlKey
     * property isn't found in the JSON response.
     */
    protected var packageUrl: String? = null

    /**
     * A constructor used by the Builder.build() method.
     * This is the preferred way to build Updater instances.
     *
     * @param builder
     */
    protected constructor(builder: Builder) : super(builder.context) {
        broadcastClass = builder.broadcastClass
        if (builder.currentVersionName != null) {
            currentVersionName = builder.currentVersionName
            currentVersionCode = if (builder.currentVersionCode != 0) {
                builder.currentVersionCode
            } else {
                getCodeFromName(currentVersionName)
            }
        } else {
            versionFromContext
        }
        if (builder.TAG != null) {
            TAG = builder.TAG!!
        }
        if (builder.ACTION != null) {
            ACTION = builder.ACTION
        }
        else {
            ACTION = "$TAG.ACTION_GET_UPDATE"
        }
        if (builder.PROVIDER != null) {
            provider = builder.PROVIDER
        }
        else
        {
            provider = "$TAG.fileprovider"
        }
        if (builder.EXTRAPREFIX != null) {
            EXTRAPREFIX = builder.EXTRAPREFIX
        }
        else
        {
            EXTRAPREFIX = TAG
        }
        if (builder.URLEXTRA != null) {
            URLEXTRA = builder.URLEXTRA
        }
        if (builder.NOTIFICATION_ID != 0) {
            NOTIFICATION_ID = builder.NOTIFICATION_ID
        }
        if (builder.PRIO != Builder.UNSPECIFIED) {
            notificationPrio = builder.PRIO
        }
        if (builder.filename != null) {
            filename = builder.filename!!
        }
        if (builder.notificationTitle != 0) {
            notificationTitle = builder.notificationTitle
        }
        if (builder.notificationMessage != 0) {
            notificationMessage = builder.notificationMessage
        }
        if (builder.notificationIcon != 0) {
            notificationIcon = builder.notificationIcon
        }
        if (builder.notificationChannelId != null) {
            notificationChannelId = builder.notificationChannelId
        }
        else {
            notificationChannelId = "$TAG.CHANNEL"
        }
        if (builder.notificationChannelPrio != Builder.UNSPECIFIED) {
            notificationChannelPrio = builder.notificationChannelPrio
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            notificationChannelPrio = NotificationManager.IMPORTANCE_HIGH
        }
        if (builder.notificationChannelName != 0) {
            notificationChannelName = builder.notificationChannelName
        }
        if (builder.notificationChannelDesc != 0) {
            notificationChannelDesc = builder.notificationChannelDesc
        }
        if (builder.updatesUrl != null) {
            updatesUrl = builder.updatesUrl
        }
        if (builder.updatesNameKey != null) {
            updatesNameKey = builder.updatesNameKey
        }
        if (builder.updatesCodeKey != null) {
            updatesCodeKey = builder.updatesCodeKey
        }
        if (builder.updatesUrlKey != null) {
            updatesUrlKey = builder.updatesUrlKey
        }
        if (builder.packageUrl != null) {
            packageUrl = builder.packageUrl
        }
    }

    private val versionFromContext: Unit
        get() {
            try {
                val pinfo = context.packageManager.getPackageInfo(context.packageName, 0)
                currentVersionName = pinfo.versionName
                currentVersionCode = pinfo.versionCode
            } catch (e: PackageManager.NameNotFoundException) {
                currentVersionName = "0.0.0"
                currentVersionCode = 0
            }
        }

    /**
     * Return the extra key for the "url" parameter.
     * This is EXTRAPREFIX + "." + URLEXTRA
     */
    fun URL(): String {
        return "$EXTRAPREFIX.$URLEXTRA"
    }

    /**
     * Generate a version code from a version name.
     *
     * This requires a three part version name,
     * e.g. '1.2.3' would become 1002003
     * @param verName
     * @return
     */
    fun getCodeFromName(verName: String?): Int {
        val ver = verName!!.split(".".toRegex(), 3).toTypedArray()
        val major = ver[0].toInt()
        val minor = ver[1].toInt()
        val patch = ver[2].toInt()
        return major * 1000000 + minor * 1000 + patch
    }

    /**
     * Check for updates based on some passed version information.
     *
     * @param newestVersionCode The newest version code.
     * @param newestVersionName The newest version name.
     * @param newestUrl The URL to download the newest package.
     */
    fun checkForUpdates(newestVersionCode: Int, newestVersionName: String?, newestUrl: String) {
        if (newestVersionCode > currentVersionCode) {
            val notifications = Notifications(context, broadcastClass)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Ensure the channel exists.
                if (notifications.getNotificationChannel(notificationChannelId) == null) {
                    if (notificationChannelName == 0) {
                        e(TAG, "No channel name string id specified, cannot continue.")
                        return
                    }
                    val name = context.getString(notificationChannelName)
                    if (notificationChannelDesc == 0) {
                        notifications.createChannel(notificationChannelId!!, name, null, notificationChannelPrio)
                    } else {
                        val desc = context.getString(notificationChannelDesc)
                        notifications.createChannel(notificationChannelId!!, name, desc, notificationChannelPrio)
                    }
                }
            }
            val title = context.getString(notificationTitle)
            val message = context.getString(notificationMessage, currentVersionName, newestVersionName)
            d(TAG, "Notification title: $title")
            d(TAG, "Notification message: $message")
            d(TAG, "Newest URL: $newestUrl")
            val bundle = Bundle()
            bundle.putString(URL(), newestUrl)
            d(TAG, "Extras Bundle: $bundle")
            val intent = notifications.createBroadcast(ACTION!!, bundle)
            val not = notifications.createNotification(
                notificationChannelId,
                title,
                message,
                null,
                notificationIcon,
                notificationPrio,
                intent
            )
            notifications.show(NOTIFICATION_ID, not)
        }
    }

    /**
     * Check for updates based on just a version name (generate a code using getCodeForName()).
     *
     * @param newestVersionName The newest version name.
     * @param newestUrl The URL to download the newest package.
     */
    fun checkForUpdates(newestVersionName: String?, newestUrl: String) {
        checkForUpdates(getCodeFromName(newestVersionName), newestVersionName, newestUrl)
    }

    /**
     * A version of checkForUpdates that will GET the updatesUrl, try to extract
     * the version information from the returned document, and then calls the
     * checkForUpdates(code, name, url) method.
     */
    fun checkForUpdates() {
        val NO_VER = "0.0.0"
        GET(updatesUrl!!, object : JSONResponseHandler {
            override fun handle(data: JSONObject) {
                val newestVerName = data.optString(updatesNameKey, NO_VER)
                var newestVerCode = data.optInt(updatesCodeKey)
                if (newestVerCode == 0 && newestVerName != NO_VER) {
                    newestVerCode = getCodeFromName(newestVerName)
                }
                val newestUrl = data.optString(updatesUrlKey, packageUrl!!)
                if (newestVerCode != 0 && newestVerName != NO_VER && !newestUrl.isEmpty()) {
                    checkForUpdates(newestVerCode, newestVerName, newestUrl)
                }
            }
        })
    }

    /**
     * Download an update from a URL.
     *
     * @param url The URL to download the update from.
     */
    fun downloadUpdate(url: String?) {
        if (url == null) {
            logError("Must specify a url to download")
            return
        }
        downloadUpdate(url, filename)
    }

    /**
     * Log an Error message and an Exception thrown.
     *
     * Override if you want to have custom error handling.
     *
     * @param logMsg An additional error message.
     * @param e The Exception that was thrown.
     */
    fun logError(logMsg: String, e: Exception) {
        e(TAG, logMsg + ": " + e.message)
    }

    /**
     * Log an Error message.
     *
     * Override if you want to have custom error handling.
     *
     * @param logMsg The error message.
     */
    fun logError(logMsg: String?) {
        e(TAG, logMsg!!)
    }

    /**
     * Log an Exception thrown
     *
     * Override if you want to have custom error handling.
     *
     * @param e The Exception that was thrown.
     */
    fun logError(e: Exception) {
        e(TAG, e.message!!)
    }

    /**
     * This can be called from your BroadcastReceiver's onReceive() method.
     *
     * @param intent The intent passed to the onReceive() method.
     * @return If the broadcast that was received is for our action.
     */
    fun checkBroadcast(intent: Intent): Boolean {
        val intentAction = intent.action
        return intentAction == ACTION
    }

    /**
     * This can be called from your BroadcastReceiver's onReceive() method,
     * assuming the checkBroadcast() method returned true.
     *
     * @param intent The intent passed to the onReceive() method.
     */
    fun downloadIntent(intent: Intent): Boolean {
        d(TAG, "downloadIntent()")
        val bundle = intent.extras
        d(TAG, "Extras Bundle: " + (bundle?.toString() ?: "null"))
        if (bundle != null) {
            val url = bundle.getString(URL())
            if (url != null) {
                downloadUpdate(url)
                return true
            } else {
                logError("The 'url' property in Intent extras was null")
            }
        } else {
            logError("The Intent extras Bundle was null")
        }
        return false
    }

    /**
     * A wrapper around checkBroadcast and downloadIntent
     * @param intent
     */
    fun downloadIfBroadcast(intent: Intent): Boolean {
        return if (checkBroadcast(intent)) {
            downloadIntent(intent)
        } else false
    }

    /**
     * A Builder for making Updater instances.
     *
     * This is the preferred way to create Updater instances.
     */
    data class Builder @JvmOverloads constructor(
        var context: Context,
        var broadcastClass: Class<*>,
        var currentVersionCode: Int = 0,
        var currentVersionName: String? = null,
        var TAG: String? = null,
        var ACTION: String? = null,
        var PROVIDER: String? = null,
        var EXTRAPREFIX: String? = null,
        var URLEXTRA: String? = null,
        var NOTIFICATION_ID: Int = 0,
        var PRIO: Int = UNSPECIFIED,
        var filename: String? = null,
        var notificationTitle: Int = 0,
        var notificationMessage: Int = 0,
        var notificationIcon: Int = 0,
        var notificationChannelId: String? = null,
        var notificationChannelPrio: Int = UNSPECIFIED,
        var notificationChannelName: Int = 0,
        var notificationChannelDesc: Int = 0,
        var updatesUrl: String? = null,
        var updatesNameKey: String? = null,
        var updatesCodeKey: String? = null,
        var updatesUrlKey: String? = null,
        var packageUrl: String? = null,
    ) {
        fun tag(tag: String?) = apply {
            TAG = tag
        }

        fun action(action: String?) = apply {
            ACTION = action
        }

        fun provider(provider: String?) = apply {
            PROVIDER = provider
        }

        fun extraPrefix(prefix: String?) = apply {
            EXTRAPREFIX = prefix
        }

        fun urlExtra(extra: String?) = apply {
            URLEXTRA = extra
        }

        fun notificationId(id: Int) = apply {
            NOTIFICATION_ID = id
        }

        fun prio(prio: Int) = apply {
            PRIO = prio
        }

        fun filename(filename: String?) = apply {
            this.filename = filename
        }

        fun notificationTitle(title: Int) = apply {
            notificationTitle = title
        }

        fun notificationMessage(msg: Int) = apply {
            notificationMessage = msg
        }

        fun notificationIcon(icon: Int) = apply {
            notificationIcon = icon
        }

        fun channel(channel: String?) = apply {
            notificationChannelId = channel
        }

        fun channelPrio(prio: Int) = apply {
            notificationChannelPrio = prio
        }

        fun channelName(name: Int) = apply {
            notificationChannelName = name
        }

        fun channelDesc(desc: Int) = apply {
            notificationChannelDesc = desc
        }

        fun updatesUrl(url: String?) = apply {
            updatesUrl = url
        }

        fun nameKey(key: String?) = apply {
            updatesNameKey = key
        }

        fun codeKey(key: String?) = apply {
            updatesCodeKey = key
        }

        fun urlKey(key: String?) = apply {
            updatesUrlKey = key
        }

        fun packageUrl(url: String?) = apply {
            packageUrl = url
        }

        fun build() = Updater(this)

        companion object {
            const val UNSPECIFIED = -1000
        }
    }
}