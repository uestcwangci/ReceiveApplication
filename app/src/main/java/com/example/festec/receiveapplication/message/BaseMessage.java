package com.example.festec.receiveapplication.message;


import com.example.festec.receiveapplication.utils.ByteUtils;

import java.util.Calendar;


public class BaseMessage {
    protected short orderLength; // 命令参数长度
    protected byte broadcastType; // 广播类型
    protected byte level; // 事件级别
    protected int startTime; // 开始时间
    protected int endTime; // 结束时间
    protected byte dataType; // 数据类型
    protected int dataLength; // 数据长度
    protected byte[] data; // 内容


    public byte[] getBaseMessageBytes() {
        byte[] bytes = ByteUtils.short2Byte(orderLength);
        byte[] bytes1 = {broadcastType};
        byte[] bytes2 = {level};
        byte[] bytes3 = {dataType};
        bytes = ByteUtils.addBytes(bytes, bytes1);
        bytes = ByteUtils.addBytes(bytes, bytes2);
        bytes = ByteUtils.addBytes(bytes, ByteUtils.intToByte(getUTCTimeInt()));// startTime
        bytes = ByteUtils.addBytes(bytes, ByteUtils.intToByte(getUTCTimeInt()));// endTime
        bytes = ByteUtils.addBytes(bytes, bytes3);// dataType
        bytes = ByteUtils.addBytes(bytes, ByteUtils.intToByte(dataLength));// dataType
        bytes = ByteUtils.addBytes(bytes, data);
        return bytes;

    }


    private int getUTCTimeInt() {
        Calendar cal = Calendar.getInstance();
        return Long.valueOf(cal.getTimeInMillis() % 100000000).intValue();// 返回的就是UTC时间
    }

    public short getOrderLength() {
        return orderLength;
    }

    public void setOrderLength(short orderLength) {
        this.orderLength = orderLength;
    }

    public byte getBroadcastType() {
        return broadcastType;
    }

    public void setBroadcastType(byte broadcastType) {
        this.broadcastType = broadcastType;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public byte getDataType() {
        return dataType;
    }

    public void setDataType(byte dataType) {
        this.dataType = dataType;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaseMessage{" +
                "orderLength=" + orderLength +
                ", broadcastType=" + broadcastType +
                ", level=" + level +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", dataType=" + dataType +
                ", dataLength=" + dataLength +
                ", data=" + new String(data) +
                '}';
    }
}

