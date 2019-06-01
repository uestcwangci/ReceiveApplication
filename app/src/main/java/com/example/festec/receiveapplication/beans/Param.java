package com.example.festec.receiveapplication.beans;


import com.example.festec.receiveapplication.utils.ByteUtils;

public class Param {
    /*
     * 1.终端本地 IP 地址信息， IP 地址（ 4 字节）、子网掩码（ 4 字节）、网关（ 4 字节），一共12 字节。
     * 2.设置设备资源编码（12字节）。
     * */
    private byte paramFlag;   //参数标识
    private byte paramLength;  //参数内容长度
    private byte[] paramContent; //参数内容

    public byte[] getParamBytes() {
        byte[] bytes = {paramFlag, paramLength};
        bytes = ByteUtils.addBytes(bytes, paramContent);
        return bytes;
    }

    public byte getParamFlag() {
        return paramFlag;
    }

    public void setParamFlag(byte paramFlag) {
        this.paramFlag = paramFlag;
    }

    public byte getParamLength() {
        return paramLength;
    }

    public void setParamLength(byte paramLength) {
        this.paramLength = paramLength;
    }

    public byte[] getParamContent() {
        return paramContent;
    }

    public void setParamContent(byte[] paramContent) {
        this.paramContent = paramContent;
    }
}
