package com.luminaryn.common

import android.annotation.SuppressLint
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
import android.util.Log
import java.lang.Error

/**
 * A helper library for the Android Notifications system.
 *
 * Offers both lower-level wrappers around  the various Notification related libraries,
 * and a higher-level API for managing channels and notifications more transparently.
 *
 * @param context The context to use for notifications, application context is preferred.
 * @param broadcastClass The BroadcastReceiver class which will be used to receive broadcasts.
 *
 * @property context The context from which this was created.
 * @property broadcastClass The class which will be used to receive broadcasts.
 */
open class Notifications(private val context: Context, private val broadcastClass: Class<*>) {

    open var TAG = DEFAULT_TAG

    protected val channels: HashMap<String, Channel> = HashMap()

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
        desc: String? = null,
        prio: Int = NotificationManager.IMPORTANCE_DEFAULT
    ): NotificationChannel {
        val channel = NotificationChannel(id, name, prio)
        if (desc != null) {
            channel.description = desc
        }
        return channel
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

    /**
     * Get a channel from the notification manager.
     *
     * @param [id] The channel id we want to get.
     */
    @TargetApi(26)
    fun getNotificationChannel(id: String?): NotificationChannel? {
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

    /**
     * Create an extremely basic Notification.
     *
     * @param [channelId] The channel (ignored if Android version is older than 8.0).
     * @param [icon] The resource id or Icon object of the small icon for this notification.
     * @param [prio] The priority (optional, ignored if Android version is 8.0 or higher).
     */
    @JvmOverloads
    fun createNotification(
            channelId: String?,
            icon: Any? = null,
            prio: Int = Notification.PRIORITY_DEFAULT
    ) : Notification.Builder {
        val builder = Notification.Builder(context)

        if (icon is Int)
            builder.setSmallIcon(icon)
        else if (icon is Icon)
            builder.setSmallIcon(icon)

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
     * @param [title] The title for this notification.
     * @param [bodySummary] A short summary for this notification (optional).
     * @param [bodyMessage] A longer message for this notification (optional).
     * @param [icon] The resource id or Icon object for this notification.
     * @param [prio] The priority (optional, ignored if Android version is 8.0 or higher).
     * @param [contentAction] A PendingIntent to be used if the notification itself is clicked.
     */
    @JvmOverloads
    fun createNotification(
        channelId: String?,
        title: String?,
        bodySummary: String? = null,
        bodyMessage: String? = null,
        icon: Any? = null,
        prio: Int = Notification.PRIORITY_DEFAULT,
        contentAction: PendingIntent? = null,
    ): Notification.Builder {

        val builder = createNotification(channelId, icon, prio).setContentTitle(title)

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
    fun show(id: Int, builder: Notification.Builder) {
        show(id, builder.build())
    }

    /**
     * Show a notification that we've built.
     *
     * @param [notification] A fully built Notification object.
     * @param [id] The notification id, should be unique for different notifications.
     */
    fun show(id: Int, notification: Notification?) {
        notificationManager.notify(id, notification)
    }

    /**
     * Close a notification we previously put up.
     */
    fun cancel(id: Int) {
        notificationManager.cancel(id)
    }

    /**
     * Close all notifications we've created.
     */
    fun cancelAll() {
        notificationManager.cancelAll()
    }

    /**
     * Add a new channel specifying the values directly.
     *
     * @param id The unique identifier for this channel, will be registered in Android.
     * @param name The name of the channel
     * @param desc Optional description of the channel.
     * @param importance
     * @param priority
     * @param icon
     */

    /**
     * Add a new channel using an array definition.
     *
     * @param id  The unique identifier for this channel, will be registered in Android.
     * @param channelDef The definition for the channel. Must be an array with five fields:
     *
     *  importance: An Int value, or one of "min", "low", "default", "high", "max"
     *  priority: An Int value, or one of "min", "low", "default", "high", "max"
     *  name: A String, the name of the channel.
     *  desc: An optional String, a description of the channel.
     *  icon: An optional int of a drawable resource, or an Icon. Set to null for none.
     *
     * @return The Channel object that was created.
     */
    fun addChannel (id: String, channelDef: Array<*>): Channel {
        if (channels.containsKey(id)) throw Error("Cannot overwrite existing channel '$id'")

        val channel = Channel.fromArray(id, channelDef, this)
        channels[id] = channel

        return channel
    }

    /**
     * Add a whole bunch of channels using a HashMap where the keys
     * are the name of the channel, and the values are Arrays in the format wanted by
     * the addChannels(id, channelDef) method.
     */
    fun addChannels (channels: HashMap<String, Array<*>>) {
        for ((id, channel) in channels) {
            addChannel(id, channel)
        }
    }

    /**
     * Get a channel if it exists.
     */
    fun getChannel (id: String): Channel? {
        return channels[id]
    }

    /**
     * If the specified channel exists, make an empty super simple notification in it.
     */
    fun makeNotification (id: String): Notification.Builder? {
        return channels[id]?.makeNotification()
    }

    @JvmOverloads
    fun makeNotification (id: String, title: String, bodyShort: String?, bodyLong: String? = null, useIcon: Any? = null): Notification.Builder? {
        return channels[id]?.makeNotification(title, bodyShort, bodyLong, useIcon)
    }

    protected fun getText (id: Int): String {
        return context.getString(id)
    }

    open class Channel constructor(
            protected val parent: Notifications,
            val id: String,
            val name: String,
            val desc: String?,
            val importance: Int ,
            val priority: Int,
            val icon: Any?,
    ) {

        constructor(parent:Notifications, id: String, name: String, desc: String, importance: String, priority: String, icon: Any?) : this(parent, id, name, desc, getImp(importance), getPrio(priority), icon)

        val notificationChannel: NotificationChannel?

        init {
            notificationChannel = if (OVER_O)
                 parent.createChannel(id, name, desc, importance)
            else
                null
        }

        fun makeNotification (): Notification.Builder {
            val icon = if (this.icon is Int || this.icon is Icon) this.icon else if (this.icon is String && ICONS.containsKey(this.icon)) ICONS[this.icon] else ICONS[DEFAULT_ICON]
            return parent.createNotification(id, icon, priority)
        }

        @JvmOverloads
        fun makeNotification (title: String, bodyShort: String?, bodyLong: String? = null, useIcon: Any? = null): Notification.Builder {
            val icon = if (useIcon is Int || useIcon is Icon)
                useIcon
            else if (useIcon is String && ICONS.containsKey(useIcon))
                ICONS[useIcon]
            else if (this.icon is Int || this.icon is Icon)
                this.icon
            else if (this.icon is String && ICONS.containsKey(this.icon))
                ICONS[this.icon]
            else
                ICONS[DEFAULT_ICON]

            return parent.createNotification(id, title, bodyShort, bodyLong, icon, priority)
        }

        companion object {

            const val F_IMP  = 0
            const val F_PRIO = 1
            const val F_NAME = 2
            const val F_DESC = 3
            const val F_ICON = 4

            const val A_SIZE = 5

            protected fun getPrio (prio: String): Int {
                return if (PRIORITY_NAMES.containsKey(prio))
                    PRIORITY_NAMES[prio]!!
                else
                    PRIORITY_NAMES[DEFAULT_PRIORITY]!!
            }

            protected fun getImp (imp: String): Int {
                return if (IMPORTANCE_NAMES.containsKey(imp))
                    IMPORTANCE_NAMES[imp]!!
                else
                    IMPORTANCE_NAMES[DEFAULT_IMPORTANCE]!!
            }

            fun fromArray (id:String, def: Array<*>, parent: Notifications): Channel {
                if (def.size != A_SIZE) throw Error("Invalid array passed to fromArray(id, def, parent); did not have $A_SIZE fields.")
                val nameField = def[F_NAME]
                val name = if (nameField is String)
                    nameField
                else if (nameField is Int)
                    parent.getText(nameField)
                else
                    throw Error("Invalid name field value")

                val descField = def[F_DESC]
                val desc = if (descField is String)
                    descField
                else if (descField is Int)
                    parent.getText(descField)
                else
                    null

                val prioField = def[F_PRIO]
                val prio = if (prioField is Int)
                    prioField
                else if (prioField is String && PRIORITY_NAMES.containsKey(prioField))
                    PRIORITY_NAMES[prioField]!!
                else
                    PRIORITY_NAMES[DEFAULT_PRIORITY]!!

                val impField = def[F_IMP]
                val imp = if (impField is Int)
                    impField
                else if (impField is String && IMPORTANCE_NAMES.containsKey(impField))
                    IMPORTANCE_NAMES[impField]!!
                else
                    IMPORTANCE_NAMES[DEFAULT_IMPORTANCE]!!

                val icon = def[F_ICON]

                return Channel(parent, id, name, desc, imp, prio, icon)
            }

        }
    }

    companion object {

        val OVER_O = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

        const val MIN = "min"
        const val LOW = "low"
        const val DEFAULT = "default"
        const val HIGH = "high"
        const val MAX = "max"

        @SuppressLint("InlinedApi")
        val IMPORTANCE_NAMES = hashMapOf<String,Int>(
                MIN to if (OVER_O) NotificationManager.IMPORTANCE_MIN else 0,
                LOW to if (OVER_O) NotificationManager.IMPORTANCE_LOW else 0,
                DEFAULT to if (OVER_O) NotificationManager.IMPORTANCE_DEFAULT else 0,
                HIGH to if (OVER_O) NotificationManager.IMPORTANCE_HIGH else 0,
                MAX to if (OVER_O) NotificationManager.IMPORTANCE_MAX else 0,
        )
        const val DEFAULT_IMPORTANCE = DEFAULT

        val PRIORITY_NAMES = hashMapOf(
                MIN to Notification.PRIORITY_MIN,
                LOW to Notification.PRIORITY_LOW,
                DEFAULT to Notification.PRIORITY_DEFAULT,
                HIGH to Notification.PRIORITY_HIGH,
                MAX to Notification.PRIORITY_MAX
        )
        const val DEFAULT_PRIORITY = DEFAULT

        val ICONS = hashMapOf(
                "alert" to android.R.drawable.ic_dialog_alert,
                "info" to android.R.drawable.ic_dialog_info,
                "details" to android.R.drawable.ic_menu_info_details,
                "help" to android.R.drawable.ic_menu_help,
                "calendar" to android.R.drawable.ic_menu_my_calendar,
                "message" to android.R.drawable.ic_dialog_email,
                "lock" to android.R.drawable.ic_lock_idle_lock,
                "camera" to android.R.drawable.ic_menu_camera,
                "compass" to android.R.drawable.ic_menu_compass,
                "manage" to android.R.drawable.ic_menu_manage,
                "map" to android.R.drawable.ic_menu_mapmode,
                "sync" to android.R.drawable.ic_popup_sync,
                "add" to android.R.drawable.ic_menu_add,
                "agenda" to android.R.drawable.ic_menu_agenda,
                "star" to android.R.drawable.btn_star,
                "check" to android.R.drawable.checkbox_on_background,
        )
        const val DEFAULT_ICON = "info"

        const val DEFAULT_TAG = "Notifications"
    }
}