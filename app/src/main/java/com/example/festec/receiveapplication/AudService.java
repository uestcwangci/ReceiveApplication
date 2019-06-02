package com.example.festec.receiveapplication;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.example.festec.receiveapplication.net.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class AudService extends Service {
    private static final String ID = "channel_2";
    private static final String NAME = "前台服2";

    private DatagramSocket datagramSocket;

    private boolean isRun = false;

    private byte[] buffer;
    private AudioTrack audioTrk;

    public AudService() {
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= 26) {
            setForeground();
        }
        initAudioTracker();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            datagramSocket = new DatagramSocket(7777);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        isRun = true;
        receiveThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    final Thread receiveThread = new Thread(new Runnable() {
        @Override
        public void run() {
            if (datagramSocket == null)
                return;
            //从文件流读数据
            audioTrk.play();
            // 包长
            while (isRun) {
                try {
                    // 数据报
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                    // 接收数据，同样会进入阻塞状态
                    datagramSocket.receive(datagramPacket);
                    audioTrk.write(datagramPacket.getData(), 0, datagramPacket.getLength());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    });

    @Override
    public void onDestroy() {
        isRun = false;
        if (audioTrk.getState() == AudioTrack.STATE_INITIALIZED) {
            audioTrk.stop();
            audioTrk.release();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void initAudioTracker() {
        //扬声器播放
        int streamType = AudioManager.STREAM_MUSIC;
        //播放的采样频率 和录制的采样频率一样
        int sampleRate = 44100;
        //和录制的一样的
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        //流模式
        int mode = AudioTrack.MODE_STREAM;
        //录音用输入单声道  播放用输出单声道
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        int recBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                channelConfig,
                audioFormat);
        System.out.println("****playRecBufSize = " + recBufSize);
        audioTrk = new AudioTrack(
                streamType,
                sampleRate,
                channelConfig,
                audioFormat,
                recBufSize,
                mode);
        audioTrk.setStereoVolume(AudioTrack.getMaxVolume(),
                AudioTrack.getMaxVolume());
        buffer = new byte[recBufSize];

    }

    @TargetApi(26)
    private void setForeground() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(ID, NAME, NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel(channel);
        Notification notification = new Notification.Builder(this, ID)
                .setContentTitle("音频服务")
                .setContentText("开启中")
                .build();
        startForeground(1, notification);

    }

}
