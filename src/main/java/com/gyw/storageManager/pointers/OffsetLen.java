package com.gyw.storageManager.pointers;

import com.gyw.parms.Parm;
import com.gyw.util.dataStructure.Pair;

public class OffsetLen extends Pair<Short, Short> {

    public static final int LEN = Parm.OFFSET_POINTER_LEN + Parm.LEN_POINTER_LEN;

    public OffsetLen(short offset, short len) {
        super(offset, len);
    }

    public OffsetLen(int offset, int len) {
        super((short)offset, (short)len);
    }

    public short getOffset() {
        return first;
    }

    public short getLen() {
        return second;
    }
}
