package tanzi.algorithm;

import tanzi.model.EnPasser;
import tanzi.model.MoveMeta;
import tanzi.model.Piece;
import tanzi.staff.BoardRegistry;

public abstract class PieceDiscovery {

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
    public static Piece discover(MoveMeta moveMeta, BoardRegistry br) {
        int type = moveMeta.type;
        int color = moveMeta.color;

        if (moveMeta.castle) {
            Piece king = br.piece(Piece.KING, moveMeta.color);
            String[] castleMeta = King.getCastleMeta(moveMeta);

            int castleResult = King.canCastle(king.currentSquare(), castleMeta[2], br);
            int castleType = moveMeta.shortCastle ? 1 : 2;
            if (castleResult == castleType) return br.piece(king.currentSquare());
            return null;
        }

        // can I take down any enemy EnPasser?
        if (type == Piece.PAWN) {
            EnPasser enemyEnPasser = br.restoreEnPasser(color);
            if (enemyEnPasser != null && enemyEnPasser.intermediateSquare.equals(moveMeta.destSquare)) {
                moveMeta.enPasserTaker = EnPassant.whoIsEnPasserTaker(enemyEnPasser.taker, moveMeta);
                if (moveMeta.enPasserTaker == null) return null;
                return br.piece(moveMeta.enPasserTaker);
            }
        }

        return Arbiter.getPiece(moveMeta, br);
    }

}
