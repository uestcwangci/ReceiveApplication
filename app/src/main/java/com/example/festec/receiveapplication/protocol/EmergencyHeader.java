package com.example.festec.receiveapplication.protocol;



import com.example.festec.receiveapplication.utils.ByteUtils;

import java.util.Arrays;


public class EmergencyHeader{
    private byte[] flag = new byte[]{(byte) 0xfe, (byte) 0xfd};  //2个字节  0xFEFD
    private byte[] version = new byte[]{0x01, 0x00};//2个字节 0x0100
    private byte[] sourceAddress; //12个字节
    private short messageId;   //消息编号，单向递增,2个字节
    private short packetType;  //1为请求包，2为应答包,2个字节
    private int packetLength; //数据包长度,整个应急广播数据包长度

    public byte[] getHeaderBytes() {
        byte[] bytes;
        bytes = ByteUtils.addBytes(flag, version);
        bytes = ByteUtils.addBytes(bytes, sourceAddress);
        bytes = ByteUtils.addBytes(bytes, ByteUtils.intToByte(messageId));
        bytes = ByteUtils.addBytes(bytes, ByteUtils.intToByte(packetType));
        bytes = ByteUtils.addBytes(bytes, ByteUtils.intToByte(packetLength));
        return bytes;
    }

    public void setFlag(byte[] flag) {
        this.flag = flag;
    }

    public void setVersion(byte[] version) {
        this.version = version;
    }

    public void setSourceAddress(byte[] sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public void setMessageId(short messageId) {
        this.messageId = messageId;
    }

    public void setPacketType(short packetType) {
        this.packetType = packetType;
    }

    public void setPacketLength(int packetLength) {
        this.packetLength = packetLength;
    }

    public byte[] getFlag() {
        return flag;
    }

    public byte[] getVersion() {
        return version;
    }

    public byte[] getSourceAddress() {
        return sourceAddress;
    }

    public short getMessageId() {
        return messageId;
    }

    public short getPacketType() {
        return packetType;
    }

    public int getPacketLength() {
        return packetLength;
    }

    @Override
    public String toString() {
        return "EmergencyHeader{" +
                "flag=" + ByteUtils.bytesToHexString(flag) +
                ", version=" + ByteUtils.bytesToHexString(version) +
                ", sourceAddress=" + Arrays.toString(sourceAddress) +
                ", messageId=" + messageId +
                ", packetType=" + packetType +
                ", packetLength=" + packetLength +
                '}';
    }
}