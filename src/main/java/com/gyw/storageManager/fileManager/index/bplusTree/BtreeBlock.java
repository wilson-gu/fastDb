package com.gyw.storageManager.fileManager.index.bplusTree;

import com.gyw.storageManager.fileManager.block.Block;
import com.gyw.storageManager.fileManager.block.BlockType;
import com.gyw.storageManager.fileManager.index.bplusTree.node.*;
import com.gyw.storageManager.pointers.BlockPointer;
import com.gyw.util.ByteUtil;

/**
 * B+ Tree block
 * @author guyw
 */
public class BtreeBlock extends Block {

    /**
     * B+ Tree Block size
     */
    public static final int LEN = 59;

    @Override
    protected BlockType getBlockType() {
        return BlockType.INDEX;
    }

    /**
     * Constructor
     * @param blockPointer block pointer
     */
    public BtreeBlock(BlockPointer blockPointer) {
        super(blockPointer);
    }

    /**
     * read buffer and get blockObj operation
     *
     * @return block object
     */
    @Override
    protected BtreeNode readOp(BlockPointer blockPointer) {
        BtreeNode btreeNode;
        switch (getNodeType()) {
            case LEAF:
                btreeNode = new LeafNode(blockPointer);
                break;

            case INTERNAL:
                btreeNode = new InternalNode(blockPointer);
                break;

            case ROOT:
                btreeNode = new RootNode(blockPointer);
                break;

            default:
                throw new RuntimeException("No support!");
        }


        btreeNode.deSerialize(buf);
        return btreeNode;
    }

    /**
     * get node type
     * @return node type of the block
     */
    private NodeType getNodeType() {
        // read node type
        int nodeTypeVal = (int) ByteUtil.readByteFromBuffer(buf, 0);
        return NodeType.fromValue(nodeTypeVal);
    }

    /**
     * get block length
     *
     * @return block length
     */
    @Override
    protected int getBlockLen() {
        return BtreeBlock.LEN;
    }


}
