package com.example.festec.receiveapplication.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

public class CRC32Utils {

    private static final int BUFFER_SIZE = 512;

    /**
     * 编码
     * @param data
     * @return
     */
    public static int encode(byte[] data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return (int) crc32.getValue();
    }

    /**
     * 编码
     * @param data
     * @return
     */
    public static int encode(InputStream data) {
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            int read = data.read(buffer, 0, BUFFER_SIZE);
            CRC32 crc32 = new CRC32();
            while (read > -1) {
                crc32.update(buffer, 0, read);
                read = data.read(buffer, 0, BUFFER_SIZE);
            }
            return (int) crc32.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
