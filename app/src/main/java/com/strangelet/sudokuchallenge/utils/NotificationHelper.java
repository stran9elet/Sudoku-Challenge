package com.strangelet.sudokuchallenge.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationHelper extends Application {

    public static final String NOTIFICATION_CHANNEL_ID = "notifId";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    public void createNotificationChannels(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel channel_1 =
                    new NotificationChannel(NOTIFICATION_CHANNEL_ID, "channel_1", NotificationManager.IMPORTANCE_HIGH);

            channel_1.setDescription("Daily Reminders");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel_1);
        }
    }
}
