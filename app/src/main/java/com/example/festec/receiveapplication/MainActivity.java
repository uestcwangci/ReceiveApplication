package com.example.festec.receiveapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.festec.receiveapplication.message.BaseMessage;
import com.example.festec.receiveapplication.net.Client;
import com.example.festec.receiveapplication.protocol.EmergencyProtocol;
import com.example.festec.receiveapplication.utils.PermissionUtil;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "waibao";
    private static final int RECEIVE_TEXT = 639;
    private static final int RECEIVE_IMG = 640;
    private static final int RECEIVE_AUD = 641;
    private static final int CANCEL_AUD = 444;


    private TextView textView, hintView;
    private ImageView imgView;



    // 要申请的权限
    private final String[] permissions = new String[]{
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hintView = findViewById(R.id.receivrHint);
        textView = findViewById(R.id.text);
        imgView = findViewById(R.id.img);
        imgView.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
        PermissionUtil.getInstance().chekPermissions(this, permissions, permissionsResult);
        Client client = new Client("192.168.0.116", 10041, "1", 6788, handler);
        client.start();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RECEIVE_TEXT:
                    textView.setVisibility(View.VISIBLE);
                    imgView.setVisibility(View.INVISIBLE);
                    hintView.setText("收到文字消息");
                    textView.setText((String) msg.obj);
                    break;
                case RECEIVE_IMG:
                    imgView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.INVISIBLE);
                    hintView.setText("收到图片消息");
                    EmergencyProtocol protocolImg = (EmergencyProtocol) msg.obj;

                    if (protocolImg != null) {
                        BaseMessage baseMessage = (BaseMessage) protocolImg.getBody().getT();
                        RequestOptions requestOptions1 = new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);
                        //将照片显示在 ivImage上
                        Glide.with(MainActivity.this)
                                .load(baseMessage.getData())
                                .apply(requestOptions1)
                                .into(imgView);
                    }
                    break;
                case RECEIVE_AUD:
                    textView.setVisibility(View.INVISIBLE);
                    imgView.setVisibility(View.INVISIBLE);
                    hintView.setText("收到音频");
                    break;
                case CANCEL_AUD:
                    textView.setVisibility(View.INVISIBLE);
                    imgView.setVisibility(View.INVISIBLE);
                    hintView.setText("音频发送结束");
                    break;

            }
        }
    };

    class audTask extends AsyncTask<Object, Void, Void> {
        EmergencyProtocol protocol;
        BaseMessage baseMessage;


        @Override
        protected void onPreExecute() {
            initAudioTracker();
        }

        @Override
        protected Void doInBackground(Object... objects) {

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            audioTrk.stop();
            audioTrk.release();
            super.onPostExecute(aVoid);
        }
    }



    private AudioTrack audioTrk = null;

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

    }

    //创建监听权限的接口对象
    PermissionUtil.IPermissionsResult permissionsResult = new PermissionUtil.IPermissionsResult() {
        @Override
        public void passPermissons() {
            Toast.makeText(MainActivity.this, "权限通过", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void forbitPermissons() {
//            finish();
            Toast.makeText(MainActivity.this, "权限不通过", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
