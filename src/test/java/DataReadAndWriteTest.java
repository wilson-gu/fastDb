//import com.google.common.collect.ArrayListMultimap;
//import com.google.common.collect.Multimap;
//import com.google.gson.Gson;
//import com.gyw.storageManager.bufferManager.DataBlockBuffer;
//import com.gyw.storageManager.bufferManager.IndexBlockBuffer;
//import com.gyw.storageManager.fileManager.block.BlockAllocator;
//import com.gyw.storageManager.fileManager.block.BlockSerializable;
//import com.gyw.storageManager.fileManager.block.BlockType;
//import com.gyw.storageManager.fileManager.data.DataBlock;
//import com.gyw.storageManager.fileManager.data.record.Record;
//import com.gyw.storageManager.fileManager.data.record.RecordData;
//import com.gyw.storageManager.fileManager.index.bplusTree.BtreeBlock;
//import com.gyw.storageManager.fileManager.index.bplusTree.node.BtreeNode;
//import com.gyw.storageManager.fileManager.index.bplusTree.node.InternalNode;
//import com.gyw.storageManager.fileManager.index.bplusTree.node.LeafNode;
//import com.gyw.storageManager.fileManager.index.bplusTree.node.RootNode;
//import com.gyw.storageManager.fileManager.tablespace.NumericFieldMeta;
//import com.gyw.storageManager.fileManager.tablespace.TableMeta;
//import com.gyw.storageManager.fileManager.tablespace.VarcharFieldMeta;
//import com.gyw.storageManager.pointers.BlockPointer;
//import com.gyw.storageManager.pointers.RecordPointer;
//import gyw.excel.ArrayMatrixBasedExcel;
//import gyw.excel.Excel;
//import gyw.excel.ExcelUtil;
//import gyw.matrix.basicMatrix.Matrix;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//
//
//public class DataReadAndWriteTest {
//
//    private static final Logger logger = LoggerFactory.getLogger(DataReadAndWriteTest.class);
//    private static Gson gson = new Gson();
//
//    private static final int COL_ID             = ExcelUtil.letterIndexToNumIndex("A");
//    private static final int COL_NAME           = ExcelUtil.letterIndexToNumIndex("B");
//    private static final int COL_DEPT           = ExcelUtil.letterIndexToNumIndex("C");
//    private static final int COL_SALARY         = ExcelUtil.letterIndexToNumIndex("D");
//    private static final int COL_BLOCK_ID       = ExcelUtil.letterIndexToNumIndex("E");
//    private static final int COL_BLOCK_POS      = ExcelUtil.letterIndexToNumIndex("F");
//    private static final int COL_RECORD_POS     = ExcelUtil.letterIndexToNumIndex("G");
//    private static final int COL_INDEX_BLOCK_ID = ExcelUtil.letterIndexToNumIndex("H");
//
//    private static TableMeta tableMeta;
//
//    @BeforeAll
//    static void init() throws IOException {
//        logger.debug("BeforeAll => BlockAllocator.init()");
//        BlockAllocator.init();
//
//        tableMeta = new TableMeta("person");
//        tableMeta.setFieldMeta(new VarcharFieldMeta("id", 5));
//        tableMeta.setFieldMeta(new VarcharFieldMeta("name", 20));
//        tableMeta.setFieldMeta(new VarcharFieldMeta("dept", 20));
//        tableMeta.setFieldMeta(new NumericFieldMeta("salary", 8, 1));
//
//    }
//
//    @AfterAll
//    static void finish() {
//        logger.debug("AfterAll => DataBlockBuffer.getInstance().invalidateAll()");
//        DataBlockBuffer.getInstance().invalidateAll();
//        IndexBlockBuffer.getInstance().invalidateAll();
//    }
//
//    @Test
//    void prepareData() throws IOException {
//
//
//        // read person data from excel
//        Excel excel = new ArrayMatrixBasedExcel("person.xlsx");
//        excel.readContentBySheetIndex(0);
//
//        Matrix<String> persons = excel.getContent();
//        logger.debug("Person excel row size = " + String.valueOf(persons.getRowSize()));
//
//        for (int i = 2; i <= persons.getRowSize(); i++) {
//            // get block
//            int blockId = Integer.valueOf(persons.getElement(i, COL_BLOCK_ID));
//            DataBlock block = DataBlockBuffer.getInstance().get(new BlockPointer(blockId * BtreeBlock.LEN));
//
//            if (block == null) {
//                block = DataBlockBuffer.getInstance().newBlock().second;
//            }
//
//            // prepare record
//            RecordData recordData = new RecordData();
//            String id = persons.getElement(i, COL_ID).trim();
//            String name = persons.getElement(i, COL_NAME).trim();
//
//            recordData.setFieldValue("id", id);
//            recordData.setFieldValue("name", name);
//            recordData.setFieldValue("dept", persons.getElement(i, COL_DEPT).trim());
//            recordData.setFieldValue("salary", new BigDecimal(persons.getElement(i, COL_SALARY).trim()));
//
//            Record record = new Record(tableMeta);
//            record.setRecordData(recordData);
//
//            // insert record
//            RecordPointer recordPointer = block.insertRecord(record);
//            logger.debug("Name = [" + name + "], " + recordPointer.toString());
//        }
//    }
//
//    private RecordData readRecordData(RecordPointer recordPointer) {
//        DataBlock block = DataBlockBuffer.getInstance().get(recordPointer.getBlockPointer());
//        return block.getRecordData(recordPointer.getRecordInnerPointer(), tableMeta);
//    }
//
//    private BtreeNode readBtreeNode(BlockPointer blockPointer) {
//        BtreeBlock block = IndexBlockBuffer.getInstance().get(blockPointer);
//        BlockSerializable blockSerializable = block.read();
//        return (BtreeNode) blockSerializable;
//    }
//
//    @Test
//    void readData() {
//
////        logger.debug(new Gson().toJson(readRecordData(new RecordPointer(0x0000, (short)0x1FA9))));
////        logger.debug(new Gson().toJson(readRecordData(new RecordPointer(0x2000, (short)0x1FAD))));
////        logger.debug(new Gson().toJson(readRecordData(new RecordPointer(0x4000, (short)0x1FAE))));
////        logger.debug(new Gson().toJson(readRecordData(new RecordPointer(0x0000, (short)0x1F81))));
//
//
//        for (int i = 0; i < 8; i++) {
//            logger.debug("i = " + i);
//            BtreeNode btreeNode = readBtreeNode(new BlockPointer(i * BtreeBlock.LEN));
//            logger.debug(gson.toJson(btreeNode));
//        }
//    }
//
//    @Test
//    void prepareIndex() throws IOException {
//        for (int i = 0; i < 5; i++) {
//            IndexBlockBuffer.getInstance().newBlock(BlockType.INDEX);
//        }
//
//        // read person data from excel
//        Excel excel = new ArrayMatrixBasedExcel("person.xlsx");
//        excel.readContentBySheetIndex(0);
//
//        Matrix<String> persons = excel.getContent();
//        logger.debug("Person excel row size = " + String.valueOf(persons.getRowSize()));
//
//        // create indexBlockIdMap
//        Multimap<Integer, Integer> indexBlockIdMap = ArrayListMultimap.create();
//
//        for (int i = 2; i <= persons.getRowSize(); i++) {
//            // prepare leaf block index
//            String indexBlockId = persons.getElement(i, COL_INDEX_BLOCK_ID).trim();
//
//            indexBlockIdMap.put(Integer.valueOf(indexBlockId), i);
//        }
//
//        Map<Integer, BtreeNode> bTreeNodes = new HashMap<>();
//        for (int i = 0; i < 5; i++) {
//            BtreeNode node = new LeafNode();
//            bTreeNodes.put(i, node);
//        }
//
//        // create btreeNode map
//        for (Integer blockId : indexBlockIdMap.keySet()) {
//            LeafNode node = (LeafNode) bTreeNodes.get(blockId);
//            Collection<Integer> rows = indexBlockIdMap.get(blockId);
//
//            for (Integer row: rows) {
//                String name = persons.getElement(row, COL_NAME).trim();
//                int blockPos = Integer.parseInt(persons.getElement(row, COL_BLOCK_POS).trim().substring(2), 16);
//                int recordPos = Integer.parseInt(persons.getElement(row, COL_RECORD_POS).trim().substring(2), 16);
//                node.addPart(name, new RecordPointer(blockPos, recordPos));
//            }
//        }
//
//        // tail pointers
//        setTailPointer(bTreeNodes, 0, 1);
//        setTailPointer(bTreeNodes, 1, 2);
//        setTailPointer(bTreeNodes, 2, 3);
//        setTailPointer(bTreeNodes, 3, 4);
//
//
//        for (int blockId = 0; blockId < 5; blockId++) {
//            BtreeNode  node           = bTreeNodes.get(blockId);
//            BtreeBlock bplusTreeBlock = IndexBlockBuffer.getInstance().get(new BlockPointer(blockId * BtreeBlock.LEN));
//            bplusTreeBlock.write(node);
//        }
//
//        InternalNode iNode1 = new InternalNode();
//        iNode1.setPointer(new BlockPointer(0 * BtreeBlock.LEN), 1);
//        iNode1.setPointer(new BlockPointer(1 * BtreeBlock.LEN), 2);
//        iNode1.setPointer(new BlockPointer(2 * BtreeBlock.LEN), 3);
//        iNode1.setKey("Einstein", 1);
//        iNode1.setKey("Gold", 2);
//
//        BtreeBlock bplusTreeBlock = IndexBlockBuffer.getInstance().get(new BlockPointer(5 * BtreeBlock.LEN));
//        bplusTreeBlock.write(iNode1);
//
//        InternalNode iNode2 = new InternalNode();
//        iNode2.setPointer(new BlockPointer(3 * BtreeBlock.LEN), 1);
//        iNode2.setPointer(new BlockPointer(4 * BtreeBlock.LEN), 2);
//        iNode2.setKey("Srinivasan", 1);
//
//        bplusTreeBlock = IndexBlockBuffer.getInstance().get(new BlockPointer(6 * BtreeBlock.LEN));
//        bplusTreeBlock.write(iNode2);
//
//        RootNode root = new RootNode();
//        root.setPointer(new BlockPointer(5 * BtreeBlock.LEN), 1);
//        root.setPointer(new BlockPointer(6 * BtreeBlock.LEN), 2);
//        root.setKey("Mozart", 1);
//
//        bplusTreeBlock = IndexBlockBuffer.getInstance().get(new BlockPointer(7 * BtreeBlock.LEN));
//        bplusTreeBlock.write(root);
//    }
//
//    private void setTailPointer(Map<Integer, BtreeNode> btreeNodeMap, int indexBlockId1, int indexBlockId2) {
//        LeafNode block1 = (LeafNode) btreeNodeMap.get(indexBlockId1);
//        LeafNode block2 = (LeafNode) btreeNodeMap.get(indexBlockId2);
//        block1.setTailPointer(new BlockPointer(indexBlockId2 * BtreeBlock.LEN));
//    }
//}
