package tanzi.algorithm;

import tanzi.model.OctalSquare;
import tanzi.model.Piece;
import tanzi.model.Square;
import tanzi.staff.Arbiter;
import tanzi.staff.BoardRegistry;
import tanzi.staff.BufferedBR;
import tanzi.staff.GeometryEngineer;

import java.util.ArrayList;

public class Sacrifice {

    // this method can estimate whether a king can be saved by his army altogether
    public static boolean sacrifice(String attackedSquare, String attackerSquare, Arbiter ar, BoardRegistry br) {
        // get the attacked and attacker pieces
        Piece attacked = br.getPiece(attackedSquare);
        Piece attacker = br.getPiece(attackerSquare);

        // get the OS of the attacker
        OctalSquare attackerOctalSquareList = new OctalSquare(attacker.getCurrentSquare());

        // squares between the attacker and attacked piece
        ArrayList<String> filteredKAS = attackerOctalSquareList.getFilteredKAS(attacked.getCurrentSquare());
        if (filteredKAS == null) return false;

        if (!canPutPieceInBetween(attackedSquare, attackerSquare)) return false;

        for (String square : filteredKAS) {
            // first verify that the in-between square is empty
            if (br.anyPieceOn(square)) continue;

            // find whether a knight can become a hero
            int numOfKnight = GeometryEngineer.possibleKnightTo(square, attacked.color, br).size();
            if (numOfKnight > 0) {
                ArrayList<String> knights = ar.whoCanGo(square, attacked.color, true, br);
                if (Sacrifice.isKingSafeBySac(attackedSquare, knights, ar, br)) return true;
            }

            // check whether any pawn can be sacrificed

            // calculate ranks for two-fashioned pawn move for both colors
            int rankInc1 = attacked.isBlack() ? -1 : 1;
            int rankInc2 = attacked.isBlack() ? -2 : 2;

            boolean sacrifice = pawnSacrifice(attacked.getCurrentSquare(), square, attacker.getCurrentSquare(), rankInc1, attacked.color, ar, br);
            sacrifice = sacrifice || pawnSacrifice(attacked.getCurrentSquare(), attacker.getCurrentSquare(), square, rankInc2, attacked.color, ar, br);
            if (sacrifice) break;

            // find whether other types of pieces can blockade

            /*
             * now see whether that square can be taken by any of under Attacked army; if so then it
             * means that we found a piece who is ready to sacrifice for its king
             */

            ArrayList<String> sacrificeListToSquare = ar.whoCanGo(square, attacked.color, true, br);
            boolean sac = Sacrifice.isKingSafeBySac(square, sacrificeListToSquare, ar, br);
            if (sac) return true;
        }

        return false;
    }

    // here precious square is where the KING is, sacrifice square is that square which can hide the
    // king from the attacker. this method can figure out whether any pawn of his own army can save
    // the king from the attacker. the saver is checked against Pin.
    public static boolean pawnSacrifice(String preciousSquare, String sacrificeSquare, String attackFrom, int rankInc, int color, Arbiter ar, BoardRegistry br) {
        // let's calculate the hero, the sacrifice who will save the day, hopefully!
        String heroSquare = GeometryEngineer.getSquareAt(sacrificeSquare, 0, rankInc);
        if (heroSquare == null) return false;

        /*
         * a pawn can move in ways when it is at home. so find the piece on the in between square of
         * precious & attacker squares for either of pawn moves of hero as defined by rankInc
         * */
        Piece hero = br.getPiece(heroSquare);
        if (hero == null) return false;

        // the piece we found can't be eligible for sacrifice
        if (hero.color != color || !hero.isPawn()) return false;

        // figure out whether the hero can make the move regarding pin
        if (!ar.pieceCanGo(heroSquare, sacrificeSquare, true, br))
            return false;

        // get the buffered BR for safety
        BufferedBR bufferedBR = br.getCopy();

        // we found a saver, let's sacrifice it
        Piece sacrifice = bufferedBR.getPiece(heroSquare);
        sacrifice.setCurrentSquare(sacrificeSquare);

        // let's see if attacker still attacks the precious square
        Piece attacker = bufferedBR.getPiece(attackFrom);
        boolean result = ar.pieceCanGo(attacker.getCurrentSquare(), preciousSquare, false, bufferedBR);

        bufferedBR.recycle();
        return result;
    }

    // for sacrificing, we need to find out whether there is a square in between the attacker
    // and the under-attacked piece. if so then we know for sure that we can sacrifice a piece.
    // this method tries to find out the difference between two square a by both file & rank and
    // tries to see whether the difference is greater than 1. if so then there is at least one
    // in-between square where a sacrifice can be done.
    private static boolean canPutPieceInBetween(String squareA, String squareB) {
        // first calculate to see whether there is one square in-between by rank AND file

        boolean oneSquareMinimum = Math.abs(Square.getRankAsInt(squareA) - Square.getRankAsInt(squareB)) > 1;
        if (!oneSquareMinimum)
            oneSquareMinimum = Math.abs(Square.getFileAsChar(squareA) - Square.getFileAsChar(squareB)) > 1;

        return oneSquareMinimum;
    }

    // this method can calculate for given army, which pieces from it can go to the focus square
    // regarding pin status and save its king. it first lists the squares for the pieces who can
    // go and the tries to see which piece saves the king.
    public static boolean isKingSafeBySac(String sacrificeSquare, ArrayList<String> sacrificerSquareList, Arbiter ar, BoardRegistry br) {
        if (sacrificerSquareList == null || sacrificerSquareList.size() < 1) return false;

        // the king square of the sacrifice
        String kingSquare = null;
        int sacrificeArmyColor = -1;

        // first get the color and the king square for the sacrifice arsenal
        for (String square : sacrificerSquareList) {
            Piece piece = br.getPiece(square);
            if (piece == null) continue;
            sacrificeArmyColor = piece.color;
            kingSquare = br.getKingSquare(sacrificeArmyColor);
        }
        if (kingSquare == null) return false;

        boolean sacrifice = false;
        for (String square : sacrificerSquareList) {
            BufferedBR bufferedBR = br.getCopy();

            // delete enemy piece if any
            bufferedBR.deleteEntry(sacrificeSquare);

            // sacrifice the piece
            bufferedBR.movePiece(square, sacrificeSquare);

            // now let's see whether we have found a real sacrifice
            if (Arbiter.amISafe(sacrificeArmyColor, kingSquare, ar, bufferedBR)) {
                sacrifice = true;
                bufferedBR.recycle();
                break;
            }
            bufferedBR.recycle();
        }

        return sacrifice;
    }

}
