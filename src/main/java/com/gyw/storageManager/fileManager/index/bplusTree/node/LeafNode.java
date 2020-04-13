package com.gyw.storageManager.fileManager.index.bplusTree.node;

import com.google.common.base.Strings;
import com.gyw.storageManager.fileManager.index.bplusTree.BtreeBlock;
import com.gyw.storageManager.pointers.BlockPointer;
import com.gyw.storageManager.pointers.PointerUtil;
import com.gyw.storageManager.pointers.RecordPointer;
import com.gyw.util.ByteUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @Description Leaf node
 * @Author guyw
 * @Date 2019/11/7 22:08
 * @Version 1.0
 */
public class LeafNode extends BtreeNode {

    /**
     * TailPointer
     */
    protected BlockPointer tailPointer;

    /**
     * Constructor
     */
    public LeafNode(BlockPointer blockPointer) {
        super(blockPointer);
        pointers = new RecordPointer[defineNodesNumN()];
    }

    /**
     * add searchKey-Pointer part
     * @param searchKey search key
     * @param pointer pointer
     */
    public void addPart(@NotNull String searchKey, @NotNull RecordPointer pointer) {
        pointers[++usedSkPtPartsNum] = pointer;
        keys[usedSkPtPartsNum] = searchKey;
    }

    /**
     * set tail pointer
     * @param tailPointer tail pointer
     */
    public void setTailPointer(BlockPointer tailPointer) {
        this.tailPointer = tailPointer;
    }

    /**
     * get tail pointer
     * @return tail pointer
     */
    public BlockPointer getTailPointer() {
        return tailPointer;
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
        for (int i = 1; i <= usedSkPtPartsNum; i++) {
            // write record pointer
            PointerUtil.setRecordPointer(buffer, offset, (RecordPointer)pointers[i]);
            offset += RecordPointer.LEN;

            // write search key
            ByteUtil.writeStringToByteBuffer(buffer, offset, keys[i]);
            offset += SEARCH_KEY_LEN;
        }

        // write tail pointer
        if (tailPointer != null) {
            PointerUtil.setBlockPointer(buffer, BtreeBlock.LEN - BlockPointer.LEN, tailPointer);
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
        for (int i = 1; i < defineNodesNumN(); i++) {
            // read record pointer
            RecordPointer recordPointer = PointerUtil.getRecordPointer(buffer, offset);
            offset += RecordPointer.LEN;

            // read search key
            String sk = ByteUtil.readStringFromByteBuffer(buffer, offset, SEARCH_KEY_LEN);
            offset += SEARCH_KEY_LEN;

            if (Strings.isNullOrEmpty(sk)) {
                break;
            }

            this.pointers[i] = recordPointer;
            this.keys[i] = sk;
            usedSkPtPartsNum++;
        }

        // read tail pointer
        tailPointer = PointerUtil.getBlockPointer(buffer, offset);
    }

    /**
     * find least index such that key equals to search key
     * @param sk search key
     * @return least index such that key equals to search key <br>
     *         0 if not found
     */
    public int findLeastIndexStKeyEqSk(String sk) {
        int index = 0;
        for (int i = 1; i <= usedSkPtPartsNum; i++) {
            if (sk.equals(keys[i])) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * find highest index such that key is less than search key
     * @param sk search key
     * @return highest index such that key is less than search key <br>
     *         0 if not found
     */
    public int findHighestIndexStKeyLtSk(String sk) {
        int highestIndex = 1;
        for (int i = 1; i <= usedSkPtPartsNum; i++) {
            // key > sk
            if (keys[i].compareTo(sk) >= 0) {
                highestIndex = i;
                break;
            }
        }
        return highestIndex-1;
    }

    /**
     * find least index such that key is greater than or equal to search key
     * @param sk search key
     * @return least index such that key is bigger than or equal to search key <br>
     *         0 if not found
     */
    public int findLeastIndexStKeyGeSk(String sk) {
        int highestIndex = 0;
        for (int i = 1; i <= usedSkPtPartsNum; i++) {
            // key > sk
            if (keys[i].compareTo(sk) >= 0) {
                highestIndex = i;
                break;
            }
        }
        return highestIndex;
    }

    /**
     * check insert index of pointer and key
     * @param k : index
     */
    private void checkInsertIndex(int k) {
        if (k < 1) {
            throw new IndexOutOfBoundsException("k < 1");
        }

        if (k > usedSkPtPartsNum) {
            throw new IndexOutOfBoundsException("k > usedSkPtPartsNum");
        }
    }

    /**
     * check used sk-pt parts number for insert
     */
    private void checkUsedSkPtPartsNumForInsert() {
        if (usedSkPtPartsNum > defineNodesNumN() - 2) {
            throw new IndexOutOfBoundsException("usedSkPtPartsNum > defineNodesNumN() - 2");
        }
    }

    /**
     * insert search key and record pointer just before index k
     * @param sk search key
     * @param pr record pointer
     * @param k index position
     */
    public void insert(String sk, RecordPointer pr, int k) {
        // the order of two checks are important
        checkUsedSkPtPartsNumForInsert();
        checkInsertIndex(k);

        // shift sk-pt to right by one
        for (int i = usedSkPtPartsNum; i >= k; i--) {
            keys[i+1] = keys[i];
            pointers[i+1] = pointers[i];
        }

        // now there is room for insert sk-pt
        keys[k] = sk;
        pointers[k] = pr;
    }

    /**
     * define nodes number n
     *
     * @return nodes number n
     */
    @Override
    public  int defineNodesNumN() {
        return BtreeNode.B_TREE_LEAF_N;
    }

    /**
     * define node type
     *
     * @return node type
     */
    @Override
    protected NodeType defineNodeType() {
        return NodeType.LEAF;
    }

    /**
     * set usedSkPtPartsNum to 0
     * logically means no sk-pt in the node after rewind
     */
    public void rewind() {
        usedSkPtPartsNum = 0;
    }

    /**
     * check begin and end index
     * @param beg begin index
     * @param end end index(off by one)
     */
    private void checkBegAndEndIndex(int beg, int end) {
        if (beg < 1) {
            throw new IndexOutOfBoundsException("beg < 1");
        }

        if (end > defineNodesNumN()) {
            throw new IndexOutOfBoundsException("end > defineNodesNumN()");
        }

        if (beg > end) {
            throw new IndexOutOfBoundsException("beg > end");
        }
    }

    /**
     * copy from another leafNode
     * @param leafNode source leafNode
     * @param beg begin position of sk-pt
     * @param end end position of sk-pt(off by one)
     */
    public void copyFrom(LeafNode leafNode, int beg, int end) {
        // rewind before coping
        rewind();

        int copyLen = end - beg;
        if (copyLen > 0) {
            // copy keys
            System.arraycopy(leafNode.getKeys(), beg, keys, 1, copyLen);

            // copy pointers
            System.arraycopy(leafNode.getPointers(), beg, pointers, 1, copyLen);

            // set usedSkPtPartsNum
            usedSkPtPartsNum = copyLen;
        }
    }

    /**
     * copy from another leafNode
     * @param leafNode source leafNode
     */
    public void copyFrom(LeafNode leafNode) {
        copyFrom(leafNode, 1, leafNode.getUsedSkPtPartsNum());
    }
}
