package com.gyw.storageManager.fileManager.tablespace;

import com.gyw.storageManager.fileManager.tablespace.enums.FieldType;
import com.gyw.storageManager.fileManager.tablespace.enums.FieldVaryLenType;

public class NumericFieldMeta extends FieldMeta {

    public NumericFieldMeta(String fieldName, int fieldLen, int precision) {
        super();
        setFieldName(fieldName);
        setFieldType(FieldType.NUMERIC);
        setFieldVaryLenType(FieldVaryLenType.VARY_LEN);
        setLength(fieldLen);
        setPrecision(precision);
    }
}
