package com.luminaryn.common

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle

// TODO: Add Notification Channels for newer versions of Android!
open class Notifications(private val context: Context, private val broadcastClass: Class<*>) {
    val notificationManager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @TargetApi(26)
    fun buildChannel(id: String?, name: String?, prio: Int): NotificationChannel {
        return NotificationChannel(id, name, prio)
    }

    @TargetApi(26)
    fun buildChannel(id: String?, name: String?): NotificationChannel {
        return buildChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
    }

    @TargetApi(26)
    fun registerChannel(channel: NotificationChannel?) {
        notificationManager.createNotificationChannel(channel!!)
    }

    @TargetApi(26)
    fun createChannel(id: String?, name: String?, prio: Int): NotificationChannel {
        val channel = buildChannel(id, name, prio)
        registerChannel(channel)
        return channel
    }

    @TargetApi(26)
    fun createChannel(id: String?, name: String?): NotificationChannel {
        val channel = buildChannel(id, name)
        registerChannel(channel)
        return channel
    }

    @TargetApi(26)
    fun createChannel(id: String?, name: String?, prio: Int, desc: String?): NotificationChannel {
        val channel = buildChannel(id, name, prio)
        channel.description = desc
        registerChannel(channel)
        return channel
    }

    @TargetApi(26)
    fun createChannel(id: String?, name: String?, desc: String?): NotificationChannel {
        val channel = buildChannel(id, name)
        channel.description = desc
        registerChannel(channel)
        return channel
    }

    @TargetApi(26)
    fun getChannel(id: String?): NotificationChannel {
        return notificationManager.getNotificationChannel(id)
    }

    fun createIntent(): Intent {
        return Intent(context, broadcastClass)
    }

    fun createIntent(action: String?): Intent {
        return createIntent().setAction(action)
    }

    fun createIntent(action: String?, extras: Bundle?): Intent {
        return createIntent(action).putExtras(extras!!)
    }

    @JvmOverloads
    fun createBroadcast(intent: Intent?, requestCode: Int = 0, flags: Int = PendingIntent.FLAG_UPDATE_CURRENT): PendingIntent {
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    @JvmOverloads
    fun createBroadcast(action: String?, requestCode: Int = 0, flags: Int = PendingIntent.FLAG_UPDATE_CURRENT): PendingIntent {
        return createBroadcast(createIntent(action), requestCode, flags)
    }

    @JvmOverloads
    fun createBroadcast(action: String?, extras: Bundle?, requestCode: Int = 0, flags: Int = PendingIntent.FLAG_UPDATE_CURRENT): PendingIntent {
        return createBroadcast(createIntent(action, extras), requestCode, flags)
    }

    fun createNotification(channelId: String?, title: String?, bodyText: String?, prio: Int, icon: Int): Notification.Builder {
        val builder = Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(bodyText)
                .setSmallIcon(icon)
                .setPriority(prio)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channelId)
        }
        return builder
    }

    fun createNotification(channelId: String?, title: String?, bodyText: String?, icon: Int): Notification.Builder {
        return createNotification(channelId, title, bodyText, Notification.PRIORITY_DEFAULT, icon)
    }

    fun createNotification(channelId: String?, title: String?, bodyText: String?, prio: Int, icon: Int, pendingIntent: PendingIntent?): Notification.Builder {
        return createNotification(channelId, title, bodyText, prio, icon).setContentIntent(pendingIntent)
    }

    fun createNotification(channelId: String?, title: String?, bodyText: String?, icon: Int, pendingIntent: PendingIntent?): Notification.Builder {
        return createNotification(channelId, title, bodyText, icon).setContentIntent(pendingIntent)
    }

    fun show(builder: Notification.Builder, id: Int) {
        show(builder.build(), id)
    }

    fun show(notification: Notification?, id: Int) {
        notificationManager.notify(id, notification)
    }
}