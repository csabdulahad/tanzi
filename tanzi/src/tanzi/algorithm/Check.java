package tanzi.algorithm;

import tanzi.model.EnPasser;
import tanzi.model.Piece;
import tanzi.staff.BoardRegistry;
import tanzi.staff.BufferedBR;

import java.util.ArrayList;

public abstract class Check {

    /**
     * haveIGivenCheck method assumes that the move was valid to play by all chess rules and the BR is
     * reflecting the move. so with this new algorithm, we don't need to make any changes to BR
     * within algorithm thus fewer possibilities to have bugs and less code.
     * <p>
     * here the color is of the army who played the move.
     */
    public static boolean didICheck(int color, BufferedBR boardRegistry) {
        String oppositeKingSquare = boardRegistry.getEnemyKingSquare(color);

        ArrayList<String> attacker = Arbiter.whoCanGo(oppositeKingSquare, color, false, boardRegistry);
        if (attacker.size() > 0) return true;

        ArrayList<String> knightOnOppositeKing = GeometryEngineer.possibleKnightTo(oppositeKingSquare, color, boardRegistry);
        return knightOnOppositeKing.size() > 0;
    }

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
    public static boolean isMate(String attackerSquare, BufferedBR br) {

        Piece attackerPiece = br.piece(attackerSquare);

        int attackerColor = attackerPiece.color;
        int inCheckColor = Piece.getOppositeColor(attackerColor);

        String inCheckKingSquare = br.getEnemyKingSquare(attackerColor);

        // checking king's valid squares according to the boardRegistry after the opponent move
        ArrayList<String> inCheckValidKingSquareList = GeometryEngineer.validSquare(Piece.KING, inCheckColor, inCheckKingSquare, true, br);


        // here safeFound means whether we have actually found the king safe by the following
        // types of ways a king can be checked for checkmate. if we find the king safe in any
        // type of ways then we don't check any further by setting furtherChecks to false

        // check for any of valid squares the checking king can go or make move
        for (String square : inCheckValidKingSquareList) {
            boolean canGo = Arbiter.pieceCanGo(inCheckKingSquare, square, false, br);
            if (canGo) return false;
        }

        // king has no valid square to move and also the king can't get out check by taking enemy
        // if there is any; so now let's see whether the attacking piece can be taken

        /*
         * here we are passing arguments of enemy to find out whether the enemy is safe or not
         * if enemy is not safe that means enemy can be taken
         */

        // enemy can be taken. let's say we have taken the enemy but are we out of check?

        boolean kingSafe = false;
        boolean enemySafe = Arbiter.amISafe(attackerColor, attackerSquare, br);
        if (!enemySafe) {
            // found a piece from our army who can take down this enemy; but we need to make
            // sure that after taking down the enemy piece the king is not in check anymore.
            ArrayList<String> sacrificerSquareList = Arbiter.whoCanGo(attackerSquare, inCheckColor, true, br);
            kingSafe = Sacrifice.isKingSafeBySac(attackerSquare, sacrificerSquareList, br);
        }
        if (kingSafe) return false;

        // check for discover check leading to checkmate situation
        ArrayList<String> discoverAttackers = Arbiter.whoCanGo(inCheckKingSquare, attackerColor, false, br);
        if (discoverAttackers.size() == 1) {

            // for discover attacker we need to see whether we can kill that discoverer,
            // or we can put any piece in-between. and after doing this checks, we will also
            // need to check if our king is safe or not. if safe, then it is not checkmate.
            String discoverer = discoverAttackers.get(0);

            // try to kill the discoverer
            ArrayList<String> enemyKillerSquares = Arbiter.whoCanGo(discoverer, inCheckColor, true, br);
            if (Sacrifice.isKingSafeBySac(discoverer, enemyKillerSquares, br))
                return false;

            // at this point we know that we can't kill the discoverer. so we have one last option
            // here to check for. can we put any piece in-between the discoverer and the king?
            // let's try to do that and see whether we can or not.
            if (Sacrifice.sacrifice(inCheckKingSquare, discoverer, br))
                return false;

        }

        // let's see whether the enemy can be taken by en-passant rule if there is any
        EnPasser enPasser = br.restoreEnPasser(inCheckColor);
        if (attackerPiece.isPawn() && enPasser != null && enPasser.nowSquare.equals(attackerSquare)) {
            BufferedBR bufferedBR = br.copy();

            // first delete the en-passer from the now square & put it on the immediate square
            bufferedBR.movePiece(enPasser.nowSquare, enPasser.intermediateSquare);

            boolean enPasserSafe = Arbiter.amISafe(attackerColor, enPasser.intermediateSquare, bufferedBR);
            bufferedBR.recycle();

            if (!enPasserSafe) return false;
        }

        boolean sacrifice = Sacrifice.sacrifice(inCheckKingSquare, attackerSquare, br);
        return !sacrifice;
    }

    /**
     * This method first makes the changes to the BR to reflect that the move has been played. Because
     * the check and checkmate algorithms assume that the move has already been played to start the
     * algorithms.
     * <p>
     * It returns "#" or "+" as mate annotation based on the output from isCheck and isCheckMate algorithms.
     */
    public static String mateStatusAfterPromo(Piece srcPiece, String destSquare, int promotionType, BoardRegistry br) {

        BufferedBR bufferedBR = br.copy();

        // get the moving piece from the buffered BR and make the move to the dest square
        Piece piece = bufferedBR.movePiece(srcPiece.currentSquare(), destSquare);

        // set promotion type
        if (promotionType != -1) piece.type = promotionType;

        // check for check and checkmate status
        String annotation = mateStatus(piece.color, destSquare, bufferedBR);
        bufferedBR.recycle();
        return annotation;
    }

    /**
     * It returns "#" or "+" based on the output of Check.didIGiveCheck() and Checkmate.isMate() methods for any move
     * made by an army towards the enemy king.
     * <p>
     * Unlike all other algorithms, it doesn't use BoardRegistry. It uses BufferBR because it has to be through the
     * various algorithms which may alter different pieces on the BR in order to calculate the mate status thus it
     * makes it safe for the main in-memory game's BR.
     */
    public static String mateStatus(int checkerColor, String square, BufferedBR bbr) {
        // now check whether I have given check to enemy
        boolean checkMate = false;

        boolean check = didICheck(checkerColor, bbr);
        if (check) checkMate = isMate(square, bbr);

        if (checkMate) return "#";
        return check ? "+" : "";
    }

}
