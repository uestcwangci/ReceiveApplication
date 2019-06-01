package com.example.festec.receiveapplication.net;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.example.festec.receiveapplication.message.BaseMessage;
import com.example.festec.receiveapplication.protocol.EmergencyProtocol;
import com.example.festec.receiveapplication.protocol.UnPackEmergencyProtocol;


import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class Client {
    private static final String TAG = "waibao";
    private static final int RECEIVE_TEXT = 639;
    private static final int RECEIVE_IMG = 640;
    private static final int RECEIVE_AUD = 641;
    private static final int CANCEL_AUD = 444;
    // TCP
    private String serverIP;
    private int serverPort = 10041;
    private Socket clientSocket = null;
    private InputStream is = null;
    private OutputStream os = null;
    private String mac;
    private boolean isTcpRun = false;// 连接状态

    //    private Handler mainHandler;
    private Messenger mMessenger;

    private AudioTrack audioTrk = null;

    // UDP

    private boolean isUdpRun = true;
    private MulticastSocket multicastSocket = null;
    private int udpPort = 8888; // 组播侦听端口
    private String mulIp = "244.0.0.12";//组播地址 使用D类地址
    private byte[] buffer; // 缓存



    private long lastSendTime; //最后一次发送数据的时间


    public Client(String tcpServer, int tcpPort, Messenger messenger) {
        this.serverIP = tcpServer;
        this.serverPort = tcpPort;
        this.mMessenger = messenger;
        this.buffer = new byte[4096];
    }

    public void stop() {
        tcpClose();
        udpClose();
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
                recBufSize*2,
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
                    Log.d(TAG, "TCP连接成功");
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                    bw.write("applyMac");
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
                            if (!"heart".equalsIgnoreCase(line)) {
                                mac = line;
                            }
//                            Log.d(TAG, "心跳接收：\t" + line);
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(mac + " 断开连接");
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                        bw.write("quit" + mac);
                        bw.newLine();
                        bw.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (clientSocket != null) {
                                clientSocket.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (isTcpRun) {
                            isTcpRun = false;
                        }
                    }
                }
            }).start();


    }

    public void udpStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 接收数据时需要指定监听的端口号
                try {
                    multicastSocket = new MulticastSocket(udpPort);
                    // 创建组播ID地址
                    InetAddress address = InetAddress.getByName(mulIp);
                    // 加入地址
                    multicastSocket.joinGroup(address);
                    Log.d(TAG, "加入组播");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isUdpRun = true;
                if (multicastSocket == null)
                    return;
                initAudioTracker();
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                audioTrk.play();
                while (isUdpRun) {
                    try {
                        // 接收数据，同样会进入阻塞状态
                        multicastSocket.receive(datagramPacket);
                        Log.d(TAG, "UDP from " + datagramPacket.getAddress().getHostAddress() + " : " + datagramPacket.getPort());
                        byte[] receiveBytes = datagramPacket.getData();
                        EmergencyProtocol protocol = UnPackEmergencyProtocol.unPack(receiveBytes,
                                datagramPacket.getOffset());
                        if (protocol != null && protocol.getBody().getT() instanceof BaseMessage) {
                            BaseMessage baseMessage = (BaseMessage) protocol.getBody().getT();
                            Message msg = new Message();
                            switch ((baseMessage.getDataType())) {
                                case 0x01:
                                    Log.d("waibao", mac + " 收到音频消息");
                                    byte[] audBytes = baseMessage.getData();
                                    audioTrk.write(audBytes, 0, audBytes.length);
                                    msg.what = RECEIVE_AUD;
                                    mMessenger.send(msg);
                                    break;
                                case 0x02:
                                    System.out.println("收到视频消息");
                                    break;
                                case 0x03:
                                    msg.what = RECEIVE_TEXT;
                                    msg.obj = new String(baseMessage.getData());
                                    mMessenger.send(msg);
                                    Log.d("waibao", mac + " 收到文字消息: " + new String(baseMessage.getData()));
                                    break;
                                case 0x04:
                                    msg.what = RECEIVE_IMG;
                                    msg.obj = protocol;
                                    mMessenger.send(msg);
                                    Log.d("waibao", mac + " 收到图片消息");
                                    break;
                                default:
                                    Log.d(TAG, "收到未知消息");
                                    break;
                            }
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (multicastSocket != null) {
                                multicastSocket.leaveGroup(InetAddress.getByName(mulIp));
                                multicastSocket.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (audioTrk != null && audioTrk.getState() == AudioRecord.STATE_INITIALIZED) {
                            audioTrk.stop();
                            audioTrk.release();
                        }
                    }
                }
            }
        }).start();
    }



    public void udpClose() {
        isUdpRun = false;
        System.out.println("udp退出监听");
    }

}
