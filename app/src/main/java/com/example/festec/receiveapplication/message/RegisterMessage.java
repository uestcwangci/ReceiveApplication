package com.example.festec.receiveapplication.message;


import com.example.festec.receiveapplication.utils.ByteUtils;

import java.util.Arrays;


public class RegisterMessage {

    private int orderLength; //4个字节
    private byte deviceType; //1接收，2发送，3收发
    private int udpPort;  //UDP端口号,4个字节
    private byte deviceMacAddressLength;
    private byte[] deviceMacAddress;
    private byte usernameLength;
    private String username;

    public byte[] getRegisterMessageBytes() {
        byte[] bytes;
        byte[] bytes1 = {deviceType};
        byte[] bytes2 = {deviceMacAddressLength};
        byte[] bytes3 = {usernameLength};
        bytes = ByteUtils.addBytes(ByteUtils.intToByte(orderLength),bytes1);
        bytes = ByteUtils.addBytes(bytes,ByteUtils.intToByte(udpPort));
        bytes = ByteUtils.addBytes(bytes, bytes2);
        bytes = ByteUtils.addBytes(bytes, deviceMacAddress);
        bytes = ByteUtils.addBytes(bytes, bytes3);
        bytes = ByteUtils.addBytes(bytes, username.getBytes());
        return bytes;
    }

    public int getOrderLength() {
        return orderLength;
    }

    public void setOrderLength(int orderLength) {
        this.orderLength = orderLength;
    }

    public byte getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(byte deviceType) {
        this.deviceType = deviceType;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public byte getDeviceMacAddressLength() {
        return deviceMacAddressLength;
    }

    public void setDeviceMacAddressLength(byte deviceMacAddressLength) {
        this.deviceMacAddressLength = deviceMacAddressLength;
    }

    public byte[] getDeviceMacAddress() {
        return deviceMacAddress;
    }

    public void setDeviceMacAddress(byte[] deviceMacAddress) {
        this.deviceMacAddress = deviceMacAddress;
        setDeviceMacAddressLength((byte) deviceMacAddress.length);
    }

    public byte getUsernameLength() {
        return usernameLength;
    }

    public void setUsernameLength(byte usernameLength) {
        this.usernameLength = usernameLength;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        setUsernameLength((byte) username.getBytes().length);
    }

    @Override
    public String toString() {
        return "RegisterMessage{" +
                "orderLength=" + orderLength +
                ", deviceType=" + deviceType +
                ", udpPort=" + udpPort +
                ", deviceMacAddressLength=" + deviceMacAddressLength +
                ", deviceMacAddress=" + Arrays.toString(deviceMacAddress) +
                ", usernameLength=" + usernameLength +
                ", username='" + username + '\'' +
                '}';
    }
}
