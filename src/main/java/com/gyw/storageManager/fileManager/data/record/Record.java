package com.gyw.storageManager.fileManager.data.record;

import com.gyw.storageManager.pointers.OffsetLen;
import com.gyw.storageManager.fileManager.tablespace.TableMeta;
import com.gyw.storageManager.pointers.RecordInnerPointer;
import com.gyw.util.ByteUtil;
import com.gyw.storageManager.pointers.PointerUtil;
import com.gyw.util.lambda.IntegerPointer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Table Record
 * @author guyw
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Record {

    /**
     * table meta definition
     */
    private TableMeta tableMeta;

    /**
     * record data
     */
    private RecordData recordData;


    /**
     * Constructor
     * @param tableMeta
     */
    public Record(TableMeta tableMeta) {
        this.tableMeta = tableMeta;
    }

    /**
     * insert/update a field
     * @param fieldName field name
     * @param fieldVal field value
     */
    public void setField(String fieldName, Object fieldVal) {
        // check if field name exist in table meta
        if (!tableMeta.isFieldNameExist(fieldName)) {
            throw new RuntimeException("Fieldname [" + fieldName + "] not found in tabLeSpace");
        }

        recordData.setFieldValue(fieldName, fieldVal);
    }

    /**
     * Write on buffer
     * @param buffer
     */
    private void writeOnBuffer(byte[] buffer, final int offset) {
        // check
        if (!recordData.isLegalFieldBytes(tableMeta)) {
            throw new RuntimeException("recordData field bytes not concord with tableMeta");
        }

        // allocate enough memory for nullBitMap
        int nullBitMapLen = (tableMeta.getFieldsNum()-1) / 8 + 1;
        byte[] nullBitMap = new byte[nullBitMapLen];

        // null list which stores 0 or 1
        List<Integer> nullList = new ArrayList<>(tableMeta.getFieldsNum());

        // buffer write pointer
        IntegerPointer bufWtPointer = new IntegerPointer(offset);
        // offset passes all offset-length parts
        // (which points to the vary-len fields)
        bufWtPointer.incrBy(tableMeta.getVaryLenFieldsNum() * OffsetLen.LEN);

        // write fixed-length fields to block cache
        tableMeta.foreachFixedField(fieldMeta -> {
            Object fieldVal = recordData.getFieldValue(fieldMeta.getFieldName());
            byte[] fieldBytes = recordData.getFieldBytes(fieldMeta.getFieldName());
            // if field value is not null
            if (fieldVal != null && fieldBytes != null) {
                // write fields value in bytes into buffer
                System.arraycopy(fieldBytes, 0, buffer, bufWtPointer.getValue(), fieldBytes.length);
                nullList.add(0);
            } else {
                nullList.add(1);
            }

            bufWtPointer.incrBy(fieldMeta.getLength());
        });

        // buffer nullBitMap write pointer
        IntegerPointer bufNullBitMapWtPointer = new IntegerPointer(bufWtPointer);

        // skip nullBitMap
        bufWtPointer.incrBy(nullBitMapLen);

        // vary-len field offset-len write pointer
        IntegerPointer varyLenFieldOffsetLenWtPointer = new IntegerPointer(offset);

        // write vary-length fields bytes and offset-len at the same time into buffer
        tableMeta.foreachVaryField(fieldMeta -> {
            Object fieldVal = recordData.getFieldValue(fieldMeta.getFieldName());
            byte[] fieldBytes = recordData.getFieldBytes(fieldMeta.getFieldName());
            // if field value is not null
            if (fieldVal != null && fieldBytes != null) {
                // write field offset-len pointer
                PointerUtil.setOffsetLen(
                        buffer,
                        varyLenFieldOffsetLenWtPointer.getValue(),
                        new OffsetLen(
                                (short)(bufWtPointer.getValue() - offset),
                                (short)fieldBytes.length
                        )
                );

                // write field data
                System.arraycopy(
                        fieldBytes, 0,
                        buffer, bufWtPointer.getValue(),
                        fieldBytes.length
                );

                bufWtPointer.incrBy(fieldBytes.length);
                nullList.add(0);
            } else {
                // write field offset-len pointer
                PointerUtil.setOffsetLen(
                        buffer,
                        varyLenFieldOffsetLenWtPointer.getValue(),
                        new OffsetLen(
                                (short)0,
                                (short)0
                        )
                );

                nullList.add(1);
            }

            varyLenFieldOffsetLenWtPointer.incrBy(OffsetLen.LEN);
        });

        // check nullBitmap
        if (nullList.size() > nullBitMapLen * 8) {
            throw new RuntimeException("No enough Memory allocated for nullBitMap");
        }

        // write nullBitmap to block cache
        for (int i = 0; i < nullList.size(); i++) {
            ByteUtil.writeBitToByteBuffer(buffer, bufNullBitMapWtPointer.getValue(), i, nullList.get(i));
        }
    }

    /**
     * Write on buffer
     * @param buffer
     */
    public void writeOnBuffer(byte[] buffer, RecordInnerPointer pointer) {
        writeOnBuffer(buffer, pointer.getRecordInnerOffset());
    }

    /**
     * get total bytes number
     * @return
     */
    public int getTotalBytesNum(){
        return recordData.getTotalBytesNum()
                + (tableMeta.getFieldsNum()-1) / 8 + 1
                + OffsetLen.LEN * tableMeta.getVaryLenFieldsNum();
    }

    /**
     * fill bytes map
     */
    public void fillBytesMap() {
        recordData.fillBytesMap(tableMeta);
    }

}
