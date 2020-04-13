package com.gyw.storageManager.fileManager.tablespace;

import com.gyw.storageManager.fileManager.tablespace.enums.FieldType;
import com.gyw.storageManager.fileManager.tablespace.enums.FieldVaryLenType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class FieldMeta {

    private String fieldName;

    private FieldType fieldType;

    private FieldVaryLenType fieldVaryLenType;

    private int length;

    private int precision;

}
