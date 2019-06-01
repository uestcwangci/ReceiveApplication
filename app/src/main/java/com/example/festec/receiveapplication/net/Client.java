package com.example.festec.receiveapplication.net;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.festec.receiveapplication.message.BaseMessage;
import com.example.festec.receiveapplication.protocol.EmergencyProtocol;
import com.example.festec.receiveapplication.protocol.UnPackEmergencyProtocol;


import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Arrays;

public class Client {
    private static final String TAG = "waibao";
    private static final int RECEIVE_TEXT = 639;
    private static final int RECEIVE_IMG = 640;
    private static final int RECEIVE_AUD = 641;
    private static final int CANCEL_AUD = 444;
    // TCP
    private String serverIP = "192.168.0.116";
    private int serverPort = 10041;
    private Socket clientSocket = null;
    private InputStream is = null;
    private OutputStream os = null;
    private String mac;
    private boolean isTcpRun = false;// 连接状态

    private Handler mainHandler;

    private AudioTrack audioTrk = null;

    // UDP
    private boolean isUdpRun = true;
    public int udpPort = 6787;//数据监听绑定端口
    private DatagramSocket udpSocket = null;


    private long lastSendTime; //最后一次发送数据的时间


    public Client(String tcpServer, int tcpPort, String mac, int udpPort, Handler handler) {
        this.serverIP = tcpServer;
        this.serverPort = tcpPort;
        this.mac = mac;
        this.udpPort = udpPort;
        this.mainHandler = handler;
    }

    public void start() {
        tcpStart();
        udpStart();
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
        audioTrk = new AudioTrack(
                streamType,
                sampleRate,
                channelConfig,
                audioFormat,
                recBufSize * 4,
                mode);

    }



    private void tcpStart(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isTcpRun) {
                        return;
                    }
                    clientSocket = new Socket(serverIP, serverPort);
                    is = clientSocket.getInputStream();
                    os = clientSocket.getOutputStream();
                    lastSendTime = System.currentTimeMillis();
                    isTcpRun = true;
                    clientSocket.setKeepAlive(true);
                    Log.d(TAG, mac + " TCP连接成功");
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                    bw.write(udpPort + "|" + mac);
                    bw.newLine();
                    bw.flush();
                    new Thread(new KeepAliveWatchDog()).start();  //保持长连接的线程，每隔5秒项服务器发一个一个保持连接的心跳消息
                    new Thread(new ReceiveWatchDog()).start();    //接受消息的线程，处理消息
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    class KeepAliveWatchDog implements Runnable {
        long checkDelay = 10;
        long keepAliveDelay = 5000;

        public void run() {
            while (isTcpRun) {
                if (System.currentTimeMillis() - lastSendTime > keepAliveDelay) {
                    try {
                        Client.this.sendHeartPack("heart");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Client.this.tcpClose();
                    }
                    lastSendTime = System.currentTimeMillis();
                } else {
                    try {
                        Thread.sleep(checkDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Client.this.tcpClose();
                    }
                }
            }
        }
    }

    private void sendHeartPack(String str) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        bw.write("heart");
        bw.newLine();
//        System.out.println(mac + " 发送心跳包");
        bw.flush();
    }


    class ReceiveWatchDog implements Runnable {
        public void run() {
            while (isTcpRun) {
                try {
                    InputStream in = clientSocket.getInputStream();
                    if (in.available() > 0) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            // ignore
//                            System.out.println("心跳接收：\t" + line);
                        }
                    } else {
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Client.this.tcpClose();
                }
            }
        }
    }


    public void tcpClose() {
        try {
            System.out.println(mac + " 断开连接");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write("quit" + udpPort);
            bw.newLine();
            bw.flush();
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (isTcpRun) {
                isTcpRun = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void udpStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, mac + " UDP开始监听");
                runUdpClient();
            }
        }).start();
    }


    private void runUdpClient() {
        try {
            byte[] receiveBuffer = new byte[8196];//数据缓冲区  8M
            udpSocket = new DatagramSocket(udpPort);//绑定端口进行数据监听
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);//数据接收包囊
            initAudioTracker();

            while (isUdpRun) {
                udpSocket.receive(receivePacket);//接收数据.阻塞式
                Log.d(TAG, "UDP from " + receivePacket.getAddress().getHostAddress() + " : " + receivePacket.getPort());
                byte[] receiveBytes = receivePacket.getData();
                EmergencyProtocol protocol = UnPackEmergencyProtocol.unPack(receiveBytes,
                        receivePacket.getOffset());
                if (protocol != null && protocol.getBody().getT() instanceof BaseMessage) {
                    BaseMessage baseMessage = (BaseMessage) protocol.getBody().getT();
                    Message msg = new Message();
                    switch ((baseMessage.getDataType())) {
                        case 0x01:
                            Log.d("waibao", mac + " 收到音频消息");
                            msg.what = RECEIVE_AUD;
                            mainHandler.sendMessage(msg);
                            audioTrk.play();

                            byte[] audBytes = baseMessage.getData();
                            audioTrk.write(audBytes, 0, audBytes.length);
//                            String checkStop = null;
//                            if (audBytes.length > 3) {
//                                checkStop = new String(audBytes, 0, 4);
//                            }
//                            if (!"stop".equalsIgnoreCase(checkStop)) {
//                                msg.what = RECEIVE_AUD;
//                                mainHandler.sendMessage(msg);
//                            } else {
////                                audioTrk.stop();
//                                audioTrk.release();
//                                msg.what = CANCEL_AUD;
//                                mainHandler.sendMessage(msg);
//                            }

                            break;
                        case 0x02:
                            System.out.println("收到视频消息");
                            break;
                        case 0x03:
                            msg.what = RECEIVE_TEXT;
                            msg.obj = new String(baseMessage.getData());
                            mainHandler.sendMessage(msg);
                            Log.d("waibao", mac + " 收到文字消息: " + new String(baseMessage.getData()));
                            break;
                        case 0x04:
                            msg.what = RECEIVE_IMG;
                            msg.obj = protocol;
                            mainHandler.sendMessage(msg);
                            Log.d("waibao", mac + " 收到图片消息");
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void udpClose() {
        isUdpRun = false;
//        if (udpSocket != null) {
//            udpSocket.close();
//        }
        System.out.println("udp客户端关闭");
    }

}
