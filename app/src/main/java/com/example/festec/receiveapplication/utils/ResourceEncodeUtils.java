package com.example.festec.receiveapplication.utils;

import android.content.Context;



public class ResourceEncodeUtils {

    private static byte[] sourceAddress;

    public static byte[] getSourceAddress(Context context) {
        return new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C};
//        //先得到省、市、县地址(从PreferShared中获取)
//        String deviceAddress = PrefUtils.getString(context, "deviceAddress", "");
//        //根据地址获得资源级别识别码(1中央级，2省级，3市级，4县级，5乡级，6村级)，1个字节
//        Map<String, String> map = AddressParseUtils.addressResolution(deviceAddress);
//        byte[] resourceFlag = new byte[1];
//        if (!map.get("province").equals("")) {
//            resourceFlag[0] = 0x02;
//        }
//        if (!map.get("city").equals("")) {
//            resourceFlag[0] = 0x03;
//        }
//        if (!map.get("county").equals("")) {
//            resourceFlag[0] = 0x04;
//        }
//        //根据地址去获得地区编码（6个字节）
//        //查询地区编码
//        //创建DBOpenHelper对象,指定名称、版本号并保存在databases目录下
//        dbOpenHelper = new DBOpenHelper(context, "resource.db", null, 1);
//        //查询单词
//        Cursor cursor = dbOpenHelper.getReadableDatabase().query("tb_resource", null,   //查询的列，null表示查询所有列
//                "address = ?", new String[]{deviceAddress}, null, null, null);
//        String resourceCode = "";
//        while (cursor.moveToNext()) {  // 遍历Cursor结果集
//            resourceCode = cursor.getString(2);
//            System.out.println("resourceCode:" + resourceCode);
//        }
//        if (!resourceCode.equals("")) {
//            byte[] resource = ByteUtils.hexStringToBytes(resourceCode);
//            System.out.println("resource:" + ByteUtils.bytesToHexString(resource));
//            sourceAddress = ByteUtils.addBytes(resourceFlag, resource);
//            //资源类型码(2个字节)  0x03,0x99  多类型台站/前端
//            sourceAddress = ByteUtils.addBytes(sourceAddress, new byte[]{0x03, (byte) 0x99});
//            //资源类型顺序码,1个字节  01~99
//            sourceAddress = ByteUtils.addBytes(sourceAddress, new byte[]{0x01});
//            //资源子类型,1个字节  0x04        终端
//            sourceAddress = ByteUtils.addBytes(sourceAddress, new byte[]{0x04});
//            //资源子类型顺序码,1个字节  01~99
//            sourceAddress = ByteUtils.addBytes(sourceAddress, new byte[]{0x01});
//            System.out.println("sourceAddress:" + Arrays.toString(sourceAddress));
//            return sourceAddress;
//        }
//        return null;
    }
}
