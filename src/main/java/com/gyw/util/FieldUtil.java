package com.gyw.util;

import com.gyw.storageManager.fileManager.tablespace.enums.FieldType;
import com.gyw.storageManager.fileManager.tablespace.enums.FieldVaryLenType;
import com.gyw.storageManager.fileManager.tablespace.FieldMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * field utilities
 * @author guyw
 */
public class FieldUtil {

    /**
     * convert a field to bytes
     * @param fieldMeta field meta definition
     * @param fieldVal field value
     * @return field bytes
     */
    public static byte[] fieldToBytes(FieldMeta fieldMeta, Object fieldVal) {
        Objects.requireNonNull(fieldMeta, "fieldMeta is null");

        if (fieldVal == null) {
            return null;
        }

        byte[] bytes;
        if (fieldMeta.getFieldType() == FieldType.VARCHAR
                && fieldVal instanceof String) {
             bytes = ((String) fieldVal).getBytes();
        } else if (fieldMeta.getFieldType() == FieldType.NUMERIC
                && fieldVal instanceof BigDecimal) {
            bytes = ((BigDecimal) fieldVal)
                    .setScale(fieldMeta.getPrecision(), RoundingMode.HALF_UP)
                    .toString()
                    .getBytes();
        } else {
            throw new RuntimeException("Not support yet!");
        }

        // check bytes length
        if (fieldMeta.getFieldVaryLenType() == FieldVaryLenType.VARY_LEN) {
            if (bytes.length > fieldMeta.getLength()) {
                throw new RuntimeException("varchar of length [" + bytes.length + "] " +
                        "which exceeds defined length = [" + fieldMeta.getLength() + "] " +
                        "FieldName = [" + fieldMeta.getFieldName() + "]");
            }
        } else {
            if (bytes.length != fieldMeta.getLength()) {
                throw new RuntimeException("varchar of length [" + bytes.length + "] " +
                        " must equal to defined length = [" + fieldMeta.getLength() + "] " +
                        "FieldName = [" + fieldMeta.getFieldName() + "]");
            }
        }

        return bytes;
    }


    /**
     * read field bytes and convert to its Java representation
     * @param buffer buffer
     * @param offset offset of field on buffer
     * @param len length of field on buffer
     * @param fieldMeta field meta
     * @return field data in java representation
     */
    public static Object bytesToField(byte[] buffer, int offset, int len, FieldMeta fieldMeta) {
        byte[] bytes;
        switch (fieldMeta.getFieldType()) {
            case VARCHAR:
                bytes = new byte[len];
                System.arraycopy(
                        buffer, offset,
                        bytes, 0,
                        len);
                return new String(bytes);

            case NUMERIC:
                bytes = new byte[len];
                System.arraycopy(
                        buffer, offset,
                        bytes, 0,
                        len);
                return new BigDecimal(new String(bytes));

            default:
                throw new RuntimeException("Unsupported fieldType [" + fieldMeta.getFieldType() + "]" +
                        ", fieldName = [" + fieldMeta.getFieldName() + "]" );
        }
    }
}
