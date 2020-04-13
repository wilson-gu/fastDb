package com.gyw.storageManager.fileManager.tablespace;


import com.google.common.base.Strings;
import com.gyw.storageManager.fileManager.tablespace.enums.FieldVaryLenType;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Table meta definition
 * @author guyw
 */
@ToString
public class TableMeta {

    private String tableName;

    private Map<String, FieldMeta> fixedLenFields = new LinkedHashMap<>();

    private Map<String, FieldMeta> varyLenFields = new LinkedHashMap<>();

    public TableMeta(String tableName) {
        this.tableName = tableName;
    }


    /**
     * set field meta
     * @param fieldMeta field meta
     */
    public void setFieldMeta(FieldMeta fieldMeta) {
        if (Strings.isNullOrEmpty(fieldMeta.getFieldName())) {
            throw new IllegalArgumentException("filedName is null!");
        }

        if (fieldMeta.getFieldVaryLenType() == null) {
            throw new IllegalArgumentException("fieldMeta is null!");
        }

        if (fieldMeta.getFieldVaryLenType() == FieldVaryLenType.VARY_LEN) {
            varyLenFields.put(fieldMeta.getFieldName(), fieldMeta);
        } else {
            fixedLenFields.put(fieldMeta.getFieldName(), fieldMeta);
        }
    }

    /**
     * get field meta
     * @param fieldName field name
     * @return
     */
    public FieldMeta getFieldMeta(String fieldName) {
        FieldMeta fieldMeta = varyLenFields.get(fieldName);
        if (fieldMeta == null) {
            return fixedLenFields.get(fieldName);
        }

        return fieldMeta;
    }

    /**
     * is field name given exist in TableMeta?
     * @param fieldName field name
     * @return true if field name exist in tableMeta
     *         no if field name NOT exist in tableMeta
     */
    public boolean isFieldNameExist(String fieldName) {
        if (varyLenFields.containsKey(fieldName)
                || fixedLenFields.containsKey(fieldName)) {
            return true;
        }

        return false;
    }

    /**
     * get fields number
     * @return field number
     */
    public int getFieldsNum() {
        return fixedLenFields.size() + varyLenFields.size();
    }

    /**
     * get vary-len fields number
     * @return vary-len fields number
     */
    public int getVaryLenFieldsNum() {
        return varyLenFields.size();
    }

    /**
     * get fixed-len fields number
     * @return fixed-len fields number
     */
    public int getFixedLenFieldsNum() {
        return fixedLenFields.size();
    }

    /**
     * get fixed-len fields size sum
     * @return sum of all fixed-len fields
     */
    public int getFixedLenFieldsSize() {
        int size = 0;
        for (FieldMeta fieldMeta : fixedLenFields.values()) {
            size += fieldMeta.getLength();
        }
        return size;
    }

    /**
     * iterate over each fixed field
     * @param consumer fixed-len field meta consumer
     */
    public void foreachFixedField(Consumer<FieldMeta> consumer) {
        Objects.requireNonNull(consumer);

        for (FieldMeta fieldMeta : fixedLenFields.values()) {
            consumer.accept(fieldMeta);
        }
    }

    /**
     * iterate over each fixed field
     * @param consumer vary-len field meta consumer
     */
    public void foreachVaryField(Consumer<FieldMeta> consumer) {
        Objects.requireNonNull(consumer);

        for (FieldMeta fieldMeta : varyLenFields.values()) {
            consumer.accept(fieldMeta);
        }
    }

    /**
     * iterate over each field
     * first fixed length fields then vary length fields
     * @param consumer field meta consumer
     */
    public void foreachField(Consumer<FieldMeta> consumer) {
        Objects.requireNonNull(consumer);

        for (FieldMeta fieldMeta : fixedLenFields.values()) {
            consumer.accept(fieldMeta);
        }

        for (FieldMeta fieldMeta : varyLenFields.values()) {
            consumer.accept(fieldMeta);
        }
    }

    /**
     * iterate over each field name
     * first fixed length fields then vary length fields
     * @param consumer field name consumer
     */
    public void foreachFieldName(Consumer<String> consumer) {
        Objects.requireNonNull(consumer);

        for (String fieldName : fixedLenFields.keySet()) {
            consumer.accept(fieldName);
        }

        for (String fieldName : varyLenFields.keySet()) {
            consumer.accept(fieldName);
        }
    }
}
