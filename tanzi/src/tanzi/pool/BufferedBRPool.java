package tanzi.pool;

import tanzi.pool.meta.BRMeta;
import tanzi.staff.BufferedBR;

public class BufferedBRPool extends PoolFactory<BufferedBR> {

    private static BufferedBRPool INSTANCE;
    private static final int POOL_SIZE = 3;

    private BufferedBRPool(int maxSize) {
        super(maxSize);
    }

    public static BufferedBRPool getInstance() {
        if (INSTANCE == null) INSTANCE = new BufferedBRPool(POOL_SIZE);
        return INSTANCE;
    }

    @Override
    public BufferedBR getObj() {
        BRMeta.request();
        int size = pool.size();

        if (size == 0) {
            return createObj();
        } else {
            BRMeta.hit();
            return pool.remove(size - 1);
        }
    }

    @Override
    public void recycleObj(BufferedBR bufferedBR) {
        BRMeta.recycleRequest();
        if (pool.size() >= poolSize) {
            BRMeta.recycleMissed();
            return;
        }

        bufferedBR.__clear();
        pool.add(bufferedBR);
        BRMeta.recycled();
    }

    public static void recycle(BufferedBR bbr) {
        BufferedBRPool.getInstance().recycleObj(bbr);
    }

    public static BufferedBR get() {
        return BufferedBRPool.getInstance().getObj();
    }

    @Override
    protected BufferedBR createObj() {
        BRMeta.created();
        return new BufferedBR();
    }

}
