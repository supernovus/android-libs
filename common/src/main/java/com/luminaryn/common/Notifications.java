package com.luminaryn.common;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

// TODO: Add Notification Channels for newer versions of Android!
public class Notifications {
    private Context context;
    private Class broadcastClass;

    public Notifications (Context context, Class broadcastClass) {
        this.context = context;
        this.broadcastClass = broadcastClass;
    }

    public NotificationManager getNotificationManager() {
        return (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public Intent createIntent () {
        return new Intent(context, broadcastClass);
    }

    public Intent createIntent (String action) {
        return createIntent().setAction(action);
    }

    public Intent createIntent (String action, Bundle extras) {
        return createIntent(action).putExtras(extras);
    }

    public PendingIntent createBroadcast (Intent intent, int requestCode, int flags) {
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }

    public PendingIntent createBroadcast (String action, int requestCode, int flags) {
        return createBroadcast(createIntent(action), requestCode, flags);
    }

    public PendingIntent createBroadcast (String action, Bundle extras, int requestCode, int flags) {
        return createBroadcast(createIntent(action, extras), requestCode, flags);
    }

    public PendingIntent createBroadcast (String action) {
        return createBroadcast(action, 0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public PendingIntent createBroadcast (String action, Bundle extras) {
        return createBroadcast(action, extras, 0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public PendingIntent createBroadcast (Intent intent) {
        return createBroadcast(intent, 0, 0);
    }

    public Notification.Builder createNotification (String title, String bodyText, int prio, int icon) {
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(bodyText)
                .setSmallIcon(icon)
                .setPriority(prio);
        return builder;
    }

    public Notification.Builder createNotification (String title, String bodyText, int icon) {
        return createNotification(title, bodyText, Notification.PRIORITY_DEFAULT, icon);
    }

    public Notification.Builder createNotification (String title, String bodyText, int prio, int icon, PendingIntent pendingIntent) {
        return createNotification(title, bodyText, prio, icon).setContentIntent(pendingIntent);
    }

    public Notification.Builder createNotification (String title, String bodyText, int icon, PendingIntent pendingIntent) {
        return createNotification(title, bodyText, icon).setContentIntent(pendingIntent);
    }

    public void show (Notification.Builder builder, int id) {
        show(builder.build(), id);
    }

    public void show (Notification notification, int id) {
        getNotificationManager().notify(id, notification);
    }
}
