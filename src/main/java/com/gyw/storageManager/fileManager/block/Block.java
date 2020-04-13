package com.gyw.storageManager.fileManager.block;

import com.gyw.storageManager.pointers.BlockPointer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Block
 * @author guyw
 */
public abstract class Block {

    private static final Logger logger = LoggerFactory.getLogger(Block.class);

    /**
     * Read-Write lock
     */
    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * block buffer
     */
    protected byte[] buf;

    /**
     * block pointer
     */
    protected BlockPointer blockPointer;

    /**
     * block status
     */
    protected BlockStatus blockStatus;


    /**
     * get block pointer
     * @return block pointer
     */
    public BlockPointer getBlockPointer() {
        return blockPointer;
    }

    /**
     * get buffer reference
     * @return buffer reference
     */
    public byte[] getBuf() {
        return buf;
    }

    /**
     * get block type
     * @return block type
     */
    protected abstract BlockType getBlockType();

    public Block(BlockPointer blockPointer) {
        this.blockPointer = blockPointer;
        this.blockStatus = BlockStatus.NULL;
    }

    /**
     * get block status
     */
    public BlockStatus getBlockStatus() {
        return blockStatus;
    }

    /**
     * Load a block from disk to cache
     */
    public void load() {
        try {
            // get write lock
            lock.writeLock().lock();

            // check status
            if (blockStatus != BlockStatus.NULL) {
                return;
            }

            // load block from disk
            loadOp();

            // change block status
            blockStatus = BlockStatus.SYNCED;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // drop write lock
            lock.writeLock().unlock();
        }
    }

    /**
     * load operation
     * @throws IOException
     */
    protected void loadOp() throws IOException {
        // raf will close automatically
        try (RandomAccessFile raf
                     = new RandomAccessFile(new File(getBlockType().getFilename()), "r")){

            // seek to offset
            raf.seek(blockPointer.getBlockOffset());

            // new a buffer
            buf = new byte[getBlockLen()];

            // read a block
            int len = raf.read(buf);
            logger.trace("Read Block " + "[" + getBlockType().getFilename() + ":" + blockPointer.getBlockOffset() + "]" + ", len = " + buf.length);
            if (len != getBlockLen()) {
                throw new IOException("Read Block " + "[" + getBlockType().getFilename() + ":" + blockPointer.getBlockOffset() + "] Fail!"
                        + ", len = " + buf.length);
            }

            // change block status
            blockStatus = BlockStatus.SYNCED;
        }
    }


    /**
     * Write block cache back to disk
     */
    public void flush() {
        try {
            // get read lock
            lock.readLock().lock();

            // check status
            if (blockStatus != BlockStatus.UPDATED) {
                return;
            }

            // write to file
            flushOp();

            // change block status
            blockStatus = BlockStatus.SYNCED;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // drop read lock
            lock.readLock().unlock();
        }
    }

    /**
     * Write Cache to file
     * @throws Exception
     */
    protected void flushOp() throws Exception {
        // raf will close automatically
        try (RandomAccessFile raf
                     = new RandomAccessFile(new File(getBlockType().getFilename()), "rw")) {

            // seek to offset
            raf.seek(blockPointer.getBlockOffset());

            // write a block
            raf.write(buf);

            logger.trace("Write Block " + "[" + getBlockType().getFilename() + ":" + blockPointer.getBlockOffset() + "]"
                    + ", len = " + buf.length);
        }
    }

    /**
     * write block object to buffer
     * @param blockObj block object
     */
    public void write(BlockSerializable blockObj) {
        try {
            // get write lock
            lock.writeLock().lock();

            // load block from disk
            writeOp(blockObj);

            // change block status
            blockStatus = BlockStatus.UPDATED;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // drop write lock
            lock.writeLock().unlock();
        }
    }


    /**
     * write block object to buffer operation
     * @param blockObj block object
     */
    protected void writeOp(@NotNull BlockSerializable blockObj) {
         blockObj.serialize(buf);
    }

    /**
     * read buffer and get blockObj
     * @return block object
     */
    public BlockSerializable read() {
        try {
            // get write lock
            lock.writeLock().lock();

            return readOp(blockPointer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // drop write lock
            lock.writeLock().unlock();
        }
    }

    /**
     * read buffer and get blockObj operation
     * @return block object
     */
    abstract protected BlockSerializable readOp(BlockPointer blockPointer);

    /**
     * create an empty block on cache
     */
    public void createEmptyBlock() {
        try {
            // get write lock
            lock.writeLock().lock();

            if (blockStatus != BlockStatus.NULL) {
                throw new RuntimeException("Block status must be BlockStatus.NULL" +
                        " when creating empty block!");
            }

            createEmptyBlockOp();

            // change block status
            blockStatus = BlockStatus.UPDATED;
        } finally {
            // drop write lock
            lock.writeLock().unlock();
        }
    }

    /**
     * create empty block operation
     */
    protected void createEmptyBlockOp() {
        // create a new block buffer
        this.buf = new byte[getBlockLen()];
    }

    /**
     * get block length
     * @return block length
     */
    abstract protected int getBlockLen();
}
