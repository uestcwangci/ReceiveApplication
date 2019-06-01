package com.example.festec.receiveapplication.protocol;


import com.example.festec.receiveapplication.utils.ByteUtils;

public class EmergencyProtocol<T> {
    private EmergencyHeader header;
    private EmergencyBody<T> body;
    private int crc32;

    public EmergencyHeader getHeader() {
        return header;
    }

    public void setHeader(EmergencyHeader header) {
        this.header = header;
    }

    public EmergencyBody<T> getBody() {
        return body;
    }

    public void setBody(EmergencyBody<T> body) {
        this.body = body;
    }

    public int getCrc32() {
        return crc32;
    }

    public void setCrc32(int crc32) {
        this.crc32 = crc32;
    }

    public byte[] getEmergencyProtocolByte() {
        byte[] bytes = ByteUtils.addBytes(header.getHeaderBytes(), body.getEmergencyBodyBytes());
        bytes = ByteUtils.addBytes(bytes, ByteUtils.intToByte(crc32));
        return bytes;
    }

    @Override
    public String toString() {
        return "EmergencyProtocol{" +
                "header=" + header +
                ", body=" + body +
                ", crc32=" + crc32 +
                '}';
    }
}
