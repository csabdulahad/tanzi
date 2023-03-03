package tanzi.algorithm;

import tanzi.model.EnPasser;
import tanzi.model.MoveMeta;
import tanzi.model.Piece;
import tanzi.model.Square;
import tanzi.staff.BoardRegistry;
import tanzi.staff.GeometryEngineer;

import java.util.ArrayList;

public abstract class EnPassant {

    /*
     * EnPassant Algorithm
     *
     * 1. first we get the piece, and we make sure it is a pawn on the 2nd or 7th rank based on its
     *    color
     * 2. then we calculate whether the passing pawn is really passing around the enemy pawns
     * 3. if yes, then we add that enemy piece to be able to capture the passing pawn
     * 4. we return the enemy pawn squares with the passing pawn square at the end of an array to
     *    be able to recognize which pawn made the EnPassant valid.
     *    [passantTaker, passantTaker, enPassant]
     * 5. we return null, it the passing pawn doesn't qualify as a passing pawn.
     * */
    public static String[] getEnPasserTaker(String srcSquare, String destSquare, BoardRegistry br) {

        // define the passing piece and its properties
        Piece piece = br.getPiece(srcSquare);
        int color = piece.color;
        int oppositeColor = Piece.getOppositeColor(color);
        int rank = piece.getRank();

        // make sure it is a pawn
        if (!piece.isPawn()) return null;

        // for white the pawn rank is at 2 and for black it is at 7
        int step;
        if (piece.isWhite() && rank == 2)
            step = 2;
        else if (piece.isBlack() && rank == 7)
            step = -2;
        else
            return null;

        // check whether the passing pawn is moving two square by the move from home
        int destRank = Character.getNumericValue(destSquare.charAt(1));
        if (Math.abs(destRank - rank) != 2) return null;

        // calculate two possible enemy side pawns of the passing pawn
        String diagonalSquare1 = GeometryEngineer.getSquareAt(srcSquare, 1, step);
        String diagonalSquare2 = GeometryEngineer.getSquareAt(srcSquare, -1, step);

        // check whether those side pieces are pawns of enemy and eligible to capture the passer
        diagonalSquare1 = checkForSidePawn(diagonalSquare1, oppositeColor, br);
        diagonalSquare2 = checkForSidePawn(diagonalSquare2, oppositeColor, br);

        // add side pawns who can into the list
        ArrayList<String> squares = new ArrayList<>();
        if (diagonalSquare1 != null) squares.add(diagonalSquare1);
        if (diagonalSquare2 != null) squares.add(diagonalSquare2);

        // if there is no side pawns, then return null otherwise return the list as array
        return squares.size() > 0 ? squares.toArray(new String[0]) : null;
    }

    /*
     * for a pawn move, this method can tell whether the move is enPassant or not. if it is enPassant
     * move then it return an EnPasser object which holds the info about the enPassant.
     *
     * whenever you get an EnPasser object, you must let the arbiter know that an enPassant move has
     * just been made.
     * */
    public static EnPasser amIEnpasser(String srcSquare, String destSquare, BoardRegistry br) {

        // get the en-passer taker for this passant move
        String[] passerTaker = getEnPasserTaker(srcSquare, destSquare, br);
        if (passerTaker == null) return null;

        // create EnPasser object to hold info about this enPassant
        EnPasser enPasser = new EnPasser();

        // set properties accordingly
        enPasser.taker = passerTaker;
        enPasser.beforeSquare = srcSquare;
        enPasser.nowSquare = destSquare;
        enPasser.takerColor = Piece.getOppositeColor(br.getPiece(srcSquare).color);

        // calculate the intermediate square
        int min = Math.min(Character.getNumericValue(srcSquare.charAt(1)), Character.getNumericValue(destSquare.charAt(1)));
        enPasser.intermediateSquare = srcSquare.charAt(0) + "" + (min + 1);

        return enPasser;
    }

    /*
     * this method tries to see whether the passed move meta can be one of the taker for the
     * enemy en-passer.
     * */
    public static String whoIsEnPasserTaker(String[] candidateTakerList, MoveMeta moveMeta) {
        if (candidateTakerList.length == 1) return candidateTakerList[0];
        for (String taker : candidateTakerList) {
            if (Square.getFileAsChar(taker) == Square.getFileAsChar(moveMeta.move)) return taker;
        }
        return null;
    }

    // for a given square this method can tell if a square is found in an en-passer object
    public static boolean amIEnpasserTaker(String mySquare, EnPasser enPasser) {
        if (enPasser == null) return false;
        for (String square : enPasser.taker)
            if (square.equals(mySquare)) return true;
        return false;
    }

    // for a destination square of pawn and en-passer object, this method can tell if
    // the pawn is really going to take the en-passer or not.
    public static boolean amITakingEnPasser(String myDestSquare, EnPasser enPasser) {
        String enPasserFile = Square.getFileAsString(enPasser.intermediateSquare);
        String myFile = Square.getFileAsString(myDestSquare);
        return myFile.equals(enPasserFile);
    }

    /*
     * for a square, this method validates whether the piece on the square should be able to capture
     * the passing pawn.
     * */
    private static String checkForSidePawn(String square, int oppositeColor, BoardRegistry boardRegistry) {
        if (square == null) return null;
        Piece sidePiece = boardRegistry.getPiece(square);
        if (sidePiece == null) return null;
        if (!sidePiece.isPawn()) return null;
        if (sidePiece.color != oppositeColor) return null;
        return square;
    }

}
