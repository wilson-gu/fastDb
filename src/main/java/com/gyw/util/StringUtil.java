package com.gyw.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class StringUtil {

    /**
     * value field in String class
     */
    private static Field valueField;

    static {
        try {
            valueField = String.class.getDeclaredField("value");
            valueField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get direct bytes from string
     * @param str string
     * @param dest destination byte array
     * @param destPos destination start position
     */
    public static void getBytes(String str, byte[] dest, int destPos) {
        try {
            // get internal char array reference of str
            char[] value = (char[]) valueField.get(str);

            // check
            if (destPos + value.length * 2 > dest.length) {
                throw new IndexOutOfBoundsException("destPos + len > dest.length");
            }

            Unsafe unsafe = UnsafeHelper.getUnsafe();

            // copy into dest
            unsafe.copyMemory(
                    value,
                    unsafe.arrayBaseOffset(char[].class),
                    dest,
                    unsafe.arrayBaseOffset(byte[].class) + destPos,
                    value.length * 2
                    );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get bytes number
     * @param str string
     * @return bytes number of the string
     */
    public static int getBytesNum(String str) {
        try {
            // get internal char array reference of str
            char[] value = (char[]) valueField.get(str);
            return value.length * 2;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * bytes to string
     * @param src bytes array source
     * @param offset offset
     * @param bytesNum bytes number
     * @return string
     */
    public static String bytesToStr(byte[] src, int offset, int bytesNum) {
        return new String(ByteUtil.byteArrayToCharArray(src, 0, bytesNum ));
    }


    public static void main(String[] args) {
        String str = "abc";
        int bytesNum = getBytesNum(str);
        System.out.println("bytesNum = " + bytesNum);

        byte[] bytes = new byte[50];
        getBytes(str, bytes, 0);

        System.out.println(bytesToStr(bytes, 0, bytesNum));
    }

}
