package com.gyw.storageManager.bufferManager;

import com.google.common.cache.*;
import com.gyw.storageManager.fileManager.block.BlockAllocator;
import com.gyw.storageManager.fileManager.block.BlockType;
import com.gyw.storageManager.fileManager.data.DataBlock;
import com.gyw.storageManager.pointers.BlockPointer;
import com.gyw.util.dataStructure.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data block buffer
 */
public class DataBlockBuffer {

    private static final Logger logger = LoggerFactory.getLogger(DataBlockBuffer.class);

    public static final int BUFFER_SIZE = 10;

    /**
     * Internal DataBlock Cache
     */
    private LoadingCache<BlockPointer, DataBlock> dataBlockCache =
            CacheBuilder.newBuilder()
                    .maximumSize(BUFFER_SIZE)
                    .removalListener(new RemovalListener<BlockPointer, DataBlock>() {
                        @Override
                        public void onRemoval(RemovalNotification<BlockPointer, DataBlock> rn) {
                            BlockPointer blockPosition = rn.getKey();
                            DataBlock block = rn.getValue();
                            logger.trace("Remove:" + blockPosition);

                            block.flush();
                        }
                    })
                    .build(new CacheLoader<BlockPointer, DataBlock>() {
                        @Override
                        public DataBlock load(BlockPointer blockPointer) throws Exception {

                            if (BlockAllocator.isBlockExistOnDisk(BlockType.DATA, blockPointer)) {
                                logger.trace("load Block: " + blockPointer + " from disk");
                                // if block exist on disk
                                // load the block and return
                                DataBlock block = new DataBlock(blockPointer);
                                block.load();
                                return block;
                            } else {
                                // FIXME  fix in the future
                                logger.warn("Block: " + blockPointer + " not exist on disk!");
//                                logger.debug("load Block: " + blockPointer + " from disk");
//                                // if block not exist on disk
//                                // create a block and return
//                                DataBlock block = new DataBlock();
//                                block.createEmptyBlock();
//                                return block;
                                return null;
                            }

                        }
                    });

    private DataBlockBuffer(){};

    private static class SingletonClassInstance {
        private static final DataBlockBuffer INSTANCE = new DataBlockBuffer();
    }

    /**
     * Get instance
     * @return only BlockBuffer instance
     */
    public static DataBlockBuffer getInstance() {
        return SingletonClassInstance.INSTANCE;
    }

    /**
     * get a block from cache
     * @param blockPointer
     * @return
     */
    public DataBlock get(BlockPointer blockPointer) {
        try {
            return dataBlockCache.get(blockPointer);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * invalidate all
     */
    public void invalidateAll() {
        dataBlockCache.invalidateAll();
    }

    /**
     * new a block and put into buffer
     * @return pair.first  : block pointer
     *         pair.second : data block
     */
    public synchronized Pair<BlockPointer, DataBlock> newBlock() {
        // create a block and return
        // allocate a block on disk
        BlockPointer blockPointer = BlockAllocator.allocateBlockOnDisk(BlockType.DATA);
        if (blockPointer == null) {
            throw new NullPointerException("Block Allocation Fail!");
        }

        DataBlock block = new DataBlock(blockPointer);
        block.createEmptyBlock();
        dataBlockCache.put(blockPointer, block);

        return new Pair<>(blockPointer, block);
    }
}
