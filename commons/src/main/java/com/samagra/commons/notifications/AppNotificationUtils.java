package com.samagra.commons.notifications;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.samagra.commons.R;

public class AppNotificationUtils {

    public static final String CHANNEL_ID = "collect_notification_channel";
    public static final int FORM_UPDATE_NOTIFICATION_ID = 0;

    private AppNotificationUtils() {
    }

    public static void createNotificationChannel(Application application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = application.getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(new NotificationChannel(
                        CHANNEL_ID,
                        application.getString(R.string.notification_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT)
                );
            }
        }
    }

    public static void showNotification(Context context, PendingIntent contentIntent,
                                        int notificationId,
                                        int title,
                                        String contentText) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID).setContentIntent(contentIntent);

        builder
                .setContentTitle(context.getString(title))
                .setContentText(contentText)
                .setSmallIcon(getNotificationAppIcon())
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }


    public static void showNotification(Context activityContext, PendingIntent contentIntent,
                                        int notificationId,
                                        String title,
                                        String contentText) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activityContext, CHANNEL_ID).setContentIntent(contentIntent);

        builder
                .setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(getNotificationAppIcon())
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) activityContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }

        private static int getNotificationAppIcon() {
            return R.drawable.samarth_logo;
        }
}
