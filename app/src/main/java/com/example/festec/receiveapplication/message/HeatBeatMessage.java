package com.example.festec.receiveapplication.message;


import com.example.festec.receiveapplication.utils.ByteUtils;

public class HeatBeatMessage {
    private int orderLength; //4个字节
    private byte status;  //1：空闲；2：工作；3：故障

    public int getOrderLength() {
        return orderLength;
    }

    public void setOrderLength(int orderLength) {
        this.orderLength = orderLength;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }


    public byte[] getHeartBeatUtilsBytes() {
        byte[] bytes1 = {status};
        byte[] bytes = ByteUtils.addBytes(ByteUtils.intToByte(orderLength), bytes1);
        return bytes;
    }

    @Override
    public String toString() {
        return "HeatBeatMessage{" +
                "status=" + status +
                '}';
    }
}
