package com.luminaryn.common

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle

/**
 * A helper library for the Android Notifications system.
 *
 * @property context The context from which this was created, application context is preferred.
 * @property broadcastClass The class which will be used to receive broadcasts.
 * @constructor Create a new Notifications manager with a set context and broadcast class.
 */
open class Notifications(private val context: Context, private val broadcastClass: Class<*>) {
    val notificationManager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * If this is true, and a bodySummary is passed but no bodyMessage is passed,
     * then if the bodySummary is longer than the maxBodyLength property, then it will also
     * be used as the bodyMessage parameter implicitly.
     */
    var autoTextSize: Boolean = true;

    /**
     * The maximum body length for Notification text content.
     * Only used if autoTextSize is true.
     */
    var maxBodyLength: Int = 35;

    /**
     * Create a Notification Channel and return it without registering it.
     *
     * @param [id] A unique id for the channel we are creating.
     * @param [name] A friendly name for the notification channel.
     * @param [desc] A friendly description for the notification channel.
     * @param [prio] The priority of the channel.
     */
    @JvmOverloads
    @TargetApi(26)
    fun buildChannel(
        id: String,
        name: String?,
        desc: String?,
        prio: Int = NotificationManager.IMPORTANCE_DEFAULT
    ): NotificationChannel {
        val channel = NotificationChannel(id, name, prio)
        if (desc != null) {
            channel.description = desc
        }
        return channel
    }

    @JvmOverloads
    @TargetApi(26)
    @Deprecated("Old API", ReplaceWith("buildChannel(id, name, null, prio)"))
    fun buildChannel(
        id: String,
        name: String?,
        prio: Int = NotificationManager.IMPORTANCE_DEFAULT
    ) : NotificationChannel {
        return buildChannel(id, name, null, prio)
    }

    /**
     * Register a notification channel with the manager.
     * After this is done, no more changes can be made to the channel!
     *
     * @param [channel] The channel object we are registering.
     */
    @TargetApi(26)
    fun registerChannel(channel: NotificationChannel) {
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Create a notification channel, and register it immediately.
     * No further changes will be able to be made to this channel.
     *
     * @param [id] A unique id for the channel we are creating.
     * @param [name] A friendly name for the notification channel.
     * @param [desc] A friendly description for the notification channel (optional).
     * @param [prio] The priority of the channel (optional).
     */
    @JvmOverloads
    @TargetApi(26)
    fun createChannel(
        id: String,
        name: String?,
        desc: String? = null,
        prio: Int = NotificationManager.IMPORTANCE_DEFAULT
    ) : NotificationChannel {
        val channel = buildChannel(id, name, desc, prio)
        registerChannel(channel)
        return channel
    }

    @TargetApi(26)
    @Deprecated("Old API", ReplaceWith("createChannel(id, name, desc, prio)"))
    fun createChannel(id: String, name: String?, prio: Int, desc: String?): NotificationChannel {
        return createChannel(id, name, desc, prio)
    }

    /**
     * Get a channel from the notification manager.
     *
     * @param [id] The channel id we want to get.
     */
    @TargetApi(26)
    fun getChannel(id: String?): NotificationChannel? {
        return notificationManager.getNotificationChannel(id)
    }

    /**
     * Create a new Intent with the context and broadcast class
     * registered in our instance properties.
     */
    fun createIntent(): Intent {
        return Intent(context, broadcastClass)
    }

    /**
     * Create a new Intent with createIntent() and then
     * populate it's action and optionally some extras.
     *
     * @param [action] The name of the action.
     * @param [extras] A Bundle of extra data for the Intent (optional).
     */
    @JvmOverloads
    fun createIntent(action: String, extras: Bundle? = null): Intent {
        val intent = createIntent().setAction(action)
        if (extras != null) {
            intent.putExtras(extras)
        }
        return intent
    }

    /**
     * Create a pending intent for a braodcast.
     *
     * @param [intent] The Intent for the broadcast.
     * @param [requestCode] Broadcast request code (optional).
     * @param [flags] PendingIntent flags (optional).
     */
    @JvmOverloads
    fun createBroadcast(intent: Intent, requestCode: Int = 0, flags: Int = PendingIntent.FLAG_UPDATE_CURRENT): PendingIntent {
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    /**
     * Create an Intent with an action (and optional extras) then create a pending intent for
     * a broadcast using it. Combines createIntent and createBroadcast into a single call.
     *
     * @param [action] The name of the action.
     * @param [extras] A Bundle of extra data for the Intent (optional).
     * @param [requestCode] Broadcast request code (optional).
     * @param [flags] PendingIntent flags (optional).
     */
    @JvmOverloads
    fun createBroadcast(action: String, extras: Bundle? = null, requestCode: Int = 0, flags: Int = PendingIntent.FLAG_UPDATE_CURRENT): PendingIntent {
        return createBroadcast(createIntent(action, extras), requestCode, flags)
    }

    @Deprecated("Old API", ReplaceWith("createBroadcast(action, null, requestCode, flags)"))
    fun createBroadcast(action: String, requestCode: Int = 0, flags: Int = PendingIntent.FLAG_UPDATE_CURRENT): PendingIntent {
        return createBroadcast(action, null, requestCode, flags)
    }

    /**
     * Create an extremely basic Notification.
     *
     * @param [channelId] The channel (ignored if Android version is older than 8.0).
     * @param [icon] The resource id of the icon for this notification.
     * @param [prio] The priority (optional, ignored if Android version is 8.0 or higher).
     */
    @JvmOverloads
    fun createNotification(
        channelId: String?,
        icon: Int,
        prio: Int = Notification.PRIORITY_DEFAULT
    ) : Notification.Builder {
        val builder = Notification.Builder(context)
            .setSmallIcon(icon)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channelId)
        }
        else
        {
            builder.setPriority(prio)
        }
        return builder
    }

    /**
     * Create a more fleshed out notification.
     *
     * If the [bodySummary] field is set but the [bodyMessage] field is null, then the
     * [autoTextSize] and [maxBodyLength] instance properties be used to determine if an extended
     * length version of the bodySummary should be used as the bodyMessage (which is displayed
     * when the notification is expanded.)
     *
     * @param [channelId] The channel (ignored if Android version is older than 8.0).
     * @param [icon] The resource id of the icon for this notification.
     * @param [title] The title for this notification.
     * @param [bodySummary] A short summary for this notification (optional).
     * @param [bodyMessage] A longer message for this notification (optional).
     * @param [prio] The priority (optional, ignored if Android version is 8.0 or higher).
     * @param [contentAction] A PendingIntent to be used if the notification itself is clicked.
     */
    @JvmOverloads
    fun createNotification(
        channelId: String?,
        icon: Int,
        title: String?,
        bodySummary: String? = null,
        bodyMessage: String? = null,
        prio: Int = Notification.PRIORITY_DEFAULT,
        contentAction: PendingIntent? = null,
    ): Notification.Builder {
        val builder = createNotification(channelId, icon, prio)
            .setContentTitle(title)
        if (!bodySummary.isNullOrEmpty()) {
            builder.setContentText(bodySummary);
        }
        if (!bodyMessage.isNullOrEmpty()) {
            builder.style = Notification.BigTextStyle().bigText(bodyMessage)
        }
        else if (autoTextSize && !bodySummary.isNullOrEmpty() && bodySummary.length > maxBodyLength) {
            builder.style = Notification.BigTextStyle().bigText(bodySummary)
        }
        if (contentAction != null) {
            builder.setContentIntent(contentAction)
        }
        return builder
    }

    @Deprecated("Old API", ReplaceWith("createNotification(channelId, icon, title, bodyText, null, prio)"))
    fun createNotification(channelId: String?, title: String?, bodyText: String?, prio: Int, icon: Int): Notification.Builder {
        return createNotification(channelId, icon, title, bodyText, null, prio)
    }

    @Deprecated("Old API", ReplaceWith("createNotification(channelId, icon, title, bodyText)"))
    fun createNotification(channelId: String?, title: String?, bodyText: String?, icon: Int): Notification.Builder {
        return createNotification(channelId, icon, title, bodyText)
    }

    @Deprecated("Old API", ReplaceWith("createNotification(channelId, icon, title, bodyText, null, prio, contentAction)"))
    fun createNotification(channelId: String?, title: String?, bodyText: String?, prio: Int, icon: Int, contentAction: PendingIntent?): Notification.Builder {
        return createNotification(channelId, icon, title, bodyText, null, prio, contentAction)
    }

    @Deprecated("Old API", ReplaceWith("createNotification(channelId, icon, title, bodyText, contentAction = contentAction)"))
    fun createNotification(channelId: String?, title: String?, bodyText: String?, icon: Int, contentAction: PendingIntent?): Notification.Builder {
        return createNotification(channelId, icon, title, bodyText, contentAction = contentAction)
    }

    /**
     * Create and return a Notification.Action.Builder
     *
     * @param [icon] The resource id for the icon.
     * @param [title] The title for the action.
     * @param [intent] The PendingIntent for the action.
     */
    fun buildAction (icon: Int, title: CharSequence, intent: PendingIntent) : Notification.Action.Builder {
        return Notification.Action.Builder(icon, title, intent)
    }

    /**
     * Create and return a Notification.Action.Builder
     *
     * @param [icon] The Icon object for the action.
     * @param [title] The title for the action.
     * @param [intent] The PendingIntent for the action.
     */
    fun buildAction (icon: Icon, title: CharSequence, intent: PendingIntent) : Notification.Action.Builder {
        return Notification.Action.Builder(icon, title, intent)
    }

    /**
     * Create and return a Notification.Action
     *
     * @param [icon] The Icon object for the action.
     * @param [title] The title for the action.
     * @param [intent] The PendingIntent for the action.
     */
    fun createAction (icon: Int, title: CharSequence, intent: PendingIntent) : Notification.Action {
        return buildAction(icon, title, intent).build()
    }

    /**
     * Create and return a Notification.Action
     *
     * @param [icon] The Icon object for the action.
     * @param [title] The title for the action.
     * @param [intent] The PendingIntent for the action.
     */
    fun createAction (icon: Icon, title: CharSequence, intent: PendingIntent) : Notification.Action {
        return buildAction(icon, title, intent).build()
    }

    /**
     * Show a notification we've crafted with createNotification()
     *
     * @param [builder] A Notification.Builder object from createNotification()
     * @param [id] The notification id, should be unique for different notifications.
     */
    @JvmOverloads
    fun show(builder: Notification.Builder, id: Int = 0) {
        show(builder.build(), id)
    }

    /**
     * Show a notification that we've built.
     *
     * @param [notification] A fully built Notification object.
     * @param [id] The notification id, should be unique for different notifications.
     */
    @JvmOverloads
    fun show(notification: Notification?, id: Int = 0) {
        notificationManager.notify(id, notification)
    }
}