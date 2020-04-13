package com.gyw.storageManager.fileManager.index.bplusTree.node;

import com.gyw.storageManager.bufferManager.IndexBlockBuffer;
import com.gyw.storageManager.fileManager.block.Block;
import com.gyw.storageManager.fileManager.block.BlockSerializable;
import com.gyw.storageManager.fileManager.index.bplusTree.BtreeBlock;
import com.gyw.storageManager.pointers.BaseFilePointer;
import com.gyw.storageManager.pointers.BlockPointer;
import com.gyw.storageManager.pointers.RecordPointer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description B+ tree node
 * @Author guyw
 * @Date 2019/11/6 21:58
 * @Version 1.0
 */
public abstract class BtreeNode implements BlockSerializable {

    private static final Logger logger = LoggerFactory.getLogger(BtreeNode.class);


    /**
     * search key length in bytes
     */
    protected static final int SEARCH_KEY_LEN = 12;

    /**
     * node type field length in bytes
     */
    protected static final int NODE_TYPE_FD_LEN = 1;

    /**
     * parameter N of B-plus tree leaf node
     */
    public static final int B_TREE_LEAF_N
            = 1 + (BtreeBlock.LEN - BlockPointer.LEN - NODE_TYPE_FD_LEN)
            / (RecordPointer.LEN + SEARCH_KEY_LEN);

    /**
     * parameter N of B-plus tree internal node
     */
    protected static final int B_TREE_NON_LEAF_N =
            1 + (BtreeBlock.LEN - BlockPointer.LEN - NODE_TYPE_FD_LEN)
                    / (BlockPointer.LEN + SEARCH_KEY_LEN);

    /**
     * Node type
     */
    protected NodeType nodeType;

    /**
     * used search key-pointer parts number
     */
    protected int usedSkPtPartsNum = 0;

    /**
     * pointers
     */
    protected BaseFilePointer[] pointers;

    /**
     * keys
     */
    protected String[] keys;


    /**
     * block pointer of this node
     */
    protected BlockPointer blockPointer;


    /**
     * Constructor
     */
    public BtreeNode(BlockPointer blockPointer) {
        this.nodeType = defineNodeType();
        this.blockPointer = blockPointer;
        keys = new String[defineNodesNumN()];
    }

    /**
     * define nodes number n
     * @return nodes number n
     */
    abstract public int defineNodesNumN();

    /**
     * define node type
     * @return node type
     */
    abstract protected NodeType defineNodeType();

    public NodeType getNodeType() {
        return nodeType;
    }

    public int getUsedSkPtPartsNum() {
        return usedSkPtPartsNum;
    }

    /**
     * check pointer type
     * @param pointer pointer
     */
    private void checkPointerType(BaseFilePointer pointer) {
        if (nodeType == NodeType.LEAF) {
            if (! (pointer instanceof RecordPointer)) {
                throw new RuntimeException("pointer must be RecordPointer for NodeType.LEAF");
            }
        } else {
            if (! (pointer instanceof BlockPointer)) {
                throw new RuntimeException("pointer must be BlockPointer for NodeType.NON_LEAF");
            }
        }
    }

    /**
     * check index of pointer and key
     * @param k : index
     */
    private void checkIndex(int k) {
        if (k < 1) {
            throw new IndexOutOfBoundsException("k < 1");
        }

        if (nodeType == NodeType.LEAF) {
            if (k > B_TREE_LEAF_N - 1) {
                throw new IndexOutOfBoundsException("k > B_TREE_LEAF_N - 1");
            }
        } else {
            if (k > B_TREE_NON_LEAF_N - 1) {
                throw new IndexOutOfBoundsException("k > B_TREE_NON_LEAF_N - 1");
            }
        }
    }

    /**
     * check search key
     * @param key search key
     */
    private void checkSearchKey(String key) {
        if (key.getBytes().length > SEARCH_KEY_LEN) {
            throw new RuntimeException("key.getBytes().length > SEARCH_KEY_LEN");
        }
    }

    /**
     * set pointer
     * @param pointer pointer
     * @param k index
     */
    public void setPointer(@NotNull BaseFilePointer pointer, int k) {
        checkPointerType(pointer);
        checkIndex(k);

        pointers[k] = pointer;
    }

    /**
     * set search key
     * @param searchKey search key
     * @param k index
     */
    public void setKey(@NotNull String searchKey, int k) {
        checkIndex(k);
        checkSearchKey(searchKey);

        keys[k] = searchKey;
    }

    /**
     * get pointer
     * @param k index
     * @return pointer
     */
    public BaseFilePointer getPointer(int k) {
        checkIndex(k);
        return pointers[k];
    }

    /**
     * get search key
     * @param k index
     * @return
     */
    public String getKey(int k) {
        checkIndex(k);
        return keys[k];
    }

    public String[] getKeys() {
        return keys;
    }

    public BaseFilePointer[] getPointers() {
        return pointers;
    }

    public BlockPointer getBlockPointer() {
        return blockPointer;
    }

    /**
     * write node into buffer
     */
    public void writeToBuffer() {
        Block block = IndexBlockBuffer.getInstance().get(this.blockPointer);
        block.write(this);
    }
}
