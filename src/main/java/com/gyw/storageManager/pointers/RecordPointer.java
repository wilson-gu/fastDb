package com.gyw.storageManager.pointers;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Position of a record
 * blockOffset : 4bytes
 * recordPosition : 4bytes
 * total : 8bytes
 * @author guyw
 */
@Getter
@AllArgsConstructor
public class RecordPointer extends BaseFilePointer {

    /**
     * length
     */
    public static final int LEN = BlockPointer.LEN + RecordInnerPointer.LEN;

    /**
     * block pointer
     */
    private BlockPointer blockPointer;

    /**
     * record inner pointer (within a block)
     */
    private RecordInnerPointer recordInnerPointer;


    /**
     * constructor
     * @param blockOffset
     * @param recordInnerOffset
     */
    public RecordPointer(int blockOffset, int recordInnerOffset) {
        blockPointer = new BlockPointer(blockOffset);
        recordInnerPointer = new RecordInnerPointer(recordInnerOffset);
    }

    @Override
    public String toString() {
        String blockPosHex = "0x" + Integer.toHexString(blockPointer.getBlockOffset()).toUpperCase();
        String recordPosHex = "0x" + Integer.toHexString(recordInnerPointer.getRecordInnerOffset()).toUpperCase();
        return "RecordPointer(" + blockPosHex + ", "
                + recordPosHex + ")";
    }

    /**
     * get pointer length in bytes
     *
     * @return pointer length
     */
    @Override
    public int getLength() {
        return BlockPointer.LEN + RecordInnerPointer.LEN;
    }
}
