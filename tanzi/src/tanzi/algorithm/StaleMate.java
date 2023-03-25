package tanzi.algorithm;

import tanzi.model.Piece;
import tanzi.staff.BoardRegistry;

import java.util.ArrayList;

public abstract class StaleMate {

    /**
     * This method can calculate whether it is stalemate for the specified BR. This method
     * should be called after executing each successful move to the BR.
     * <p>
     * Currently, this method algorithm can check for stalemate by insufficient material or
     * two lonely king stalemate.
     *
     * @param nextColor It is the color of the army who is making the next move. This method
     *                  checks whether any of that colored army can make any move at all by
     *                  all chess rules.
     * @param br        The BR which executed the last move already.
     */
    public static boolean isStaleMate(int nextColor, BoardRegistry br) {

        // Check whether we have two kings and any other type of piece presents on the board.
        // If the other piece is either a bishop or a knight, then it is stalemate.
        // Another way it can be a stalemate, if there is only two kings on the board.
        int pieceCount = br.pieceCount();
        if (pieceCount <= 4) {
            // Two lonely kings.
            if (pieceCount == 2) return true;

            // When there are only pieces on the board, this checks for cases where both army
            // has one bishop/knight each and still it's stalemate.
            boolean whiteBishop = br.pieceOf(Piece.BISHOP, Piece.COLOR_WHITE).size() == 1;
            boolean whiteKnight = br.pieceOf(Piece.KNIGHT, Piece.COLOR_WHITE).size() == 1;
            boolean blackBishop = br.pieceOf(Piece.BISHOP, Piece.COLOR_BLACK).size() == 1;
            boolean blackKnight = br.pieceOf(Piece.KNIGHT, Piece.COLOR_BLACK).size() == 1;
            if ((whiteBishop || whiteKnight) && (blackBishop || blackKnight)) return true;

            // Insufficient material when there are only three pieces including both kings.
            return br.pieceOf(Piece.BISHOP).size() == 1 || br.pieceOf(Piece.KNIGHT).size() == 1;
        }

        // Get the squares of the army for which we are checking whether it is stalemate or not
        ArrayList<String> squaresOfArmy = br.squaresOfArmy(nextColor);

        // After getting all the pieces of the army, let's check for each piece whether they can
        // make move regarding pic check. If any of them can do, then it's not a stalemate yet.
        for (String square : squaresOfArmy) {
            Piece piece = br.piece(square);

            ArrayList<String> pieceSquares = GeometryEngineer.validSquare(piece.type, piece.color, piece.currentSquare(), true, br);
            for (String destSquare : pieceSquares) {
                if (Arbiter.pieceCanGo(piece.currentSquare(), destSquare, true, br)) return false;
            }
        }

        return true;
    }

}
