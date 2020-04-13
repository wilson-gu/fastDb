package com.gyw.storageManager.pointers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Block pointer class
 * @author guyw
 */
@ToString
@AllArgsConstructor
@Getter
public class BlockPointer extends BaseFilePointer {

    public static final int LEN = 4;

    private int blockOffset;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockPointer
                && this.blockOffset == ((BlockPointer) obj).getBlockOffset()) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return blockOffset;
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
