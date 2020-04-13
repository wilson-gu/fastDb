package com.gyw.storageManager.bufferManager;

import com.google.common.cache.*;
import com.gyw.storageManager.fileManager.block.BlockAllocator;
import com.gyw.storageManager.fileManager.block.BlockType;
import com.gyw.storageManager.fileManager.index.bplusTree.BtreeBlock;
import com.gyw.storageManager.pointers.BlockPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data block buffer
 */
public class IndexBlockBuffer {

    private static final Logger logger = LoggerFactory.getLogger(IndexBlockBuffer.class);

    public static final int BUFFER_SIZE = 10;

    /**
     * Internal BplusTreeBlock Cache
     */
    private LoadingCache<BlockPointer, BtreeBlock> BplusTreeBlockCache =
            CacheBuilder.newBuilder()
                    .maximumSize(BUFFER_SIZE)
                    .removalListener(new RemovalListener<BlockPointer, BtreeBlock>() {
                        @Override
                        public void onRemoval(RemovalNotification<BlockPointer, BtreeBlock> rn) {
                            BlockPointer blockPosition = rn.getKey();
                            BtreeBlock   block         = rn.getValue();
                            logger.trace("Remove:" + blockPosition);

                            // flush before removing the block
                            block.flush();
                        }
                    })
                    .build(new CacheLoader<BlockPointer, BtreeBlock>() {
                        @Override
                        public BtreeBlock load(BlockPointer blockPointer) throws Exception {

                            if (BlockAllocator.isBlockExistOnDisk(BlockType.DATA, blockPointer)) {
                                logger.trace("load Block: " + blockPointer + " from disk");
                                // if block exist on disk
                                // load the block and return
                                BtreeBlock block = new BtreeBlock(blockPointer);
                                block.load();
                                return block;
                            } else {
                                // FIXME  fix in the future
                                logger.warn("Block: " + blockPointer + " not exist on disk!");
//                                logger.debug("load Block: " + blockPointer + " from disk");
//                                // if block not exist on disk
//                                // create a block and return
//                                BplusTreeBlock block = new BplusTreeBlock();
//                                block.createEmptyBlock();
//                                return block;
                                return null;
                            }

                        }
                    });

    private IndexBlockBuffer(){};

    private static class SingletonClassInstance {
        private static final IndexBlockBuffer INSTANCE = new IndexBlockBuffer();
    }

    /**
     * Get instance
     * @return only BlockBuffer instance
     */
    public static IndexBlockBuffer getInstance() {
        return SingletonClassInstance.INSTANCE;
    }

    /**
     * get a block from cache
     * @param blockPointer
     * @return
     */
    public BtreeBlock get(BlockPointer blockPointer) {
        try {
            return BplusTreeBlockCache.get(blockPointer);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * invalidate all
     */
    public void invalidateAll() {
        BplusTreeBlockCache.invalidateAll();
    }

    /**
     * new a block and put into buffer
     * @return a new block
     */
    public synchronized BtreeBlock newBlock(BlockType blockType) {
        // create a block and return
        // allocate a block on disk
        BlockPointer blockPointer = BlockAllocator.allocateBlockOnDisk(blockType);
        if (blockPointer == null) {
            throw new NullPointerException("Block Allocation Fail!");
        }

        BtreeBlock btreeBlock = new BtreeBlock(blockPointer);

        btreeBlock.createEmptyBlock();
        logger.trace("Block: " + blockPointer.toString() + " is put into IndexBlockBuffer");
        BplusTreeBlockCache.put(blockPointer, btreeBlock);

        return btreeBlock;
    }
}
