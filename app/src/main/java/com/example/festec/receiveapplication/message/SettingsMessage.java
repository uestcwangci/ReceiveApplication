package com.example.festec.receiveapplication.message;


import com.example.festec.receiveapplication.beans.Param;
import com.example.festec.receiveapplication.utils.ByteUtils;

import java.util.List;


public class SettingsMessage {
    //命令参数长度
    private int orderLength;
    //设置参数个数
    private byte settingCount;
    //参数设置列表
    private List<Param> settingsParams;

    public byte[] getSettingMessageBytes() {
        byte[] bytes;
        byte[] bytes1 = {settingCount};
        bytes = ByteUtils.addBytes(ByteUtils.intToByte(orderLength), bytes1);
        for (int i = 0; i < settingCount; i++) {
            bytes = ByteUtils.addBytes(bytes, settingsParams.get(i).getParamBytes());
        }
        return bytes;
    }

    public int getOrderLength() {
        return orderLength;
    }

    public void setOrderLength(int orderLength) {
        this.orderLength = orderLength;
    }

    public byte getSettingCount() {
        return settingCount;
    }

    public void setSettingCount(byte settingCount) {
        this.settingCount = settingCount;
    }

    public List<Param> getSettingsParams() {
        return settingsParams;
    }

    public void setSettingsParams(List<Param> settingsParams) {
        this.settingsParams = settingsParams;
    }
}
