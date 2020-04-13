package com.gyw.storageManager.fileManager.block;

/**
 * Block status
 * @author guyw
 */
public enum BlockStatus {

    /**
     * Block cache has updated,
     * this data on cache is ahead of disk
     */
    UPDATED("UPDATED"),

    /**
     * Nothing is in cache
     */
    NULL("NULL"),

    /**
     * Block cache is synced with disk
     */
    SYNCED("SYNCED");

    private String enName;

    BlockStatus(String enName) {
        this.enName = enName;
    }

    @Override
    public String toString() {
        return enName;
    }
}
