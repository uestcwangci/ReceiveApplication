package com.example.festec.receiveapplication.message;


public class TextMessage extends BaseMessage{


    public TextMessage() {
    }

    public TextMessage(String broadcastType, String broadcastLevel) {
        this.orderLength = 7;
        switch (broadcastType) {
            case "应急演练-发布系统演练":
                this.broadcastType = 0x01;
                break;
            case "应急演练-模拟演练":
                this.broadcastType = 0x02;
                break;
            case "应急演练-实际演练":
                this.broadcastType = 0x03;
                break;
            case "应急广播":
                this.broadcastType = 0x04;
                break;
            case "日常广播":
                this.broadcastType = 0x05;
                break;
            default:
                break;
        }
        switch (broadcastLevel) {
            case "特别重大":
                this.level = 0x01;
                break;
            case "重大":
                this.level = 0x02;
                break;
            case "较大":
                this.level = 0x03;
                break;
            case "一般":
                this.level = 0x04;
                break;
            default:
                break;
        }
        this.dataType = 0x03;

    }


    @Override
    public String toString() {
        return "TextMessage{" +
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
