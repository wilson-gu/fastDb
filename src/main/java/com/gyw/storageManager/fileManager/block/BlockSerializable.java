package com.gyw.storageManager.fileManager.block;

public interface BlockSerializable {

    /**
     * serialize the object to bytes
     * @param buffer buffer
     */
    void serialize(byte[] buffer);

    /**
     * deSerialize bytes to object
     * @param buffer buffer
     */
    void deSerialize(byte[] buffer);

}
