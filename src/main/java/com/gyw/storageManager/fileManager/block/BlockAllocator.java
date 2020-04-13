package com.gyw.storageManager.fileManager.block;

import com.gyw.storageManager.fileManager.data.DataBlock;
import com.gyw.storageManager.fileManager.index.bplusTree.BtreeBlock;
import com.gyw.storageManager.pointers.BlockPointer;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * This class is still a toy
 * need to refactor
 */
public class BlockAllocator {

    public static int dataFreeSpacePointer = 0;

    public static int dataTotalSpacePointer = 0;

    public static int indexFreeSpacePointer = 0;

    public static int indexTotalSpacePointer = 0;


    public static void init() {
        // TODO replace with real situation in the future
        // 500MB
        int fileSize = 10 * 1024;

        byte[] dataBuf = new byte[fileSize];
        byte[] indexBuf = new byte[fileSize];
        initDiskFile(BlockType.DATA, dataBuf);
        initDiskFile(BlockType.INDEX, indexBuf);

        dataTotalSpacePointer = fileSize;
        indexTotalSpacePointer = fileSize;
    }

    private static void initDiskFile(BlockType blockType, byte[] buf) {
        File file = new File(blockType.getFilename());

        if (blockType.getFilename().equals(BlockType.DATA.getFilename())) {
            if (!file.exists()) {
                try (RandomAccessFile raf
                             = new RandomAccessFile(blockType.getFilename(), "rw")){
                    raf.write(buf);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else {
                dataFreeSpacePointer = 3 * DataBlock.LEN;
            }
        } else if (blockType.getFilename().equals(BlockType.INDEX.getFilename())) {
            if (!file.exists()) {
                try (RandomAccessFile raf
                             = new RandomAccessFile(blockType.getFilename(), "rw")){
                    raf.write(buf);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            } else {
                indexFreeSpacePointer = 4 * BtreeBlock.LEN;
            }
        }
    }

    /**
     * Allocate a block on disk file
     * @param blockType
     * @return
     */
    public synchronized static BlockPointer allocateBlockOnDisk(BlockType blockType) {
        BlockPointer blockPointer;
        if (blockType == BlockType.DATA) {
            blockPointer = new BlockPointer(dataFreeSpacePointer);
            dataFreeSpacePointer += DataBlock.LEN;
        } else {
            blockPointer = new BlockPointer(indexFreeSpacePointer);
            indexFreeSpacePointer += BtreeBlock.LEN;
        }

        return blockPointer;
    }

    /**
     * is block exist on disk?
     * @param blockType
     * @param blockPointer
     * @return
     */
    public synchronized static boolean isBlockExistOnDisk(
            BlockType blockType,
            BlockPointer blockPointer) {
        if ( (blockType == BlockType.DATA
                && blockPointer.getBlockOffset() < dataFreeSpacePointer)
                || (blockType == BlockType.INDEX
                && blockPointer.getBlockOffset() < indexFreeSpacePointer)) {
            return true;
        } else {
            return false;
        }
    }
}
