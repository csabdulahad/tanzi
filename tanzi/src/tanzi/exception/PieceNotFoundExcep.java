package tanzi.exception;

public class PieceNotFoundExcep extends Exception {

    public PieceNotFoundExcep(String square) {
        super("No piece found on " + square);
    }

    public PieceNotFoundExcep() {
        super("The piece is not found on required square");
    }

}
