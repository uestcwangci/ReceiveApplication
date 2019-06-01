package com.example.festec.receiveapplication.utils;

public class AckUtils {
    private byte resultCode;
    private short resultLength;
    private String resultContent;

    public byte[] getAckUtilsBytes() {
        byte[] bytes;
        byte[] bytes1 = {resultCode};
        bytes = ByteUtils.addBytes(bytes1, ByteUtils.intToByte(resultLength));
        bytes = ByteUtils.addBytes(bytes, resultContent.getBytes());
        return bytes;
    }

    public byte getResultCode() {
        return resultCode;
    }

    public void setResultCode(byte resultCode) {
        this.resultCode = resultCode;
    }

    public short getResultLength() {
        return resultLength;
    }

    public void setResultLength(short resultLength) {
        this.resultLength = resultLength;
    }

    public String getResultContent() {
        return resultContent;
    }

    public void setResultContent(String resultContent) {
        this.resultContent = resultContent;
        setResultLength((short) resultContent.getBytes().length);
    }

    public int getLength() {
        return getAckUtilsBytes().length;
    }

    @Override
    public String toString() {
        return "AckUtils{" +
                "resultCode=" + resultCode +
                ", resultLength=" + resultLength +
                ", resultContent='" + resultContent + '\'' +
                '}';
    }
}
