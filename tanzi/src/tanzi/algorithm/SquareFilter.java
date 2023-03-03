package tanzi.algorithm;


import tanzi.model.Piece;
import tanzi.staff.BoardRegistry;

import java.util.List;

/*
 * this class has methods that filter squares for selections such as when considering a possible
 * coming square or destination square for a piece type it can validate that.
 * */

public class SquareFilter {

    /**
     * this filter method evaluates whether a square can be appropriate for one of the destination
     * squares for a piece to move by considering whether it is killing its own army or landing
     * on an empty square.
     * <p>
     * If returns true to block the ITERATION-GEOMETRY nature of finding valid squares for bishop or
     * rook in GeometryEngineer class. Look how it is called from validSquare of GeometryEngineer class.
     * <p>
     * It returns false to allow geometry engineer to continue lookup on finding more valid squares.
     * <p>
     * NOTE : the square arguments to these methods are the geometrically calculated squares which
     * are not evaluated as valid squares. so these methods do that. they run checks on given squares
     * & on finding valid squares they add those square to the argument ArrayList.
     */
    public static boolean filterForIterativeSquareFinding(int color, String square, boolean ownArmyCheck, List<String> squareList, BoardRegistry br) {

        if (square == null) return true;

        if (ownArmyCheck && br.killingOwnArmy(square, color)) return true;

        Piece piece = br.getPiece(square);
        if (piece != null && piece.color != color) {
            // we hit an enemy piece for the square; say we can take upon him; and return true
            // for blocking further look-up alongside/diagonal by Geometry Engineer
            squareList.add(square);
            return true;
        }

        /*
         * add the square as it is an empty valid square for bishop to move & keep on calculating
         * by returning false
         * */
        squareList.add(square);
        return false;
    }

    /*
     * same idea goes with these methods
     * */
    public static void filterSquare(int color, String square, boolean ownArmyCheck, List<String> squareList, BoardRegistry br) {
        if (square == null) return;

        if (ownArmyCheck) {
            if (!br.killingOwnArmy(square, color)) squareList.add(square);
        } else squareList.add(square);
    }

    /**
     * For any given square, these filtering methods calculate whether the piece on the square is
     * eligible by type, color & chess rules to become one of the possible pieces to a focus square.
     * <p>
     * For example, when you want to know whether a piece can make move to the focus square along any
     * specified direction or not.
     */

    public static boolean filterPossibleSquareToFocusSquare(int pieceType, int color, String square, List<String> squareList, BoardRegistry br) {

        // since no valid square give to calculate; so we assume we can't go along given diagonal/side
        if (square == null) return true;

        // we have a square to calculate. let's see whether there is any piece on the square
        Piece piece = br.getPiece(square);

        // for now along the given diagonal/side we haven't found any piece; maybe we can find it next time
        if (piece == null) return false;

        // let's see whether it is from own army or not
        if (piece.color != color)
            // as it is enemy piece, so we block further look-up as we don't need to
            return true;

        // now check whether the piece is of specified type from own army or any enemy pieces
        if (piece.type != pieceType)
            // not specified piece; don't go anymore as no piece can go from the diagonal/side-wise to focus square
            return true;

        // finally we found a specified piece from own army & added to list
        squareList.add(square);

        // stop next check as we don't need to
        return true;
    }

    public static void filterPossibleKnightSquareToFocusSquare(int color, String square, List<String> squareList, BoardRegistry br) {
        if (!br.anyPieceOn(square)) return;

        Piece piece = br.getPiece(square);
        if (piece.color == color && piece.type == Piece.PIECE_KNIGHT) squareList.add(square);
    }

}