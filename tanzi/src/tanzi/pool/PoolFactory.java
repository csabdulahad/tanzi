package tanzi.pool;

import java.util.ArrayList;

/*
 * an implementation of pool factory design pattern.
 * */

public abstract class PoolFactory<T> {

    protected ArrayList<T> pool;
    protected int poolSize;

    public PoolFactory(int poolSize) {
        this.poolSize = poolSize;
        this.pool = new ArrayList<>(poolSize);
    }

    public T get() {
        int size = pool.size();
        return size == 0 ? createObject() : pool.remove(size - 1);
    }

    public void recycle(T object) {
        if (pool.size() >= poolSize) return;
        pool.add(object);
    }

    public final int size() {
        return this.pool.size();
    }

    public abstract T createObject();

}
