package com.gyw.storageManager.pointers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Block pointer class
 */
@ToString
@AllArgsConstructor
@Getter
public class RecordInnerPointer extends BaseFilePointer {

    public static final int LEN = 2;

    private short recordInnerOffset;

    public RecordInnerPointer(int recordInnerOffset) {
        this.recordInnerOffset = (short)recordInnerOffset;
    }

    /**
     * Copy constructor
     * @param recordInnerPointer
     */
    public RecordInnerPointer(RecordInnerPointer recordInnerPointer) {
        this.recordInnerOffset = recordInnerPointer.getRecordInnerOffset();
    }

    /**
     * pointer increment
     * @param increment
     */
    public void incr(int increment) {
        recordInnerOffset += (short) increment;
    }

    /**
     * pointer decrement
     * @param decrement
     */
    public void decr(int decrement) {
        recordInnerOffset -= (short) decrement;
    }

    /**
     * seek to specific place
     * @param pos
     */
    public void seek(int pos) {
        recordInnerOffset = (short)pos;
    }

    /**
     * seek to specific place
     */
    public void seek(RecordInnerPointer pointer) {
        recordInnerOffset = pointer.getRecordInnerOffset();
    }

    /**
     * difference of record inner pointers
     * @return
     */
    public int diff(RecordInnerPointer recordInnerPointer) {
        return this.recordInnerOffset - recordInnerPointer.getRecordInnerOffset();
    }

    /**
     * get pointer length in bytes
     *
     * @return pointer length
     */
    @Override
    public int getLength() {
        return LEN;
    }
}
