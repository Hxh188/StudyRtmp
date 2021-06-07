package com.huang.android.video_rtmp;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import com.android.rtmpvideo.RtmpJni;

import java.io.IOException;
import java.nio.ByteBuffer;

import androidx.annotation.RequiresApi;

/**
 * @author huangxiaohui
 * @date 2021/6/5
 */
public class VideoCodec extends Thread{
    public static final String TAG = "HxhVideoCodec";
    private MediaCodec codec;
    private boolean isLiving;
    private long timeStamp;
    private long startTime;
    private long ptr;
    public VideoCodec()
    {
    }

    public void init(long ptr, int width, int height) {
        this.ptr = ptr;
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        try {
            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            //TODO MediaCodec.CONFIGURE_FLAG_ENCODE??
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Surface getSurface()
    {
        if(codec == null)
        {
            return null;
        }
        return codec.createInputSurface();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run()
    {
        isLiving = true;
        codec.start();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        byte[] sps = null;
        byte[] pps = null;
        int lenSps = 0;
        int lenPps = 0;

        while (isLiving)
        {
            if(timeStamp != 0)
            {
                if(System.currentTimeMillis() - timeStamp >= 2000)
                {
                    Bundle params = new Bundle();
                    params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
                    codec.setParameters(params);
                    timeStamp = System.currentTimeMillis();
                }
            }else{
                timeStamp = System.currentTimeMillis();
            }

            int index = codec.dequeueOutputBuffer(bufferInfo, 10);

            if(index >= 0)
            {
                //buffer 为一个NALU??
                ByteBuffer buffer =  codec.getOutputBuffer(index);
                //outData 是包含startCode的NALU，NALU第一个字节低5位表示类型
                byte[] outData = new byte[bufferInfo.size];
                buffer.get(outData);
                if(startTime == 0)
                {
                    startTime = bufferInfo.presentationTimeUs / 1000;
                }
                long tms = (bufferInfo.presentationTimeUs / 1000) - startTime;
                Log.e(TAG, "getOutputBuffer inner :" + outData.length);
                //sps + pps
                if((outData[4] & 0x1f) == 7)
                {
                    Log.e(TAG, "getOutputBuffer saveSpsPps :" + (outData[4] & 0x1f));
                    for(int i = 0;i < outData.length;i++)
                    {
                        if(i + 4 < outData.length)
                        {
                            if(outData[i] == 0x00
                            && outData[i + 1] == 0x00
                            && outData[i + 2] == 0x00
                            && outData[i + 3] == 0x01
                            ){
                                if(outData[i + 4] == 0x68)
                                {
                                    lenSps = i - 4;
                                    sps = new byte[lenSps];
                                    for (int i1 = 0; i1 < lenSps; i1++) {
                                        sps[i1] = outData[4 + i1];
                                    }
                                    lenPps = outData.length - (4 + lenSps) - 4;
                                    pps = new byte[lenPps];
                                    for (int i1 = 0; i1 < lenPps; i1++) {
                                        pps[i1] = outData[4 + i + i1];
                                    }
                                    int bb = 0;
                                    bb ++;
                                    break;
                                }
                            }
                        }
                    }
                }else{
                    //关键帧
                    if((outData[4] & 0x1f) == 5)
                    {
                        if(sps != null && pps != null)
                        {
                            Log.e(TAG, "getOutputBuffer sendSpsPps");
                            RtmpJni.sendSpsAndPps(ptr, sps, sps.length, pps, pps.length);
                        }
                    }
                }
                Log.e(TAG, "getOutputBuffer sendVideoFrame");
                RtmpJni.sendVideoFrame(ptr, outData, outData.length, (int)tms);
                codec.releaseOutputBuffer(index, false);
            }
        }
        release();
    }

    public void release() {
        isLiving = false;
        startTime = 0;
        if(codec != null)
        {
            codec.stop();
            codec.release();
        }

    }
}
