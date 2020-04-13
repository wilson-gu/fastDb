import com.google.gson.Gson;
import com.gyw.storageManager.bufferManager.IndexBlockBuffer;
import com.gyw.storageManager.fileManager.block.BlockAllocator;
import com.gyw.storageManager.fileManager.data.record.RecordData;
import com.gyw.storageManager.fileManager.index.bplusTree.Btree;
import com.gyw.storageManager.fileManager.index.bplusTree.BtreeBlock;
import com.gyw.storageManager.fileManager.tablespace.NumericFieldMeta;
import com.gyw.storageManager.fileManager.tablespace.TableMeta;
import com.gyw.storageManager.fileManager.tablespace.VarcharFieldMeta;
import com.gyw.storageManager.pointers.BlockPointer;
import com.gyw.storageManager.pointers.RecordPointer;
import com.gyw.util.Timer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BtreeTest {

    private static final Logger logger = LoggerFactory.getLogger(BtreeTest.class);
    private static Gson gson = new Gson();


    private static TableMeta tableMeta;

    @BeforeAll
    static void init() throws IOException {
        logger.debug("BeforeAll => BlockAllocator.init()");
        BlockAllocator.init();

        tableMeta = new TableMeta("person");
        tableMeta.setFieldMeta(new VarcharFieldMeta("id", 5));
        tableMeta.setFieldMeta(new VarcharFieldMeta("name", 20));
        tableMeta.setFieldMeta(new VarcharFieldMeta("dept", 20));
        tableMeta.setFieldMeta(new NumericFieldMeta("salary", 8, 1));

        // read  Btree root blocks when starts up
        IndexBlockBuffer.getInstance().get(new BlockPointer(7 * BtreeBlock.LEN));

    }

    @AfterAll
    static void finish() {
//        logger.debug("AfterAll => DataBlockBuffer.getInstance().invalidateAll()");
//        DataBlockBuffer.getInstance().invalidateAll();
//        IndexBlockBuffer.getInstance().invalidateAll();
    }


    @Test
    void findTest() throws IOException {
        BlockPointer pRoot = new BlockPointer(7 * BtreeBlock.LEN);
        String[] sks = new String[]{"Katz", "Kim", "Gold", "Nut", "Brandt"};
        List<List<RecordData>> findResults = new ArrayList<>();


        Timer timer = new Timer();
        timer.tic();
        for (String sk : sks) {
            findResults.add(Btree.getAllRecords(pRoot, sk, tableMeta));
        }
        long timeCost = timer.toc();

        for (List<RecordData> findResult : findResults) {
            logger.debug(gson.toJson(findResult));
        }

        logger.debug("TimeCost = " + timeCost + "ms");
    }


    @Test
    void insertTest() throws IOException {
        BlockPointer pRoot = new BlockPointer(7 * BtreeBlock.LEN);
        String sk = "Lamport";
        RecordPointer pr = new RecordPointer(1024, 100);


        Timer timer = new Timer();
        timer.tic();

        Btree.insert(pRoot, sk, pr);

        long timeCost = timer.toc();
        logger.debug("TimeCost = " + timeCost + "ms");

//        Pair<LeafNode, Integer> findRt = Btree.find(pRoot, "");
//        logger.debug(gson.toJson(findRt));
    }

}
