package com.strangelet.sudokuchallenge.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.strangelet.sudokuchallenge.R;
import com.strangelet.sudokuchallenge.activities.MainActivity;

public class NotifReceiver extends BroadcastReceiver {

    private NotificationManagerCompat notificationManagerCompat;
    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        notificationManagerCompat = NotificationManagerCompat.from(context);
        sendNotification();
    }

    public void sendNotification()
    {
        //first build your notification
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context,0 ,intent,0);

        Notification notification =
                new NotificationCompat.Builder(context,NotificationHelper.NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.sudoku_icon_foreground)
                        .setContentTitle("Daily challenge available")
                        .setContentText("Have you tried today's challenge yet?")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)//use this only if your notification belongs to one of the given categories, otherwise not.
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .build();

        notificationManagerCompat.notify(1,notification);
    }
}