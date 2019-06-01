package com.example.festec.receiveapplication.test;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.festec.receiveapplication.R;
import com.example.festec.receiveapplication.utils.PermissionUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ReceiveActivity extends AppCompatActivity {
    private static final String TAG = "waibao";

    // 要申请的权限
    private final String[] permissions = new String[]{
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO};

    private DatagramSocket datagramSocket;

    private boolean isRun = false;

    private byte[] buffer;
    private AudioTrack audioTrk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        initAudioTracker();
        initUI();
        PermissionUtil.getInstance().chekPermissions(this, permissions, permissionsResult);

        Log.d(TAG, "ip:" + getIpAddressString());

    }


    public static String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "0.0.0.0";
    }

    private void initUI() {
        ToggleButton button = findViewById(R.id.jianting);


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
                        Log.d(TAG, "收到消息");

                        audioTrk.write(datagramPacket.getData(), 0, datagramPacket.getLength());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        });

        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isRun = true;
                    receiveThread.start();
                } else {
                    isRun = false;
                    audioTrk.stop();
                    audioTrk.release();
                }
            }
        });

        try {
            datagramSocket = new DatagramSocket(7777);
        } catch (SocketException e) {
            e.printStackTrace();
        }


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

    //创建监听权限的接口对象
    PermissionUtil.IPermissionsResult permissionsResult = new PermissionUtil.IPermissionsResult() {
        @Override
        public void passPermissons() {
            Toast.makeText(ReceiveActivity.this, "权限通过", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void forbitPermissons() {
//            finish();
            Toast.makeText(ReceiveActivity.this, "权限不通过", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }


}
