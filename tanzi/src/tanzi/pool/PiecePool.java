package tanzi.pool;

import tanzi.model.Piece;
import tanzi.pool.meta.PieceMeta;

/**
 * this pool class for piece really reduces object initialization and redundant piece objects.
 */

public class PiecePool extends PoolFactory<Piece> {

    private static PiecePool INSTANCE;

    // a chess board has 64 squares. here we have 150% of amount 64 to make a stable piece pool
    private static final int POOL_SIZE = 96;

    private PiecePool() {
        super(POOL_SIZE);
    }

    public static PiecePool getInstance() {
        if (INSTANCE == null) INSTANCE = new PiecePool();
        return INSTANCE;
    }

    @Override
    public Piece getObj() {
        PieceMeta.request();
        int size = pool.size();

        if (size == 0) {
            return createObj();
        } else {
            PieceMeta.hit();
            return pool.remove(size - 1);
        }
    }

    @Override
    public void recycleObj(Piece piece) {
        PieceMeta.recycleRequest();

        if (pool.size() >= poolSize) {
            PieceMeta.recycleMissed();
            return;
        }

        // reset the piece which may have already been used previously before putting back in app.pool
        piece.color = -1;
        piece.type = -1;
        piece.setCurrentSquare(null);
        piece.setPreviousSquare(null);
        pool.add(piece);

        PieceMeta.recycled();
    }

    public static Piece get() {
        return PiecePool.getInstance().getObj();
    }

    public static void recycle(Piece piece) {
        PiecePool.getInstance().recycleObj(piece);
    }

    @Override
    public Piece createObj() {
        PieceMeta.created();
        return new Piece();
    }

}
