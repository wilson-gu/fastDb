package com.gyw.storageManager.fileManager.index.bplusTree.node;

/**
 * node block type for B+ tree
 * @author guyw
 */
public enum NodeType {

    /**
     * leaf block
     */
    LEAF(1),

    /**
     * internal block
     */
    INTERNAL(2),

    /**
     * root block
     */
    ROOT(3),

    /**
     * temp leaf
     */
    TMP_LEAF(4);

    private int value;

    private NodeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * convert value to NodeType item
     * @param value value of NodeType item
     * @return NodeType item
     */
    public static NodeType fromValue(int value) {
        switch (value) {
            case 1:
                return LEAF;

            case 2:
                return INTERNAL;

            case 3:
                return ROOT;

            default:
                throw new IllegalArgumentException("Value [" + value + "] not defined for enum NodeType.");
        }
    }
}
