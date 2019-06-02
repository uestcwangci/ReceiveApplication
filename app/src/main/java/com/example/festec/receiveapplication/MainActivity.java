package com.example.festec.receiveapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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


    private int udpPort;

    private TextView textView, hintView;
    private ImageView imgView;
    private EditText editText;
    private ToggleButton startService;
    private ToggleButton startAud;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hintView = findViewById(R.id.receivrHint);
        textView = findViewById(R.id.text);
        imgView = findViewById(R.id.img);
        editText = findViewById(R.id.editPort);
        startService = findViewById(R.id.button);
        startAud = findViewById(R.id.bt_aud);
        imgView.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
        // 要申请的权限
        String[] permissions = null;
        if (VERSION.SDK_INT >= VERSION_CODES.P) {
            permissions = new String[]{
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.FOREGROUND_SERVICE};
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.RECORD_AUDIO};
        }
        PermissionUtil.getInstance().chekPermissions(this, permissions, permissionsResult);
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(editText.getText())) {
                    udpPort = Integer.parseInt(editText.getText().toString());
                }
            }
        };
        editText.addTextChangedListener(afterTextChangedListener);


        startService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (TextUtils.isEmpty(editText.getText())) {
                    startService.setChecked(false);
                    return;
                }
                if (udpPort == 0) {
                    startService.setChecked(false);
                    return;
                }

                if (isChecked) {
                    Intent intent = new Intent(MainActivity.this, ForeService.class);
                    intent.putExtra("messenger", new Messenger(handler));
                    intent.putExtra("port", udpPort);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(intent);
                    } else {
                        startService(intent);
                    }
                } else {
                    Intent stop = new Intent(MainActivity.this, ForeService.class);
                    stopService (stop);
                    hintView.setText("消息提示");
                    textView.setVisibility(View.INVISIBLE);
                    imgView.setVisibility(View.INVISIBLE);
                }
            }
        });

        startAud.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (TextUtils.isEmpty(editText.getText())) {
                    startAud.setChecked(false);
                    return;
                }
                if (udpPort == 0) {
                    startAud.setChecked(false);
                    return;
                }
                if (isChecked) {
                    Intent intent = new Intent(MainActivity.this, AudService.class);
                    intent.putExtra("port", udpPort);
                    if (Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(intent);
                    } else {
                        startService(intent);
                    }
                } else {
                    Intent stop = new Intent(MainActivity.this, AudService.class);
                    stopService (stop);
                }
            }
        });
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

    @Override
    protected void onDestroy() {
        Intent stop = new Intent(MainActivity.this, ForeService.class);
        stopService (stop);
        super.onDestroy();
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
