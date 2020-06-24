package com.luminaryn.common;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

// TODO: Add Notification Channels for newer version of Android!
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

    public PendingIntent createAction (String action, int requestCode, int flags) {
        Intent updateIntent = new Intent(context, broadcastClass);
        updateIntent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, updateIntent, flags);
        return pendingIntent;
    }

    public PendingIntent createAction (String action) {
        return createAction(action, 0, 0);
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

    public Notification.Builder createNotification (String title, String bodyText, int prio, int icon, String action) {
        Notification.Builder builder = createNotification(title, bodyText, prio, icon);
        PendingIntent intent = createAction(action);
        builder.setContentIntent(intent).setAutoCancel(true);
        return builder;
    }

    public Notification.Builder createNotification (String title, String bodyText, int icon, String action) {
        return createNotification(title, bodyText, Notification.PRIORITY_DEFAULT, icon, action);
    }

    public void show (Notification.Builder builder, int id) {
        show(builder.build(), id);
    }

    public void show (Notification notification, int id) {
        getNotificationManager().notify(id, notification);
    }
}
