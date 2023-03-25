package tanzi.staff;

import tanzi.model.Piece;
import tanzi.staff.BoardRegistry;
import tanzi.pool.BufferedBRPool;
import tanzi.pool.PiecePool;

import java.util.Map;

/**
 * This class particularly does nothing special but introduces a safety net to the system.
 * Algorithms that play with Board Registry (BR), and it is critical to make and revert changes
 * to the BR as they happen. if we forget to revert any change or any undo to the BR, then entire
 * system call collapse.
 * <p>
 * Algorithms which use BR and make changes for calculations need to use this BufferedBR for safety
 * which guarantees that the main game's BR is unharmed by any of algorithms. This BufferedBR type
 * helps make sure we are always using Buffered BR to run algorithms; not mistakenly changing/using
 * the original BR.
 * <p>
 * Note: Any use of this BufferedBR must be recycled to save memory and improve overall performance.
 */

public class BufferedBR extends BoardRegistry {

    public void recycle() {
        // recycle the pieces
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet())
            PiecePool.recycle(entry.getValue());

        // now pass the BufferedBR to the BufferedBRPool
        BufferedBRPool.recycle(this);
    }

    public static void copySquarePieceMap(Map<String, Piece> source, Map<String, Piece> copy) {
        for (Map.Entry<String, Piece> entry : source.entrySet())
            copy.put(entry.getKey(), Piece.clone(entry.getValue()));
    }

}
