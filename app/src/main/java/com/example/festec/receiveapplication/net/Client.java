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
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

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
    private DatagramSocket socket;
    private int udpPort; // 组播侦听端口
    private static int BUFF_SIZE;
    private byte[] buffer;



    private long lastSendTime; //最后一次发送数据的时间


    public Client(String tcpServer, int tcpPort, int udpPort, Messenger messenger) {
        this.serverIP = tcpServer;
        this.serverPort = tcpPort;
        this.udpPort = udpPort;
        this.mMessenger = messenger;
        this.mac = getMacAddress();
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
                recBufSize,
                mode);
        audioTrk.setStereoVolume(AudioTrack.getMaxVolume(),
                AudioTrack.getMaxVolume());
        BUFF_SIZE = recBufSize + 100;
        buffer = new byte[BUFF_SIZE];


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
                    bw.write(mac + "|"
                            + clientSocket.getLocalAddress().toString().replaceFirst("/", "")
                            + "|" + udpPort);
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
//                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
//                        String line = null;
//                        while ((line = br.readLine()) != null) {
//                            // ignore
////                            Log.d(TAG, "心跳接收：\t" + line);
//                        }
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
                        Log.d(TAG, mac + " 断开连接");
                        if (os != null) {
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                            bw.write("quit" + mac);
                            bw.newLine();
                            bw.flush();
                        }
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
                    socket = new DatagramSocket(udpPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isUdpRun = true;
                initAudioTracker();
                audioTrk.play();
                while (isUdpRun && socket != null && !socket.isClosed()) {
                    try {
                        // 接收数据，同样会进入阻塞状态
                        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                        socket.receive(datagramPacket);
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
                                    audioTrk.write(baseMessage.getData(), 0, baseMessage.getDataLength());
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

    /**
     * 根据IP地址获取MAC地址
     * @return
     */
    private String getMacAddress() {
        String strMacAddr = null;
        try {
            // 获得IpD地址
            InetAddress ip = getLocalInetAddress();
            byte[] b = NetworkInterface.getByInetAddress(ip)
                    .getHardwareAddress();
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append(':');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toUpperCase();
        } catch (Exception e) {
            // ignore
        }
        return strMacAddr;
    }
    /**
     * 获取移动设备本地IP
     * @return
     */
    private static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            // 列举
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface
                    .getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {// 是否还有元素
                NetworkInterface ni = (NetworkInterface) en_netInterface
                        .nextElement();// 得到下一个元素
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();// 得到一个ip地址的列举
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(":"))
                        break;
                    else
                        ip = null;
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {

            e.printStackTrace();
        }
        return ip;
    }

}
