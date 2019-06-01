package com.example.festec.receiveapplication.message;


import com.example.festec.receiveapplication.utils.ByteUtils;

import java.util.Arrays;


public class QueryMessage {

    private int orderLength;
    //长度：N个字节，每个参数1个字节
    //1：终端资源编码；2：物理地址编码；3：工作状态；4：故障代码；5：设备类型；6：硬件版本号；7：软件版本号
    private byte queryCount; //查询参数数量
    private byte[] queryParams;

    public byte[] getQueryMessageBytes() {
        byte[] bytes1 = {queryCount};
        byte[] bytes = ByteUtils.addBytes(ByteUtils.intToByte(orderLength), bytes1);
        bytes = ByteUtils.addBytes(bytes, queryParams);
        return bytes;
    }

    public int getOrderLength() {
        return orderLength;
    }

    public void setOrderLength(int orderLength) {
        this.orderLength = orderLength;
    }

    public byte getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(byte queryCount) {
        this.queryCount = queryCount;
    }

    public byte[] getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(byte[] queryParams) {
        this.queryParams = queryParams;
    }

    public int getLength() {
        return getQueryMessageBytes().length;
    }

    @Override
    public String toString() {
        return "QueryMessage{" +
                "queryCount=" + queryCount +
                ", queryParams=" + Arrays.toString(queryParams) +
                '}';
    }
}
