//package com.gyw.util;
//
//import com.gyw.storageManager.fileManager.index.bplusTree.nodeBlock.LeafBlock;
//import com.gyw.types.RecordPointer;
//import com.gyw.storageManager.fileManager.index.bplusTree.SearchKeyPointerPair;
//
//public class NodeBlockTest {
//    public static void main(String[] args) {
//        LeafBlock nodeBlock = new LeafBlock(0);
//        nodeBlock.createEmptyBlock();
//
//        SearchKeyPointerPair searchKeyPointerPair1
//                = new SearchKeyPointerPair("Brandt", new RecordPointer(0, (short)32));
//
//
//        SearchKeyPointerPair searchKeyPointerPair2
//                = new SearchKeyPointerPair("Califieri", new RecordPointer(0, (short)1024));
//
//        SearchKeyPointerPair searchKeyPointerPair3
//                = new SearchKeyPointerPair("Crick", new RecordPointer(0, (short)200));
//
//        nodeBlock.appendSearchKeyPointerPairOp(searchKeyPointerPair1);
//        nodeBlock.appendSearchKeyPointerPairOp(searchKeyPointerPair2);
//        nodeBlock.appendSearchKeyPointerPairOp(searchKeyPointerPair3);
//
//        nodeBlock.flush();
//    }
//}
