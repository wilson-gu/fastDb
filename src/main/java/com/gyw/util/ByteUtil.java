package com.gyw.util;

import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.util.Arrays;

/** 
 * Description: ByteUtil <p>
 * Created  By: GuYiwei         <br>
 *          At: 2019年6月12日  		上午10:53:55  <p>
 * Modified By: GuYiwei         <br>
 *          At: 2019年6月12日  		上午10:53:55  <p>
 * @author GuYiwei (Yiwei.gu09@gmail.com)
 */
public class ByteUtil {

    private static final ThreadLocal<ByteBuffer> shortBuffers = new ThreadLocal<>();
    private static final ThreadLocal<ByteBuffer> intBuffers = new ThreadLocal<>();
    private static final ThreadLocal<ByteBuffer> longBuffers = new ThreadLocal<>();

    static {
        shortBuffers.set(ByteBuffer.allocate(Short.BYTES));
        intBuffers.set(ByteBuffer.allocate(Integer.BYTES));
        longBuffers.set(ByteBuffer.allocate(Long.BYTES));
    }

    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	
	/**
	 * Write long value to byte buffer
     * long value consists of 8 bytes
	 */
	public static int writeLongValueToByteBuffer(byte[] buf, int offset, long num) {
		buf[offset++] = (byte)(num >> 56);
		buf[offset++] = (byte)(num >> 48);
		buf[offset++] = (byte)(num >> 40);
		buf[offset++] = (byte)(num >> 32);
		buf[offset++] = (byte)(num >> 24);
		buf[offset++] = (byte)(num >> 16);
		buf[offset++] = (byte)(num >> 8);
		buf[offset++] = (byte) num;

		return offset;
	}

    /**
     * Read long value from byte buffer
     * int value consists of 8 bytes
     */
    public static long readLongValueFromByteBuffer(byte[] buf, int offset) {
        ByteBuffer buffer = longBuffers.get();
        buffer.clear();
        buffer.put(buf, offset, Long.BYTES);
        buffer.flip();
        return buffer.getLong();
    }

    /**
     * Write int value to byte buffer
     * int value consists of 4 bytes
     */
    public static int writeIntValueToByteBuffer(byte[] buf, int offset, int num) {
        buf[offset++] = (byte)(num >> 24);
        buf[offset++] = (byte)(num >> 16);
        buf[offset++] = (byte)(num >> 8);
        buf[offset++] = (byte) num;

        return offset;
    }

    /**
     * Read int value from byte buffer
     * int value consists of 4 bytes
     */
    public static int readIntValueFromByteBuffer(byte[] buf, int offset) {
        ByteBuffer buffer = intBuffers.get();
        buffer.clear();
        buffer.put(buf, offset, Integer.BYTES);
        buffer.flip();
        return buffer.getInt();
    }

    /**
     * Write short value to byte buffer
     * int value consists of 2 bytes
     */
    public static int writeShortValueToByteBuffer(byte[] buf, int offset, short num) {
        buf[offset++] = (byte)(num >> 8);
        buf[offset++] = (byte) num;

        return offset;
    }

    /**
     * Read int value from byte buffer
     * int value consists of 4 bytes
     */
    public static short readShortValueFromByteBuffer(byte[] buf, int offset) {
        ByteBuffer buffer = intBuffers.get();
        buffer.clear();


        buffer.put(buf, offset, Short.BYTES);
        buffer.flip();
        return buffer.getShort();
    }

    /**
     * Write byte value into buffer
     */
    public static int writeByteToBuffer(byte[] buf, int offset, byte b) {
        buf[offset++] = b;
        return offset;
    }

    /**
     * Write byte value into buffer
     * the lower bits in byteInt will be written
     */
    public static int writeByteToBuffer(byte[] buf, int offset, int byteInt) {
        buf[offset++] = (byte)byteInt;
        return offset;
    }

    /**
     * read byte value from buffer
     * 1bit will be read
     */
    public static byte readByteFromBuffer(byte[] buf, int offset) {
        return buf[offset];
    }

    /**
     * Write string to byte buffer
     */
    public static int writeStringToByteBuffer(byte[] buf, int offset, String str) {
        byte[] strBytes = str.getBytes();
        System.arraycopy(strBytes, 0, buf, offset, strBytes.length);
        return offset + strBytes.length;
    }

    /**
     * Read string from byte buffer
     */
    public static String readStringFromByteBuffer(byte[] buf, int offset, int readLen) {
        byte[] tmp = new byte[readLen];
        System.arraycopy(buf, offset, tmp, 0, readLen);

        return (new String(tmp)).replace("\u0000", "");
    }

//    /**
//     * Write string to byte buffer
//     */
//    public static int writeFixedLenStringToByteBuffer(byte[] buf, int offset, String str) {
//        byte[] strBytes = str.getBytes();
//        System.arraycopy(strBytes, 0, buf, offset, strBytes.length);
//        return offset + strBytes.length;
//    }
//
//    /**
//     * Read string from byte buffer
//     */
//    public static String readFixedLenStringFromByteBuffer(byte[] buf, int offset, int readLen) {
//        byte[] tmp = new byte[readLen];
//        System.arraycopy(buf, offset, tmp, 0, readLen);
//
//        String str = new String(tmp);
//        if (str.hashCode() == 0) {
//            return null;
//        } else {
//            return str;
//        }
//    }

    /**
     * Read 1bit from byte buffer
     * @param buf buffer
     * @param offset byte offset on buffer
     * @param kthBit k-th bit from offset (zero base)
     * @return 1 or 0
     */
    public static int readBitFromByteBuffer(byte[] buf, int offset, int kthBit) {
        byte b = Arrays.copyOfRange(buf, offset, offset+1)[0];
        return (b >> kthBit) & 1;
    }

    /**
     * Write 1bit to byte buffer
     * @param buf buffer
     * @param offset byte offset on buffer
     * @param kthBit k-th bit from offset (zero base)
     * @param bitVal bit value(0 or 1)
     */
    public static void writeBitToByteBuffer(byte[] buf, int offset, int kthBit, int bitVal) {
        if (bitVal == 1) {
            buf[offset] = (byte) (buf[offset] | (1 << kthBit));
        } else if (bitVal == 0) {
            buf[offset] = (byte) (buf[offset] & ~(1 << kthBit));
        } else {
            throw new IllegalArgumentException("bitVal only can be 0 or 1. bitVal = [" + bitVal + "]");
        }
    }

    public static String byteToHex(byte b) {
        char[] buf = new char[2];

        buf[0] = HEX_CHAR[b >> 4 & 0xF];
        buf[1] = HEX_CHAR[b & 0xF];

        return new String(buf);
    }

    /**
     * convert bytes to its Hex string representation
     * @param bytes
     * @param offset
     * @param len
     * @return
     */
    public static String bytesToHex(byte[] bytes, int offset, int len) {
        // exceed boundary
        if (offset + len > bytes.length) {
            throw new ArrayIndexOutOfBoundsException(offset + len);
        }

        // allocate buffer
        int charLen = 2 * len;
        char[] buf = new char[charLen];

        int index = 0;
        int byteIndex = 0;
        while (byteIndex < len) {
            byte b = bytes[offset + byteIndex++];
            buf[index++] = HEX_CHAR[b >> 4 & 0xF];
            buf[index++] = HEX_CHAR[b & 0xF];
        }

        return new String(buf);
    }

    /**
     * convert bytes to its Hex string representation
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, 0, bytes.length);
    }

    /**
     * Convert byte array to char array
     * @param bytes
     * @param offset
     * @param len
     * @return char array
     */
    public static char[] byteArrayToCharArray(byte[] bytes, int offset, int len) {
        // check len is even
        if (len % 2 != 0) {
            throw new IllegalArgumentException("argument len must be even!");
        }

        // check exceed boundary
        if (offset + len > bytes.length) {
            throw new ArrayIndexOutOfBoundsException(offset + len);
        }

        char[] outputBuf = new char[len / 2];

        Unsafe unsafe = UnsafeHelper.getUnsafe();

        unsafe.copyMemory(
                bytes,
                unsafe.arrayBaseOffset(byte[].class),
                outputBuf,
                unsafe.arrayBaseOffset(char[].class),
                len);

        return outputBuf;
    }

    /**
     * Convert byte array to char array
     * @param bytes
     * @return
     */
    public static char[] byteArrayToCharArray(byte[] bytes) {
        return byteArrayToCharArray(bytes, 0, bytes.length);
    }
}
