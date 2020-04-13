package com.gyw.parms;

public class Parm {

    /**
     * Block Default Size = 8K
     */
    public static final int BLOCK_DEFAULT_SIZE = 8 * 1024;

    /**
     * Offset pointer length in record and block
     */
    public static final int OFFSET_POINTER_LEN = 2;

    /**
     * Length pointer length in record and block
     */
    public static final int LEN_POINTER_LEN = 2;

    /**
     * Offset-length pointer length
     * Position
     */
    public static final int OFFSET_LEN_POINTER_LEN =
            OFFSET_POINTER_LEN + LEN_POINTER_LEN;

    /**
     * Data filename
     */
    public static final String DATA_FILENAME = "data";

    /**
     * Index filename
     */
    public static final String INDEX_FILENAME = "index";

    /**
     * Search key size
     */
    public static final int SEARCH_KEY_SIZE = 12;

    /**
     * block offset pointer length
     */
    @Deprecated
    public static final int BLOCK_POINTER_LEN = 4;

    /**
     * record pointer length
     */
    public static final int RCRD_PT_LEN = BLOCK_POINTER_LEN + OFFSET_POINTER_LEN;

    /**
     * search key number field length
     */
    public static final int SEARCH_KEY_NUM_FIELD_LEN = 2;

}
