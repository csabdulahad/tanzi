package tanzi.algorithm;

import tanzi.model.Piece;
import tanzi.staff.Arbiter;
import tanzi.staff.BoardRegistry;
import tanzi.staff.GeometryEngineer;

import java.util.ArrayList;

public class StaleMate {
    public static boolean isStaleMate(int color, Arbiter ar, BoardRegistry br) {

        // get the squares of the army for which we are checking whether it is stalemate or not
        ArrayList<String> squaresOfArmy = br.getSquareListForArmyOf(color);

        for (String square : squaresOfArmy) {
            Piece piece = Piece.clone(br.getPiece(square));

            ArrayList<String> pieceSquares = GeometryEngineer.validSquare(piece.type, piece.color, piece.getCurrentSquare(), true, br);
            for (String destSquare : pieceSquares) {
                if (ar.pieceCanGo(piece.getCurrentSquare(), destSquare, true, br))
                    return false;
            }
        }
        return true;
    }
}
