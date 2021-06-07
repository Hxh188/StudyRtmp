package com.huang.android.video_rtmp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;

import com.android.rtmpvideo.RtmpJni;

import androidx.annotation.RequiresApi;

/**
 * @author huangxiaohui
 * @date 2021/6/5
 */
public class ScreenLive {
    public static final String TAG = "ScreenLive";
    private long ptr;
    private  String url = "rtmp://sendtc3.douyu.com/live/9855770rKCTxwskF?wsSecret=59d3f6d8cc97c94db5171c3671057060&wsTime=60bb3f84&wsSeek=off&wm=0&tw=0&roirecognition=0&record=flv&origin=tct";
    private Activity activity;
    private MediaProjectionManager mProjectionManager;
    private MediaProjection projection;
    private VirtualDisplay display;
    private VideoCodec videoCodec;

    public ScreenLive(Activity activity)
    {
        this.activity = activity;
        videoCodec = new VideoCodec();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startProjection(int reqCode) {
        mProjectionManager =
                (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        activity.startActivityForResult(mProjectionManager.createScreenCaptureIntent(), reqCode);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int resultCode, int reqCode, Intent intent)
    {
        projection = mProjectionManager.getMediaProjection(resultCode, intent);
        display = projection.createVirtualDisplay("1111", 720, 1280, 1,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, videoCodec.getSurface(), null, null);
//
//        new Thread()
//        {
//            @Override
//            public void run() {
//                super.run();
//                startConnect();
//            }
//        }.start();

    }

    private void startConnect()
    {
        ptr = RtmpJni.initRtmp(url, activity.getExternalFilesDir("Download") + "/log.txt");
        Log.e(TAG, "startRtmp:" + ptr);
    }

    public void stop()
    {
        RtmpJni.stopRtmp(ptr);
        Log.e(TAG, "stopRtmp");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopProjection(){
        projection.stop();
    }
}
