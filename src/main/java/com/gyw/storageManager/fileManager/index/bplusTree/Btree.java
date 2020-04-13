package com.gyw.storageManager.fileManager.index.bplusTree;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.gyw.storageManager.bufferManager.DataBlockBuffer;
import com.gyw.storageManager.bufferManager.IndexBlockBuffer;
import com.gyw.storageManager.fileManager.block.BlockType;
import com.gyw.storageManager.fileManager.data.DataBlock;
import com.gyw.storageManager.fileManager.data.record.RecordData;
import com.gyw.storageManager.fileManager.index.bplusTree.node.*;
import com.gyw.storageManager.fileManager.tablespace.TableMeta;
import com.gyw.storageManager.pointers.BlockPointer;
import com.gyw.storageManager.pointers.RecordPointer;
import com.gyw.util.cast.DynamicCast;
import com.gyw.util.dataStructure.Pair;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description B+ Tree
 * @Author guyw
 * @Date 2019/11/8 10:37
 * @Version 1.0
 */
@Log
public class Btree {

    private static final Logger logger = LoggerFactory.getLogger(Btree.class);

    /**
     * get B+ tree node
     * @param blockPointer block pointer of the node
     * @return B+ tree node
     */
    private static BtreeNode getBtreeNode(BlockPointer blockPointer) {
        BtreeBlock block = IndexBlockBuffer.getInstance().get(blockPointer);
        return (BtreeNode) block.read();
    }

    /**
     * do a search in B+ tree and drill down to a possible leaf
     * @param pRoot root pointer of B+ search tree
     * @param sk search key
     * @param recordPath record path flag <br>
     *                   true if you want to record path
     * @return Pair.first  :  possible leaf node <br>
     *         Pair.second :  Path from root node to leaf node found.
     */
    private static Pair<LeafNode, BiMap<BlockPointer, BlockPointer>> drillDownToLeaf(
            @NotNull BlockPointer pRoot,
            @NotNull String sk,
            boolean recordPath) {

        BlockPointer father = pRoot;

        // path
        BiMap<BlockPointer, BlockPointer> path = null;

        if (recordPath) {
            // create path bi-map
            path = HashBiMap.create();
        }

        BtreeNode node = getBtreeNode(pRoot);
        while (node.getNodeType() != NodeType.LEAF) {
            int i = ((NonLeafNode) node).findSmallerIndexStSkLeqKey(sk);

            if (i == -1) {
                // no key satisfy sk <= key
                // get last non null pointer
                BlockPointer pointer = ((NonLeafNode) node).getLastNonNullPointer();
                node = getBtreeNode(pointer);
            } else if (sk.equals(node.getKey(i))) {
                node = getBtreeNode((BlockPointer) node.getPointer(i+1));
            } else {
                node = getBtreeNode((BlockPointer) node.getPointer(i));
            }
            
            if (recordPath) {
                path.put(father, node.getBlockPointer());
                father = node.getBlockPointer();
            }
        }

        return new Pair<>((LeafNode) node, path);
    }


    /**
     * find search key in B+ tree
     * @param pRoot root pointer
     * @param sk search key
     * @return Pair.first   : LeafNode <br>
     *         Pair.second  : smallest index such that search key equals key <br>
     *         null if search key not found in B+ tree
     */
    public static Pair<LeafNode, Integer> find(BlockPointer pRoot, String sk) {

        LeafNode leafNode = drillDownToLeaf(pRoot, sk, false).first;

        int i = leafNode.findLeastIndexStKeyEqSk(sk);
        if (i == 0) {
            return null;
        }

        return new Pair<>(leafNode, i);
    }

    /**
     * get record data
     * @param recordPointer record pointer
     * @param tableMeta table meta
     * @return record data
     */
    private static RecordData getRecordData(RecordPointer recordPointer, TableMeta tableMeta) {
        DataBlock dataBlock = DataBlockBuffer.getInstance().get(recordPointer.getBlockPointer());
        return dataBlock.getRecordData(recordPointer.getRecordInnerPointer(), tableMeta);
    }

    /**
     * get aLL records whose search key is sk
     * @param pRoot root of B+ search tree
     * @param sk search key
     * @param tableMeta table meta
     * @return list of records found whose search key is sk
     */
    public static List<RecordData> getAllRecords(BlockPointer pRoot, String sk, TableMeta tableMeta) {
        // initialize result
        List<RecordData> res = new ArrayList<>();

        // find in B+ search tree
        Pair<LeafNode, Integer> findRt = find(pRoot, sk);

        // sk not found in B+ tree
        if (findRt == null) {
            return res;
        }

        LeafNode leafNode = findRt.first;
        int i = findRt.second;

        boolean done = false;

        // walk forward to find all records whose search key is sk
        while (!done && leafNode != null) {
            while (i <= leafNode.getUsedSkPtPartsNum()
                    && leafNode.getKey(i).compareTo(sk) <= 0) {
                // get data block
                RecordPointer recordPointer = (RecordPointer) leafNode.getPointer(i);
                RecordData recordData = getRecordData(recordPointer, tableMeta);
                res.add(recordData);

                // walk forward
                i++;
            }

            if (i > leafNode.getUsedSkPtPartsNum()) {
                // find in the next node
                leafNode = (LeafNode) getBtreeNode(leafNode.getTailPointer());
                i = 1;
            } else {
                done = true;
            }
        }

        return res;
    }

    /**
     * allocate index block, create node and put the node into the newly created block
     * @param tClass node class
     * @return Btree node
     */
    @NotNull
    private static <T extends BtreeNode> T createNode(@NotNull Class<T> tClass) {

        T node;

        // if tClass extends BtreeNode
        if (BtreeNode.class.isAssignableFrom(tClass)) {
            // find space for new leaf node
            BtreeBlock btreeBlock = IndexBlockBuffer.getInstance().newBlock(BlockType.INDEX);

            if (tClass.equals(LeafNode.class)) {
                // create a new leaf node
                node = DynamicCast.cast(new LeafNode(btreeBlock.getBlockPointer()));
            } else if (tClass.equals(InternalNode.class)) {
                // create a new internal node
                node = DynamicCast.cast(new InternalNode(btreeBlock.getBlockPointer()));
            } else if (tClass.equals(RootNode.class)) {
                // create a new internal node
                node = DynamicCast.cast(new RootNode(btreeBlock.getBlockPointer()));
            } else {
                throw new IllegalArgumentException("NodeType class[" + tClass + "] not supported!");
            }
        } else {
            throw new IllegalArgumentException("NodeType class[" + tClass + "] not supported!");
        }

        return node;
    }

    /**
     * @param pRoot
     * @param sk
     * @param pr
     * @return
     */
    public static BlockPointer insert(BlockPointer pRoot, String sk, RecordPointer pr) {

        // tree is empty, create an empty root node
        if (pRoot == null) {
            RootNode rootNode = createNode(RootNode.class);
            rootNode.writeToBuffer();
            return rootNode.getBlockPointer();
        }

        // find the leaf node that should contain search key sk, denoted as L
        Pair<LeafNode, BiMap<BlockPointer, BlockPointer>> result = drillDownToLeaf(pRoot, sk, true);

        LeafNode leafNode = result.first;

        if (leafNode.getUsedSkPtPartsNum() < BtreeNode.B_TREE_LEAF_N - 1) {
            // There is still space to insert
            insertInLeaf(leafNode, sk, pr);
        } else {
            // create L'
            LeafNode leafNode2 = createNode(LeafNode.class);

            // create temp leaf node T
            TmpLeafNode tmpLeafNode = new TmpLeafNode();

            // copy leafNode[1..n-1] into T
            tmpLeafNode.copyFrom(leafNode, 1, BtreeNode.B_TREE_LEAF_N);

            // insert sk-pr into tmpLeafNode
            insertInLeaf(tmpLeafNode, sk, pr);

            // update the chain pointers
            leafNode2.setTailPointer(leafNode.getTailPointer());
            leafNode.setTailPointer(leafNode2.getBlockPointer());

            // copy T[1..ceil(n/2)] into leafNode
            leafNode.copyFrom(tmpLeafNode, 1, 1 + (int) BtreeNode.B_TREE_LEAF_N / 2);

            // copy T[ceil(n/2)..N] into leafNode2
            leafNode2.copyFrom(tmpLeafNode, 1 + (int) BtreeNode.B_TREE_LEAF_N / 2, BtreeNode.B_TREE_LEAF_N + 1);

            // key2 is the smallest key in leafNode2
            String key2 = leafNode2.getKey(1);

            // insert in parent
            insertInParent(leafNode, key2, leafNode2, result.second);
        }

        // TODO
        return null;
    }

    /**
     * insert sk-pr into leaf
     * @param leafNode leaf node
     * @param sk search key
     * @param pr record pointer
     */
    private static void insertInLeaf(LeafNode leafNode, String sk, RecordPointer pr) {
        if (sk.compareTo(leafNode.getKey(1)) < 0) {
            leafNode.insert(sk, pr, 1);
        } else {
            int k = leafNode.findLeastIndexStKeyGeSk(sk);
            if (k == 0) {
                // not found such k; append sk-pr
                leafNode.addPart(sk, pr);
            } else {
                leafNode.insert(sk, pr, k);
            }
        }
    }

    private static RootNode insertInParent(
            BtreeNode node,
            String key,
            BtreeNode node2,
            BiMap<BlockPointer, BlockPointer> path) {
        if (node.getNodeType() == NodeType.ROOT) {
            // if node is root, create a new root
            RootNode rootNode = createNode(RootNode.class);
            rootNode.setPointer(node.getBlockPointer(), 1);
            rootNode.setKey(key, 1);
            rootNode.setPointer(node2.getBlockPointer(), 2);
            return rootNode;
        }

        BtreeNode p = parent(node, path);
        if (p.getUsedSkPtPartsNum() < p.defineNodesNumN()) {
            
        }
    }

    private static BtreeNode parent(BtreeNode son, BiMap<BlockPointer, BlockPointer> path) {
        BlockPointer pParent = path.inverse().get(son.getBlockPointer());
        if (pParent == null) {
            return null;
        }

        return getBtreeNode(pParent);
    }
}
