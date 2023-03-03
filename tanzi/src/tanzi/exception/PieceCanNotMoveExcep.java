package tanzi.exception;

import tanzi.model.Piece;

public class PieceCanNotMoveExcep extends Exception {

    public PieceCanNotMoveExcep(Piece piece, String srcSquare, String destSquare) {
        super(String.format("Piece %s of %s is at %s can't go to %s", Piece.getFullName(piece.type), Piece.getColorName(piece.color), srcSquare, destSquare));
    }

}
