package com.huang.android.studyrtmp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import com.android.rtmpvideo.RtmpJni;
import com.mask.mediaprojection.interfaces.MediaRecorderCallback;
import com.mask.mediaprojection.utils.MediaProjectionHelper;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private TextView tv_state;
    private String url =
            "rtmp://sendtc3a.douyu.com/live/"
            + "9855770r7b3CJzcP?wsSecret=3a0f83799436ae92882b3603b6552403&wsTime=60bdb646&wsSeek=off&wm=0&tw=0&roirecognition=0&record=flv&origin=tct";
    private long rtmpPtr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_state = findViewById(R.id.tv_state);
        MediaProjectionHelper.getInstance().startService(MainActivity.this);
        findViewById(R.id.tv_start).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                rtmpPtr = RtmpJni.initRtmp(url, getExternalFilesDir("Download") + "/log.txt");
                MediaProjectionHelper.getInstance().startMediaRecorder(rtmpPtr, new MediaRecorderCallback() {
                    @Override
                    public void onSuccess(File file) {
                        super.onSuccess(file);
                    }

                    @Override
                    public void onFail() {
                        super.onFail();
                    }
                });
            }
        });
        findViewById(R.id.tv_stop).setOnClickListener(new View.OnClickListener(){

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                RtmpJni.stopRtmp(rtmpPtr);
                tv_state.setText("已结束");
                MediaProjectionHelper.getInstance().stopMediaRecorder();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MediaProjectionHelper.getInstance().createVirtualDisplay(requestCode, resultCode, data, true, true);

        tv_state.setText("进行中...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaProjectionHelper.getInstance().stopService(this);
    }
}