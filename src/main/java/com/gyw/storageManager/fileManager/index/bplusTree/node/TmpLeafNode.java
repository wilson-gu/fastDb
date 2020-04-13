package com.gyw.storageManager.fileManager.index.bplusTree.node;

/**
 * @Description Temporary leaf node
 * @Author guyw
 * @Date 2019/11/16 21:23
 * @Version 1.0
 */
public class TmpLeafNode extends LeafNode {

    public TmpLeafNode() {
        // do not need to record the block positions
        super(null);
    }

    /**
     * define nodes number n
     *
     * @return nodes number n
     */
    @Override
    public int defineNodesNumN() {
        return BtreeNode.B_TREE_LEAF_N + 1;
    }
}
