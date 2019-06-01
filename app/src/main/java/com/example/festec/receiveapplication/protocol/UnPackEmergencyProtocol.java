package com.example.festec.receiveapplication.protocol;


import com.example.festec.receiveapplication.message.BaseMessage;
import com.example.festec.receiveapplication.utils.ByteUtils;

import java.util.ArrayList;
import java.util.List;

public class UnPackEmergencyProtocol {


    public UnPackEmergencyProtocol() {

    }

    public static EmergencyProtocol unPack(byte[] data, int offset) {
        EmergencyHeader header = new EmergencyHeader();

        header.setFlag(ByteUtils.splice(data, 0, 2, offset));
        header.setVersion(ByteUtils.splice(data, 2, 4, offset));
        header.setSourceAddress(ByteUtils.splice(data, 4, 10, offset));
        header.setMessageId(ByteUtils.byte2short(ByteUtils.splice(data, 10, 12, offset)));
        header.setPacketType(ByteUtils.byte2short(ByteUtils.splice(data, 12, 14, offset)));
        header.setPacketLength(ByteUtils.byteToInt(ByteUtils.splice(data, 14, 18, offset)));


        short destinationCount = ByteUtils.byte2short(ByteUtils.splice(data, 18, 20, offset)); // 6
        List<byte[]> destinationList = new ArrayList<>();
        destinationList.add(ByteUtils.splice(data, 20, 20 + destinationCount, offset));
        short orderID = ByteUtils.byte2short(ByteUtils.splice(data, 20 + destinationCount,
                22 + destinationCount, offset));
        switch (orderID) {
            case 0x0001:
                // 开始播发
                byte dataType = data[34 + destinationCount];
                switch (dataType) {
                    case 0x01:
                        // 音频
                    case 0x02:
                        // 视频
                    case 0x03:
                        // 文字
                    case 0x04:
                        // 图片
                        EmergencyProtocol<BaseMessage> protocol = new EmergencyProtocol<>();
                        EmergencyBody<BaseMessage> body = new EmergencyBody<>();
                        body.setDestinationCount(destinationCount);
                        body.setDestinationAddressList(destinationList);
                        body.setOrderID(orderID);
                        BaseMessage baseMessage = new BaseMessage();
                        baseMessage.setOrderLength(ByteUtils.byte2short(ByteUtils.splice(data, 22 + destinationCount, 24 + destinationCount, offset)));
                        baseMessage.setBroadcastType(ByteUtils.splice(data, 24 + destinationCount, 25 + destinationCount, offset)[0]);
                        baseMessage.setLevel(ByteUtils.splice(data, 25 + destinationCount, 26 + destinationCount, offset)[0]);
                        baseMessage.setStartTime(ByteUtils.byteToInt(ByteUtils.splice(data, 26 + destinationCount, 30 + destinationCount, offset)));
                        baseMessage.setEndTime(ByteUtils.byteToInt(ByteUtils.splice(data, 30 + destinationCount, 34 + destinationCount, offset)));
                        baseMessage.setDataType(ByteUtils.splice(data, 34 + destinationCount, 35 + destinationCount, offset)[0]);
                        baseMessage.setDataLength(ByteUtils.byteToInt(ByteUtils.splice(data, 35 + destinationCount, 39 + destinationCount, offset)));
                        baseMessage.setData(ByteUtils.splice(data, 39 + destinationCount, 39 + destinationCount + baseMessage.getDataLength(), offset));
                        body.setT(baseMessage);
                        protocol.setHeader(header);
                        protocol.setBody(body);
                        protocol.setCrc32(ByteUtils.byteToInt(ByteUtils.splice(data,
                                header.getPacketLength() - 4, header.getPacketLength(), offset)));

                        return protocol;
                }
                break;
            case 0x002:
                // 停止播发
                return null;
            default:
                return null;
        }

        return null;

    }


}
