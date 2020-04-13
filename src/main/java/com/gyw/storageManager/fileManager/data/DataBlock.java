package com.gyw.storageManager.fileManager.data;

import com.gyw.storageManager.fileManager.block.Block;
import com.gyw.storageManager.fileManager.block.BlockSerializable;
import com.gyw.storageManager.fileManager.block.BlockStatus;
import com.gyw.storageManager.fileManager.block.BlockType;
import com.gyw.storageManager.pointers.RecordPointer;
import com.gyw.storageManager.pointers.OffsetLen;
import com.gyw.storageManager.fileManager.data.record.Record;
import com.gyw.storageManager.fileManager.data.record.RecordData;
import com.gyw.storageManager.fileManager.tablespace.TableMeta;
import com.gyw.storageManager.pointers.BlockPointer;
import com.gyw.storageManager.pointers.RecordInnerPointer;
import com.gyw.util.ByteUtil;
import com.gyw.util.FieldUtil;
import com.gyw.storageManager.pointers.PointerUtil;
import com.gyw.util.lambda.IntegerPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.gyw.parms.Parm.OFFSET_LEN_POINTER_LEN;
import static com.gyw.parms.Parm.OFFSET_POINTER_LEN;

/**
 * Data block
 * @author guyw
 */
public class DataBlock extends Block {

    private static final Logger logger = LoggerFactory.getLogger(DataBlock.class);

    /**
     * Block size
     */
    public static final int LEN = 8 * 1024;

    /**
     * entry number length
     */
    private static final int ENTRY_NUM_LEN = 2;

    /**
     * offset of entry number
     */
    private static final int OFFSET_ENTRY_NUM = 0;

    /**
     * offset of free end pointer
     */
    private static final int OFFSET_FREE_END_POINTER = ENTRY_NUM_LEN;

    @Override
    protected BlockType getBlockType() {
        return BlockType.DATA;
    }

    public DataBlock(BlockPointer blockPointer) {
        super(blockPointer);
    }

    /**
     * read buffer and get blockObj operation
     *
     * @return block object
     */
    @Override
    protected BlockSerializable readOp(BlockPointer blockPointer) {
        // TODO...
        return null;
    }


    /**
     * get free end pointer operation
     */
    private short getFreeEndPointerOp() {
        return ByteUtil.readShortValueFromByteBuffer(buf, OFFSET_FREE_END_POINTER);
    }

    /**
     * set free end pointer operation
     * @param freeEndPointerVal free space end pointer value
     */
    private void setFreeEndPointerOp(short freeEndPointerVal) {
        ByteUtil.writeShortValueToByteBuffer(buf, OFFSET_FREE_END_POINTER, freeEndPointerVal);
    }

    /**
     * get entry number operation
     */
    private short getEntryNumOp() {
        return ByteUtil.readShortValueFromByteBuffer(buf, 0);
    }

    /**
     * set entry number operation
     * @param entryNum : entry number
     */
    private void setEntryNumOp(short entryNum) {
        ByteUtil.writeShortValueToByteBuffer(buf, 0, entryNum);
    }

    /**
     * read record from buffer operation
     * @param pointer record inner pointer
     * @param tableMeta table meta
     */
    public RecordData getRecordData(final RecordInnerPointer pointer, TableMeta tableMeta) {
        try {
            // get read lock
            lock.readLock().lock();

            // check block status
            if (blockStatus == BlockStatus.NULL) {
                return null;
            }

            // get record data
            return getRecordDataOp(pointer, tableMeta);
        } finally {
            // drop read lock
            lock.readLock().unlock();
        }
    }

    /**
     * read record from buffer operation
     * @param pointer record inner pointer
     * @param tableMeta table meta
     */
    public RecordData getRecordDataOp(final RecordInnerPointer pointer, TableMeta tableMeta) {
        // check arguments
        Objects.requireNonNull(tableMeta, "tableMeta is null!");

        // RecordInnerPointer originalPointer = new RecordInnerPointer(pointer);

        // fixed-len field read pointer
        IntegerPointer fixedLenRdPointer = new IntegerPointer(pointer.getRecordInnerOffset()
                + tableMeta.getVaryLenFieldsNum() * OffsetLen.LEN);

        // vary-len field read pointer
        IntegerPointer varyLenRdPointer = new IntegerPointer(pointer.getRecordInnerOffset());

        // nullBitMap read pointer
        IntegerPointer nullBitMapRdPointer = new IntegerPointer(pointer.getRecordInnerOffset()
                + tableMeta.getVaryLenFieldsNum() * OffsetLen.LEN
                + tableMeta.getFixedLenFieldsSize());

        // new record data
        RecordData recordData = new RecordData();

        // bit pointer, initial set to 0
        IntegerPointer bitPointer = new IntegerPointer(0);

        // read fixed fields from buffer
        tableMeta.foreachFixedField(fieldMeta -> {
            // get field null-value
            int nullVal = ByteUtil.readBitFromByteBuffer(
                    buf,
                    nullBitMapRdPointer.getValue(),
                    bitPointer.getValue()
            );

            bitPointer.incr();

            if (nullVal == 0) {
                // field is NOT null
                // read field
                recordData.setFieldValue(
                        fieldMeta.getFieldName(),
                        FieldUtil.bytesToField(
                                buf,
                                fixedLenRdPointer.getValue(),
                                fieldMeta.getLength(),
                                fieldMeta)
                );
            } else {
                // field is null
                recordData.setFieldValue(fieldMeta.getFieldName(), null);
            }

            fixedLenRdPointer.incrBy(fieldMeta.getLength());
        });


        // read vary fields first from buffer
        tableMeta.foreachVaryField(fieldMeta -> {
            // get field null-value
            int nullVal = ByteUtil.readBitFromByteBuffer(
                    buf,
                    nullBitMapRdPointer.getValue(),
                    bitPointer.getValue()
            );

            bitPointer.incr();

            if (nullVal == 0) {
                // field is NOT null
                // read field offset-len
                OffsetLen offsetLen = PointerUtil.getOffsetLen(buf, varyLenRdPointer.getValue());
                varyLenRdPointer.incrBy(OffsetLen.LEN);

                // read field data
                recordData.setFieldValue(
                        fieldMeta.getFieldName(),
                        FieldUtil.bytesToField(
                                buf,
                                offsetLen.getOffset() + pointer.getRecordInnerOffset(),
                                offsetLen.getLen(),
                                fieldMeta)
                );
            } else {
                // field is null
                recordData.setFieldValue(fieldMeta.getFieldName(), null);
            }
        });

        return recordData;
    }


    /**
     * insert a record into block cache
     * @param record record to be inserted
     * @return  record pointer of inserted record
     */
    public RecordPointer insertRecord(Record record) {
        // fill byte arrays of record fields
        record.fillBytesMap();

        try {
            // get write lock
            lock.writeLock().lock();

            if (blockStatus == BlockStatus.NULL) {
                throw new RuntimeException("Block status must not be BlockStatus.NULL" +
                        " when inserting a record!");
            }

            RecordInnerPointer recordInnerPointer = insertRecordOp(record);

            // change block status
            blockStatus = BlockStatus.UPDATED;

            // return recordPointer
            RecordPointer recordPointer = new RecordPointer(blockPointer, recordInnerPointer);
            return recordPointer;
        } finally {
            // drop write lock
            lock.writeLock().unlock();
        }
    }

    /**
     * insert record operation
     * @param record record to be inserted
     * @return recordInnerPointer of record inserted on buffer
     */
    private RecordInnerPointer insertRecordOp(Record record) {
        // get old entry number
        short oldEntryNum = getEntryNumOp();

        // entryNUm++ and write new entryNum into cache
        setEntryNumOp((short)(oldEntryNum+1));

        int pointer = getFreeEndPointerOp() - record.getTotalBytesNum();

        // write new free end pointer to cache
        ByteUtil.writeShortValueToByteBuffer(buf, OFFSET_FREE_END_POINTER, (short)pointer);

        // move increment
        pointer++;

        // write record data
        record.writeOnBuffer(buf, new RecordInnerPointer(pointer));

        // calc new record pointer
        int outerPointer = oldEntryNum * OFFSET_LEN_POINTER_LEN
                + ENTRY_NUM_LEN
                + OFFSET_POINTER_LEN;

        // prepare offsetLen object
        OffsetLen offsetLen = new OffsetLen((short)pointer, (short)record.getTotalBytesNum());

        // write offset-len to cache
        PointerUtil.setOffsetLen(buf, outerPointer, offsetLen);

        return new RecordInnerPointer(pointer);
    }


    /**
     * delete record from block cache
     * @param outerPtOfDelEntry : outer pointer of delete entry
     */
    public void deleteRecord(int outerPtOfDelEntry) {
        try {
            // get write lock
            lock.writeLock().lock();

            if (blockStatus == BlockStatus.NULL) {
                throw new RuntimeException("Block status must not be BlockStatus.NULL" +
                        " when inserting a record!");
            }

            deleteRecordOp(outerPtOfDelEntry);

            // change block status
            blockStatus = BlockStatus.UPDATED;
        } finally {
            // drop write lock
            lock.writeLock().unlock();
        }
    }


    /**
     * create empty block operation
     */
    @Override
    protected void createEmptyBlockOp() {
        super.createEmptyBlockOp();

        // write empty block header
        // write entryNum = 0
        ByteUtil.writeShortValueToByteBuffer(buf, OFFSET_ENTRY_NUM, (short)0);

        // write freeEndPointer = BLOCK_SIZE - 1
        setFreeEndPointerOp((short)(DataBlock.LEN -1));
    }

    /**
     * get block length
     *
     * @return block length
     */
    @Override
    protected int getBlockLen() {
        return DataBlock.LEN;
    }


    /**
     * delete record operation
     * @param outerPtOfDelEntry
     */
    private void deleteRecordOp(int outerPtOfDelEntry) {
        int endOuterPt = getEntryNumOp() * OFFSET_LEN_POINTER_LEN
                + ENTRY_NUM_LEN
                + OFFSET_POINTER_LEN;

        int outerPt = outerPtOfDelEntry;

        // get delete entry
        OffsetLen delEntry = PointerUtil.getOffsetLen(buf, outerPtOfDelEntry);

        // delete record length
        short delRcrdLen = delEntry.getLen();

        // update delete entry to (-1, -1)
        PointerUtil.setOffsetLen(buf, outerPtOfDelEntry, new OffsetLen(-1, -1));

        // pointer move forward
        outerPt += OFFSET_LEN_POINTER_LEN;

        // change afterwards entries
        while (outerPt < endOuterPt) {
            // pass those deleted records
            if (isRecordDeleted(outerPt)) {
                // pointer move forward
                outerPt += OFFSET_LEN_POINTER_LEN;
                continue;
            }

            OffsetLen entry = PointerUtil.getOffsetLen(buf, outerPt);

            // shift record to right by delRcrdLen
            shiftRcrdRightWardsByLen(
                    entry.getOffset(),
                    entry.getLen(),
                    delRcrdLen);

            // update entry offset
            PointerUtil.setOffsetField(buf, outerPt, (short) (entry.getOffset()+delRcrdLen));

            // pointer move forward
            outerPt += OFFSET_LEN_POINTER_LEN;
        }

        // update freeEndPointer
        setFreeEndPointerOp((short) (getFreeEndPointerOp() + delRcrdLen));
    }


    /**
     * shift record rightwards by length
     * @param rcrdOldOffset
     * @param rcrdLen
     * @param shiftLen
     * @return
     */
    private void shiftRcrdRightWardsByLen(short rcrdOldOffset, short rcrdLen, short shiftLen) {
        System.arraycopy(buf, rcrdOldOffset, buf, rcrdOldOffset+shiftLen, rcrdLen);
    }


    /**
     * is record deleted
     * @param outerPtOfEntry : outer pointer of the entry
     */
    private boolean isRecordDeleted(int outerPtOfEntry) {
        outerPtOfEntry += OFFSET_POINTER_LEN;
        short len = ByteUtil.readShortValueFromByteBuffer(buf, outerPtOfEntry);
        return len == 0;
    }
}
