package tanzi.algorithm;

import tanzi.model.EnPasser;
import tanzi.model.Piece;
import tanzi.staff.Arbiter;
import tanzi.staff.BoardRegistry;
import tanzi.staff.BufferedBR;
import tanzi.staff.GeometryEngineer;

import java.util.ArrayList;

public abstract class Checkmate {

    /*
     * like check algorithm, this method also assumes that the move was valid to play by all the
     * chess rules and the move is reflected in the BR. this way we don't modify the BR and less code
     * is required to perform the calculation.
     *
     * here the color is of the army who played the move.
     * */
    /*
     * CHECKMATE Algorithm
     *
     * 1. get the current board registry.
     * 2. get opposite king's color & position.
     * 3. now generate valid king squares for opposite checking king.
     * 4. for each king's square get it checked by the arbiter whether the checking king can go
     *    or make move to the square.
     * 6. if for any of king's squares, king can go or make move that means the king is in check
     *    but not checkmated. return negative value; REMEMBER to revert changes to the BR.
     * */
    public static boolean isMate(String attackerSquare, Arbiter ar, BoardRegistry br) {

        Piece attackerPiece = br.getPiece(attackerSquare);

        int attackerColor = attackerPiece.color;
        int inCheckColor = Piece.getOppositeColor(attackerColor);

        String inCheckKingSquare = br.getOppositeKingSquare(attackerColor);

        // checking king's valid squares according to the boardRegistry after the opponent move
        ArrayList<String> inCheckValidKingSquareList = GeometryEngineer.validSquare(Piece.PIECE_KING, inCheckColor, inCheckKingSquare, true, br);


        // here safeFound means whether we have actually found the king safe by the following
        // types of ways a king can be checked for checkmate. if we find the king safe in any
        // type of ways then we don't check any further by setting furtherChecks to false

        // check for any of valid squares the checking king can go or make move
        for (String square : inCheckValidKingSquareList) {
            boolean canGo = ar.pieceCanGo(inCheckKingSquare, square, false, br);
            if (canGo) return false;
        }

        // king has no valid square to move and also the king can't get out check by taking enemy
        // if there any; so now let's see whether the attacking piece can be taken

        /*
         * here we are passing arguments of enemy to find out whether the enemy is safe or not
         * if enemy is not safe that means enemy can be taken
         */

        // enemy can be taken. let's say we have taken the enemy but are we out of check?

        boolean kingSafe = false;
        boolean enemySafe = Arbiter.amISafe(attackerColor, attackerSquare, ar, br);
        if (!enemySafe) {
            // found a piece from our army who can take down this enemy; but we need to make
            // sure that after taking down the enemy piece the king is not in check any more.
            ArrayList<String> sacrificerSquareList = ar.whoCanGo(attackerSquare, inCheckColor, true, br);
            kingSafe = Sacrifice.isKingSafeBySac(attackerSquare, sacrificerSquareList, ar, br);
        }
        if (kingSafe) return false;

        // check for discover check leading to checkmate situation
        ArrayList<String> discoverAttackers = ar.whoCanGo(inCheckKingSquare, attackerColor, false, br);
        if (discoverAttackers.size() == 1) {

            // for discover attacker we need to see whether we can kill that discoverer,
            // or we can put any piece in-between. and after doing this checks, we will also
            // need to check if our king is safe or not. if safe, then it is not checkmate.
            String discoverer = discoverAttackers.get(0);

            // try to kill the discoverer
            ArrayList<String> enemyKillerSquares = ar.whoCanGo(discoverer, inCheckColor, true, br);
            if (Sacrifice.isKingSafeBySac(discoverer, enemyKillerSquares, ar, br))
                return false;

            // at this point we know that we can't kill the discoverer. so we have one last option
            // here to check for. can we put any piece in-between the discoverer and the king?
            // let's try to do that and see whether we can or not.
            if (Sacrifice.sacrifice(inCheckKingSquare, discoverer, ar, br))
                return false;

        }

        // let's see whether the enemy can be taken by en-passant rule if there is any
        EnPasser enPasser = ar.getEnPasserFor(inCheckColor);
        if (attackerPiece.isPawn() && enPasser != null && enPasser.nowSquare.equals(attackerSquare)) {
            BufferedBR bufferedBR = br.getCopy();

            // first delete the en-passer from the now square & put it on the immediate square
            bufferedBR.movePiece(enPasser.nowSquare, enPasser.intermediateSquare);

            boolean enPasserSafe = Arbiter.amISafe(attackerColor, enPasser.intermediateSquare, ar, bufferedBR);
            bufferedBR.recycle();

            if (!enPasserSafe) return false;
        }

        boolean sacrifice = Sacrifice.sacrifice(inCheckKingSquare, attackerSquare, ar, br);
        return !sacrifice;
    }

}
