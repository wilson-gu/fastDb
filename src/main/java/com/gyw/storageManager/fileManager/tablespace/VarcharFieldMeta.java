package com.gyw.storageManager.fileManager.tablespace;

import com.gyw.storageManager.fileManager.tablespace.enums.FieldType;
import com.gyw.storageManager.fileManager.tablespace.enums.FieldVaryLenType;

public class VarcharFieldMeta extends FieldMeta {

    public VarcharFieldMeta(String fieldName, int fieldLen) {
        super();
        setFieldName(fieldName);
        setFieldType(FieldType.VARCHAR);
        setFieldVaryLenType(FieldVaryLenType.VARY_LEN);
        setLength(fieldLen);
    }
}
