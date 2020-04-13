package com.gyw.storageManager.fileManager.index.bplusTree.node;

import com.google.common.base.Strings;
import com.gyw.storageManager.pointers.BlockPointer;
import com.gyw.storageManager.pointers.PointerUtil;
import com.gyw.util.ByteUtil;

/**
 * @Description non-leaf node
 * @Author guyw
 * @Date 2019/11/7 22:14
 * @Version 1.0
 */
public abstract class NonLeafNode extends BtreeNode {

    /**
     * Constructor
     */
    public NonLeafNode(BlockPointer blockPointer) {
        super(blockPointer);
        pointers = new BlockPointer[defineNodesNumN()];
    }

    /**
     * serialize the object to bytes
     *
     * @param buffer buffer
     */
    @Override
    public void serialize(byte[] buffer) {
        int offset = 0;

        // write node type
        ByteUtil.writeByteToBuffer(buffer, offset, nodeType.getValue());
        offset += NODE_TYPE_FD_LEN;

        // write n-1 pointers and keys
        for (int i = 1; i < B_TREE_LEAF_N; i++) {
            // write record pointer
            PointerUtil.setBlockPointer(buffer, offset, (BlockPointer)pointers[i]);
            offset += BlockPointer.LEN;

            if (keys[i] == null) {
                break;
            }

            // write search key
            ByteUtil.writeStringToByteBuffer(buffer, offset, keys[i]);
            offset += SEARCH_KEY_LEN;
        }
    }

    /**
     * deSerialize bytes to object
     *
     * @param buffer buffer
     */
    @Override
    public void deSerialize(byte[] buffer) {
        int offset = 0;

        // read node type
        int nodeTypeVal = (int) ByteUtil.readByteFromBuffer(buffer, offset);
        this.nodeType = NodeType.fromValue(nodeTypeVal);

        offset += NODE_TYPE_FD_LEN;

        usedSkPtPartsNum = 0;
        // read n-1 pointers and keys
        for (int i = 1; i < B_TREE_LEAF_N; i++) {
            // read record pointer
            BlockPointer blockPointer = PointerUtil.getBlockPointer(buffer, offset);
            offset += BlockPointer.LEN;

            this.pointers[i] = blockPointer;

            // read search key
            String sk = ByteUtil.readStringFromByteBuffer(buffer, offset, SEARCH_KEY_LEN);
            offset += SEARCH_KEY_LEN;

            if (Strings.isNullOrEmpty(sk)) {
                break;
            }

            this.keys[i] = sk;
            usedSkPtPartsNum++;
        }
    }

    /**
     * find smallest index such that search key is less or equal to key
     * @param sk search key
     * @return
     */
    public int findSmallerIndexStSkLeqKey(String sk) {
        int index = -1;
        for (int i = 1; i <= usedSkPtPartsNum; i++) {
            if (sk.compareTo(keys[i]) <= 0) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * get last non null pointer
     * @return last non null pointer
     */
    public BlockPointer getLastNonNullPointer() {
        return (BlockPointer) pointers[usedSkPtPartsNum + 1];
    }
}
