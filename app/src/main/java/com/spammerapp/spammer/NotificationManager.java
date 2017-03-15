package com.spammerapp.spammer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * Created by leona on 10/24/2015.
 */
public class NotificationManager {

    private Context mContext;

    public NotificationManager(Context context){
        this.mContext = context;
    }

    public void notifySendComplete(String recipient, int quantitySent, int quantityRequested){
        //Set locals
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String title = "Spammer";
        String msg = "Send Complete!";
        String msgExpand = "Sent " + quantitySent + " of " + quantityRequested + " to " + recipient + ".";
        long[] pattern = {500,500};
        //Create notification builder
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                        .setContentTitle(title)
                        .setContentText(msg)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setAutoCancel(true)
                        .setSound(soundUri)
                        .setSubText(msgExpand)
                        .setVibrate(pattern)
                        .setStyle(new NotificationCompat.InboxStyle());
        //Create result intent
        Intent resultIntent = new Intent(mContext, HistoryDetailActivity.class);
        //Create Stack Builder
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        //Configure Stack Builder
        stackBuilder.addParentStack(HistoryDetailActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        //Create pending intent
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        //Set Intent on Notification
        builder.setContentIntent(resultPendingIntent);
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        //Send notification
        notificationManager.notify(Constants.NOTIF_ID_SEND_COMPLETE, builder.build());
    }

    public void notifySendProgress(String recipient, int quantitySent, int quantityRequested){
        //Set locals
        String title = "Spammer";
        String msg = "Sending...";
        String msgExpand = "Sent " + quantitySent + " of " + quantityRequested + " to " + recipient + ".";
        //Create notification builder
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                        .setContentTitle(title)
                        .setContentText(msg)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setAutoCancel(true)
                        .setSubText(msgExpand)
                        .setStyle(new NotificationCompat.InboxStyle())
                        .setOngoing(true);
        //Create result intent
        Intent resultIntent = new Intent(mContext, MainActivity.class);
        //Create Stack Builder
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        //Configure Stack Builder
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        //Create pending intent
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        //Set Intent on Notification
        //builder.setContentIntent(resultPendingIntent);
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        //Send notification
        notificationManager.notify(Constants.NOTIF_ID_SEND_PROGRESS, builder.build());
    }

    public void removeSendProgressNotif(){
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.NOTIF_ID_SEND_PROGRESS);
    }

}
