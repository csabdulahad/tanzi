package tanzi.pool.meta;

import tanzi.pool.PiecePool;

public class PieceMeta {

    private static int REQUEST = 0;
    private static int CREATED = 0;
    private static int HIT = 0;
    private static int RECYCLE_REQUEST = 0;
    private static int RECYCLED = 0;
    private static int RECYCLE_MISSED = 0;

    public static void log() {
        double unit = RECYCLED / 100d;

        System.out.println("Piece Pool Details:");
        logCommonParam(unit, REQUEST, CREATED, RECYCLE_REQUEST, RECYCLED, RECYCLE_MISSED, HIT);
        System.out.println("Piece in pool: " + PiecePool.getInstance().size());
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

    static void logCommonParam(double unit, int request, int created, int recycleRequest, int recycled, int recycleMissed, int hit) {
        System.out.println("Request: " + request);
        System.out.println("Created/Missed: " + created);
        System.out.println("Recycle Request: " + recycleRequest);
        System.out.println("Recycled: " + recycled);
        System.out.println("Recycle Missed: " + recycleMissed);
        System.out.println("Hit : " + hit);
        System.out.printf("Hit/Miss: %.3f/%.3f\n", hit /unit, recycleMissed /unit);
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
