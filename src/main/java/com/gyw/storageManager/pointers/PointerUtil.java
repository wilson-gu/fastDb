package com.gyw.storageManager.pointers;

import com.gyw.parms.Parm;
import com.gyw.util.ByteUtil;

public class PointerUtil {

    /**
     * read offset-len (commonly, indirect pointer) from buffer
     * @param buf : buffer
     * @param pointer : pointer points to data of offset-len on buffer
     * @return offset-len obtained
     */
    public static OffsetLen getOffsetLen(byte[] buf, int pointer) {

        // read offset field
        short offset = ByteUtil.readShortValueFromByteBuffer(buf, pointer);
        pointer += Parm.OFFSET_POINTER_LEN;

        // read len field
        short len = ByteUtil.readShortValueFromByteBuffer(buf, pointer);

        return new OffsetLen(offset, len);
    }


    /**
     * write offset-len (commonly, indirect pointer) to buffer
     * @param buf : buffer
     * @param pointer : pointer points to data of offset-len on buffer
     * @param offsetLen : offset-len object
     */
    public static void setOffsetLen(byte[] buf, int pointer, OffsetLen offsetLen) {

        // write offset field
        ByteUtil.writeShortValueToByteBuffer(buf, pointer, offsetLen.getOffset());
        pointer += Parm.OFFSET_POINTER_LEN;

        // write len field
        ByteUtil.writeShortValueToByteBuffer(buf, pointer, offsetLen.getLen());
    }


    /**
     * read offset-len (commonly, indirect pointer) from buffer
     * @param buf : buffer
     * @param pointer : pointer points to data of offset-len on buffer
     * @return offset obtained
     */
    public static short getOffsetField(byte[] buf, int pointer) {
        // read offset field
        return ByteUtil.readShortValueFromByteBuffer(buf, pointer);
    }

    /**
     * read offset-len (commonly, indirect pointer) from buffer
     * @param buf : buffer
     * @param pointer : pointer points to data of offset-len on buffer
     * @return offset obtained
     */
    public static short getLenField(byte[] buf, int pointer) {
        pointer += Parm.OFFSET_POINTER_LEN;

        // read offset field
        return ByteUtil.readShortValueFromByteBuffer(buf, pointer);
    }


    /**
     * write offset-len (commonly, indirect pointer) to buffer
     * @param buf : buffer
     * @param pointer : pointer points to data of offset-len on buffer
     * @param offset : offset
     */
    public static void setOffsetField(byte[] buf, int pointer, short offset) {

        // write offset field
        ByteUtil.writeShortValueToByteBuffer(buf, pointer, offset);
    }


    /**
     * write offset-len (commonly, indirect pointer) to buffer
     * @param buf : buffer
     * @param pointer : pointer points to data of offset-len on buffer
     * @param len : len
     */
    public static void setLenField(byte[] buf, int pointer, short len) {
        pointer += Parm.OFFSET_POINTER_LEN;

        // write offset field
        ByteUtil.writeShortValueToByteBuffer(buf, pointer, len);
    }

    /**
     * write blockOffset to buffer
     * @param buf : buffer
     * @param pointer : pointer points to data of offset-len on buffer
     * @param blockOffset : blockOffset
     */
    @Deprecated
    public static void setBlockOffset(byte[] buf, int pointer, int blockOffset) {
        ByteUtil.writeIntValueToByteBuffer(buf, pointer, blockOffset);
    }

    /**
     * write blockOffset to buffer
     * @param buf buffer
     * @param offset buffer offset
     * @param blockPointer blockPointer
     */
    public static void setBlockPointer(byte[] buf, int offset, BlockPointer blockPointer) {
        ByteUtil.writeIntValueToByteBuffer(buf, offset, blockPointer.getBlockOffset());
    }

    /**
     * write blockOffset to buffer
     * @param buf buffer
     * @param offset buffer offset
     * @return block pointer
     */
    public static BlockPointer getBlockPointer(byte[] buf, int offset) {
         return new BlockPointer(ByteUtil.readIntValueFromByteBuffer(buf, offset));
    }

    /**
     * set record pointer
     * @param buf buffer
     * @param offset buffer offset
     * @param recordPointer recordPointer
     */
    public static void setRecordPointer(byte[] buf, int offset, RecordPointer recordPointer) {
        ByteUtil.writeIntValueToByteBuffer(buf, offset, recordPointer.getBlockPointer().getBlockOffset());
        offset += BlockPointer.LEN;
        ByteUtil.writeShortValueToByteBuffer(buf, offset, recordPointer.getRecordInnerPointer().getRecordInnerOffset());
    }

    /**
     * get record pointer
     * @param buf buffer
     * @param offset buffer offset
     * @return record pointer
     */
    public static RecordPointer getRecordPointer(byte[] buf, int offset) {
        int blockOffset = ByteUtil.readIntValueFromByteBuffer(buf, offset);
        offset += BlockPointer.LEN;
        int recordInnerOffset = ByteUtil.readShortValueFromByteBuffer(buf, offset);
        return new RecordPointer(blockOffset, recordInnerOffset);
    }
}
