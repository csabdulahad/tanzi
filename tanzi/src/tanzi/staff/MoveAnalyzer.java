package tanzi.staff;

import tanzi.model.MoveMeta;
import tanzi.model.Piece;

import java.util.ArrayList;

/*
 * this class can analyze moves written in PGN format and returns MoveMeta objects after analysis as
 * result. with such a move meta, the app can really understand which pieces are involved in the move
 * and how the game proceeds.
 *
 * calls to each analysis method must maintain the following order, otherwise the outcome
 * would be incomplete or incorrect.
 *
 * ORDER OF METHOD CALLS : takeMove > castleMove > promotionMove > uniqueMove > simpleMove
 * */

public class MoveAnalyzer {

    private MoveAnalyzer() {

    }

    public static MoveMeta analyze(MoveRepo moveRepo) {
        String move = moveRepo.nextMove();

        MoveMeta moveMeta = analyze(move);
        moveMeta.color = moveRepo.whoseTurn();
        moveMeta.moveIndexInPGN = moveRepo.currentIndex();

        return moveMeta;
    }

    public static MoveMeta analyze(String move) {
        MoveMeta moveMeta = new MoveMeta(move.trim());

        takeMove(moveMeta);
        castleMove(moveMeta);
        promotionMove(moveMeta);
        unique(moveMeta);
        simpleMove(moveMeta);

        return moveMeta;
    }

    public static ArrayList<MoveMeta> analyze(String[] moves) {
        ArrayList<MoveMeta> moveMetaArrayList = new ArrayList<>();
        for (String move : moves) moveMetaArrayList.add(analyze(move));
        return moveMetaArrayList;
    }

    // verify whether a given move is simple move such as e4, g4, Nf3, Bb5
    private static void simpleMove(MoveMeta moveMeta) {

        // get which type of piece it is moving
        moveMeta.type = Piece.getType(moveMeta.normalizedMove.charAt(0));

        if (moveMeta.takes || moveMeta.castle || moveMeta.promotion || moveMeta.uniqueName)
            return;

        moveMeta.simpleMove = true;

        // check whether it is just a 2 digit simple pawn move
        if (moveMeta.normalizedMove.length() == 2) {
            moveMeta.destSquare = moveMeta.normalizedMove;
            return;
        }

        // calculate how many O in the move, to avoid trusting on blind length counting
        int oLen = moveMeta.normalizedMove.split("-").length;

        // check whether it is just a 3 digit piece move, not an O-O-O castle move lurking in the move
        if (moveMeta.normalizedMove.length() == 3 && oLen == 1)
            moveMeta.destSquare = moveMeta.normalizedMove.substring(1);

    }

    // check if a given move involves in taking other piece such as exd5, Nxg7, Kxe3, Nf3xf3=Q+
    private static void takeMove(MoveMeta moveMeta) {
        // check whether it is a take move
        int xPos = moveMeta.normalizedMove.indexOf("x");
        if (xPos != -1) {
            moveMeta.destSquare = moveMeta.normalizedMove.substring(xPos + 1, xPos + 3);
            moveMeta.takes = true;
        }
    }

    // verify whether a given move is a castle move. cases are O-O(short castle) & O-O-O(long castle)
    private static void castleMove(MoveMeta moveMeta) {
        // calculate how many O in the move to figure castle type
        int oLen = moveMeta.normalizedMove.split("-").length;

        // check whether it is castling move
        if (oLen >= 2) {
            if (oLen == 2) moveMeta.shortCastle = true; // O-O
            if (oLen == 3) moveMeta.longCastle = true;  // O-O-O
            moveMeta.destSquare = null;
            moveMeta.castle = true;
        }
    }

    // detect a move is a promotion move or not such as exd8=Q+, g8=R#
    private static void promotionMove(MoveMeta moveMeta) {
        // find whether it is a promotion move
        int equalPos = moveMeta.normalizedMove.indexOf("=");
        if (equalPos != -1) {
            moveMeta.promotion = true;
            moveMeta.promoteType = Piece.getType(moveMeta.normalizedMove.charAt(equalPos + 1));
            // let's see if destSquare has been already calculated in take validation phase
            if (!moveMeta.takes)
                moveMeta.destSquare = moveMeta.normalizedMove.substring(equalPos - 2, equalPos);
        }
    }

    // try to detect if a given move has unique piece name such Ncxd6, R7e7, Rc3xf3=N#
    private static void unique(MoveMeta moveMeta) {

        String move = moveMeta.normalizedMove;

        // test whether there is anything unique about the move
        if (move.charAt(1) == 'x' || moveMeta.castle || move.length() < 4) return;

        // usually it is after the piece name such Nc3 here we're talking about the "c"
        char uChar = move.charAt(1);

        // get the index for dest square based on whether "x" is there or not in the move
        int destSquareIndex = (!moveMeta.takes) ? 2 : 3;

        if (!Character.isDigit(uChar)) {
            /*
             * it could be start of unique square name see what is next char, whether it is a digit;
             * if so then it is unique square name.
             */
            char u2Char = move.charAt(2);
            if (Character.isDigit(u2Char)) {
                // it is a fully expressed unique name
                moveMeta.uniqueSquareName = uChar + String.valueOf(u2Char);
                moveMeta.uniqueSquare = true;

                // update the dest square index accordingly
                destSquareIndex = (!moveMeta.takes) ? 3 : 4;
            } else {
                // unique file name
                moveMeta.uniqueFileName = uChar;
                moveMeta.uniqueFile = true;
            }
        } else {
            // unique rank name
            moveMeta.uniqueRankName = Character.getNumericValue(uChar);
            moveMeta.uniqueRank = true;
        }

        if (moveMeta.destSquare == null)
            moveMeta.destSquare = move.substring(destSquareIndex, destSquareIndex + 2);

        moveMeta.uniqueName = true;
    }

}
