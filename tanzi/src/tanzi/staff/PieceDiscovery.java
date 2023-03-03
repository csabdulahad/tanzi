package tanzi.staff;

import tanzi.algorithm.EnPassant;
import tanzi.algorithm.King;
import tanzi.model.EnPasser;
import tanzi.model.MoveMeta;
import tanzi.model.Piece;

public class PieceDiscovery {

    /*
     * This method can calculate which piece of the board can make the move as specified
     * by MoveMeta object according to all the chess rules.
     *
     * It checks whether it is castle move. If so, then it checks whether the king can
     * make the castle move given the situation in the board registry.
     *
     * For a pawn, it also takes enPassant into consideration. It asks the arbiter if there is
     * any EnPasser object available for this pawn move and takes actions based on the EnPasser
     * object.
     *
     * */
    public static Piece discover(MoveMeta moveMeta, Arbiter ar) {
        int type = moveMeta.type;
        int color = moveMeta.color;

        BoardRegistry br = ar.getBR();

        if (moveMeta.castle) {
            Piece king = br.getPiece(Piece.PIECE_KING, moveMeta.color);
            String[] castleMeta = King.getCastleMeta(moveMeta);

            int castleResult = King.canCastle(king.getCurrentSquare(), castleMeta[2], ar, br);
            int castleType = moveMeta.shortCastle ? 1 : 2;
            if (castleResult == castleType) return br.getPiece(king.getCurrentSquare());
            return null;
        }

        // can I take down any enemy EnPasser?
        if (type == Piece.PIECE_PAWN) {
            EnPasser enemyEnPasser = ar.getEnPasserFor(color);
            if (enemyEnPasser != null && enemyEnPasser.intermediateSquare.equals(moveMeta.destSquare)) {
                moveMeta.enPasserTaker = EnPassant.whoIsEnPasserTaker(enemyEnPasser.taker, moveMeta);
                if (moveMeta.enPasserTaker == null) return null;
                return br.getPiece(moveMeta.enPasserTaker);
            }
        }

        return ar.getPiece(moveMeta, br);
    }

}
