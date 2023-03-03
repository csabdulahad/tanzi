package tanzi.staff;

import tanzi.model.Piece;
import tanzi.pool.BufferedBRPool;
import tanzi.pool.PiecePool;

import java.util.Map;

/*
 * this class does nothing but introduce a safety to the system. algorithms play with Board Registry
 * (BR), and it is critical to make and revert changes to BR as they happen. if we forget to
 * revert any change or any undo to the BR, then entire system call collapse.
 *
 * algorithms which use BR and make changes for calculations need to use this BufferedBR for safety
 * which guarantees that the main game's BR is unharmed by any of algorithms. this BufferedBR type
 * helps make sure we are always using Buffered BR to run algorithms; not mistakenly changing/using
 * the original BR.
 * */

public class BufferedBR extends BoardRegistry {

    public void recycle() {
        // recycle the pieces
        PiecePool piecePool = PiecePool.getInstance();
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            piecePool.recycle(entry.getValue());
        }

        // now pass the BufferedBR to the BufferedBRPool
        BufferedBRPool.getInstance().recycle(this);
    }

    public static void copySquarePieceMap(Map<String, Piece> source, Map<String, Piece> copy) {
        for (Map.Entry<String, Piece> entry : source.entrySet())
            copy.put(entry.getKey(), Piece.clone(entry.getValue()));
    }

}
