package tanzi.pool.meta;

import tanzi.pool.BufferedBRPool;

public class BRMeta {

    private static int REQUEST = 0;
    private static int CREATED = 0;
    private static int HIT = 0;
    private static int RECYCLE_REQUEST = 0;
    private static int RECYCLED = 0;
    private static int RECYCLE_MISSED = 0;

    public static void log() {
        double unit = RECYCLED / 100d;

        System.out.println("\nBoardRegistry Pool Details:");
        PieceMeta.logCommonParam(unit, REQUEST, CREATED, RECYCLE_REQUEST, RECYCLED, RECYCLE_MISSED, HIT);
        System.out.println("BR in pool: " + BufferedBRPool.getInstance().size());
        System.out.println(" ");
    }

    public static void reset() {
        REQUEST = 0;
        CREATED = 0;
        HIT = 0;
        RECYCLE_REQUEST = 0;
        RECYCLED = 0;
        RECYCLE_MISSED = 0;
    }

    public static void request() {
        REQUEST++;
    }

    public static void hit() {
        HIT++;
    }

    public static void recycleRequest() {
        RECYCLE_REQUEST++;
    }

    public static void recycled() {
        RECYCLED++;
    }

    public static void recycleMissed() {
        RECYCLE_MISSED++;
    }

    public static void created() {
        CREATED++;
    }
}
