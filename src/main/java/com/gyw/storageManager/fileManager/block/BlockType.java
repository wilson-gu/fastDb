package com.gyw.storageManager.fileManager.block;

/**
 * Block type
 */
public enum BlockType {

    DATA("data"),
    INDEX("index");

    private String filename;

    private BlockType(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
