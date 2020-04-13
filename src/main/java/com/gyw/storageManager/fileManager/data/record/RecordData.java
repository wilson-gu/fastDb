package com.gyw.storageManager.fileManager.data.record;

import com.google.common.base.Strings;
import com.gyw.storageManager.fileManager.tablespace.TableMeta;
import com.gyw.util.FieldUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Record data
 * @author guyw
 */
public class RecordData extends HashMap<String, Object> {

    Map<String, byte[]> fieldBytes = new HashMap<>();

    /**
     * set field value
     *
     * @param fieldName
     * @param fieldVal
     */
    public void setFieldValue(String fieldName, Object fieldVal) {
        if (Strings.isNullOrEmpty(fieldName)) {
            throw new IllegalArgumentException("fieldName is null!");
        }
        this.put(fieldName, fieldVal);
    }

    /**
     * get field value
     * @param fieldName
     * @return field value
     */
    public Object getFieldValue(String fieldName) {
        Objects.requireNonNull(fieldName);
        return this.get(fieldName);
    }

    /**
     * Turn all field data into fieldBytes
     * and put them into fieldBytes map internally
     * @param tableMeta table meta definition
     */
    public void fillBytesMap(TableMeta tableMeta) {
        if (this.size() != tableMeta.getFieldsNum()) {
            throw new RuntimeException("recordData number not concord with tableMeta field number!");
        }

        tableMeta.foreachField(fieldMeta -> {
            Object fieldVal = getFieldValue(fieldMeta.getFieldName());
            if (fieldVal != null) {
                fieldBytes.put(
                        fieldMeta.getFieldName(),
                        FieldUtil.fieldToBytes(fieldMeta, fieldVal)
                );
            } else {
                fieldBytes.put(fieldMeta.getFieldName(), null);
            }
        });
    }

    /**
     * get field fieldBytes
     * @param fieldName field name
     * @return field fieldBytes
     *         null if field fieldBytes not exist
     */
    public byte[] getFieldBytes(String fieldName) {
        Objects.requireNonNull(fieldName);
        return fieldBytes.get(fieldName);
    }

    /**
     * is field byte map size equals to field number in tableMeta
     * @param tableMeta table meta
     * @return true / false
     */
    public boolean isLegalFieldBytes(TableMeta tableMeta) {
        return fieldBytes.size() == tableMeta.getFieldsNum();
    }

    /**
     * get total bytes number
     * @return total bytes number
     */
    public int getTotalBytesNum() {
        int cnt = 0;
        for (byte[] fieldByte : fieldBytes.values()) {
            if (fieldByte == null) {
                cnt += 0;
            } else {
                cnt += fieldByte.length;
            }
        }
        return cnt;
    }
}
