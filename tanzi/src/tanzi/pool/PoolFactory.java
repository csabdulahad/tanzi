package tanzi.pool;

import java.util.ArrayList;

/**
 * An implementation of pool factory design pattern.
 */

public abstract class PoolFactory<T> {

    protected ArrayList<T> pool;
    protected int poolSize;

    public PoolFactory(int poolSize) {
        this.poolSize = poolSize;
        this.pool = new ArrayList<>(poolSize);
    }

    public T getObj() {
        int size = pool.size();
        return size == 0 ? createObj() : pool.remove(size - 1);
    }

    public void recycleObj(T object) {
        if (pool.size() >= poolSize) return;
        pool.add(object);
    }

    public final int size() {
        return this.pool.size();
    }

    protected abstract T createObj();

}
