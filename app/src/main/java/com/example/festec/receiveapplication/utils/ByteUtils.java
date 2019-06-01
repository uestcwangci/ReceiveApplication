package com.example.festec.receiveapplication.utils;

public class ByteUtils {
    //合并两个byte数组
    public static byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    // 对数组切片从start（含）到end（不含)
    public static byte[] splice(byte[] data, int start, int end, int offset) {
        start = start + offset;
        end = end + offset;
        byte[] bytes = new byte[end - start];
        for (int i = start; i < end; i++) {
            bytes[i - start] = data[i];
        }
        return bytes;
    }


    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    //将整数转化为字节数组,注意转型
    public static byte[] intToByte(int data1){
        byte[] data2 = new byte[4];
        data2[3] = (byte) (data1 & 0xFF);
        data2[2] = (byte) (data1 >> 8 & 0xFF);
        data2[1] = (byte) (data1 >> 16 & 0xFF);
        data2[0] = (byte) (data1 >> 24 & 0xFF);
        return data2;
    }

    // 将4个字节的byte转为int
    public static int byteToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    //将数组转化为十六进制
    public static String bytesToHexString(byte[] data1){
        StringBuilder stringBuilder = new StringBuilder("");
        if (data1 == null || data1.length <= 0) {
            return null;
        }
        for (int i = 0; i < data1.length; i++) {
            int v = data1[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static byte[] short2Byte(short x) {
        byte high = (byte) (0x00FF & (x>>8));
        byte low = (byte) (0x00FF & x);
        byte[] bytes = new byte[2];
        bytes[0] = high;
        bytes[1] = low;
        return bytes;
    }



    public static short byte2short(byte[] bytes) {
        byte high = bytes[0];
        byte low = bytes[1];
        return (short) (((high & 0x00FF) << 8) | (0x00FF & low));
    }


    public static byte[] macToByte(String mac) {
        byte[] bytes = new byte[6];
        String[] macs = mac.split(":");
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = hexStringToBytes(macs[i])[0];
        }
        return bytes;
    }
}

