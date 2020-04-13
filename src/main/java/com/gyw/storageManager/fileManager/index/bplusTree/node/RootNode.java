package com.gyw.storageManager.fileManager.index.bplusTree.node;

import com.gyw.storageManager.pointers.BlockPointer;

/**
 * @Description Root node
 * @Author guyw
 * @Date 2019/11/8 0:47
 * @Version 1.0
 */
public class RootNode extends NonLeafNode {

    /**
     * Constructor
     *
     * @param blockPointer
     */
    public RootNode(BlockPointer blockPointer) {
        super(blockPointer);
    }

    /**
     * define nodes number n
     *
     * @return nodes number n
     */
    @Override
    public int defineNodesNumN() {
        return BtreeNode.B_TREE_NON_LEAF_N;
    }

    /**
     * define node type
     *
     * @return node type
     */
    @Override
    protected NodeType defineNodeType() {
        return NodeType.ROOT;
    }
}
