package com.gyw.storageManager.fileManager.tablespace.enums;

public enum FieldType {

    /**
     * VARCHAR
     */
    VARCHAR("VARCHAR"),

    /**
     * NUMERIC
     */
    NUMERIC("NUMERIC");

    private String enName;

    private FieldType(String enName) {
        this.enName = enName;
    }

    @Override
    public String toString() {
        return enName;
    }
}
