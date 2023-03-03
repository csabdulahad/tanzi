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
    public BufferedBR get() {
        BRMeta.request();
        int size = pool.size();

        if (size == 0) {
            return createObject();
        } else {
            BRMeta.hit();
            BufferedBR bufferedBR = pool.remove(size - 1);
            bufferedBR.__clear();
            return bufferedBR;
        }
    }

    @Override
    public void recycle(BufferedBR bufferedBR) {
        BRMeta.recycleRequest();
        if (pool.size() < poolSize) {
            bufferedBR.__clear();
            pool.add(bufferedBR);
            BRMeta.recycled();
        } else {
            BRMeta.recycleMissed();
        }
    }

    @Override
    public BufferedBR createObject() {
        BRMeta.created();
        return new BufferedBR();
    }

}
