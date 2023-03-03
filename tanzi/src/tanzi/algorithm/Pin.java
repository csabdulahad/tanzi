package tanzi.algorithm;

import tanzi.model.Piece;
import tanzi.staff.Arbiter;
import tanzi.staff.BoardRegistry;
import tanzi.staff.BufferedBR;

import java.util.ArrayList;

public abstract class Pin {

    public static boolean isPinned(String from, String to, Arbiter ar, BoardRegistry br) {

        /*
         * PIN DETECTION ALGORITHM USING BOARD-REGISTRY
         *
         * 1. make that move in the board registry (which requires deletion followed by
         *    addition of the piece into the board registry) & also delete any opposite piece as it
         *    has to be a taken move
         * 2. now see the move from the king's prospective to find whether any piece of
         *    enemy can reach the king's square. so get the list of squares that are aligned with
         *    the king's square from where possibly an attack can come from
         * 3. with the squares aligned with the king, try to see if any piece from enemy can reach
         *    the king's square. if so, then it must be a pin thus the move can't be played
         * 4. if it is pin then we return null for the list of valid moves for the piece
         * */

        // get a buffered copy of the BR
        BufferedBR bufferedBR = br.getCopy();

        // find the king's position of color of the piece on the srcSquare
        Piece srcPiece = bufferedBR.getPiece(from);
        Piece kingPiece = bufferedBR.getPiece(Piece.PIECE_KING, srcPiece.color);

        // step 1 - first make that destination move in the board registry TEMPORARILY
        bufferedBR.deleteEntry(to);
        bufferedBR.deleteEntry(srcPiece.getCurrentSquare());
        srcPiece.setCurrentSquare(to);
        bufferedBR.addEntry(srcPiece);

        /*
         * now see from the king's prospective whether any piece of enemy can reach the king's square
         *
         * get the list of squares that are aligned with the king's square where possibly an attack
         * can come from
         * */

        // step 2, 3
        ArrayList<String> squaresAlignedWithKing = bufferedBR.getOSSquare(srcPiece.color);

        /*
         * with the squares that are aligned with the king's square, try to see ANY PIECE OF ENEMY
         * can reach the king's square. if so, then it must be a pin thus the move can't be played
         * */

        boolean pin = false;
        for (String alignedSquare : squaresAlignedWithKing) {
            Piece piece = bufferedBR.getPiece(alignedSquare);

            // empty square where there is no piece so ignore it but go ahead
            if (piece == null) continue;

            // it is from own army then it is never going to attack so ignore it too
            if (piece.color == srcPiece.color) continue;

            // got an enemy piece, let's see whether it can attack the opposite king
            if (ar.pieceCanGo(piece.getCurrentSquare(), kingPiece.getCurrentSquare(), false, bufferedBR)) {
                pin = true;
                break;
            }
        }

        // recycle the Buffered BR
        bufferedBR.recycle();

        // if it is pin then we return null for the list of valid moves for the piece
        return pin;
    }

}
