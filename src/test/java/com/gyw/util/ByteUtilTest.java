package com.gyw.util;

public class ByteUtilTest {
    public static void main(String[] args) {
        byte[] buf = new byte[1024];
        ByteUtil.writeShortValueToByteBuffer(buf, 100, (short)32767);
        System.out.println(ByteUtil.readShortValueFromByteBuffer(buf, 100));


        ByteUtil.writeIntValueToByteBuffer(buf, 300, (int)1024);
        System.out.println(ByteUtil.readIntValueFromByteBuffer(buf, 300));


        ByteUtil.writeLongValueToByteBuffer(buf, 500, 9223372036854775807L);
        System.out.println(ByteUtil.readLongValueFromByteBuffer(buf, 500));

        System.out.println(ByteUtil.readBitFromByteBuffer(buf, 500, 2));
        ByteUtil.writeBitToByteBuffer(buf, 500, 2, 0);
        System.out.println(ByteUtil.readBitFromByteBuffer(buf, 500, 2));
    }
}
