package com.huang.android.studyrtmp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * @author huangxiaohui
 * @date 2021/6/5
 */
public class MediaProjectionService extends Service {
    private MediaProjectionManager mProjectionManager;
    private MediaProjection projection;
    private VirtualDisplay display;
    @Override
    public void onCreate() {
        super.onCreate();
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.setAction("stop");
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String channelId = "001";
            String channelName = "myChannel";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager != null) {
                manager.createNotificationChannel(channel);
                Notification notification = new Notification.
                        Builder(getApplicationContext(), channelId)
                        .setOngoing(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .setContentTitle("Mainactivity")
                        .setContentIntent(contentIntent)
                        .build();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(0, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
                } else {
                    startForeground(0, notification);
                }
            }
        } else {
            startForeground(0, new Notification());
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
