package tanzi.app;

import java.util.ArrayList;
import java.util.List;

/**
 * A pool for improving performance of processing the pieces of graphical form. Unlike
 * other object-oriented pool design pattern implementation, this class has no default
 * private constructor. It is the responsibility of the application to maintain its
 * singleton instance. This is so that the factory for this class can be setup more
 * easily than making it go through singleton pattern with very complex code structure`.
 *
 * @param <B> The bitmap type representing the piece to be maintained by the pool. It must
 *          be as same type as the SquareDU uses to draw the bitmap on the canvas.
 */

public class PieceBitmapPool<B> {

    /**
     * Here we have 32 x 3 = 96 as pool size to allow caching three chess board
     * pieces. One for game BR, one for internal algorithm usage and one extra
     * to allow efficient caching.
     */
    private static final int POOL_SIZE = 96;

    private final List<PieceBitmap<B>> piecePool;

    private final PieceBitmapFactory<PieceBitmap<B>> factory;

    public PieceBitmapPool(PieceBitmapFactory<PieceBitmap<B>> factory) {
        this.factory = factory;
        piecePool = new ArrayList<>(POOL_SIZE);
    }

    public PieceBitmap<B> get(int type, int color) {
        // search through the pool to find whether we have the requested type of PieceBitmap
        // if so then return it from the pool
        if (piecePool.size() > 0) {
            for (PieceBitmap<B> pieceBitmap : piecePool)
                if (pieceBitmap.type == type && pieceBitmap.color == color)
                    return piecePool.remove(piecePool.indexOf(pieceBitmap));
        }

        // requested PieceBitmap isn't in the pool or the pool is empty. so let's crate one
        return create(type, color);
    }

    public void recycle(PieceBitmap<B> object) {
        if (piecePool.size() < POOL_SIZE) piecePool.add(object);
    }

    private PieceBitmap<B> create(int type, int color) {
        return factory.produce(type, color);
    }

}
