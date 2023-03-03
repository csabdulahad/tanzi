package tanzi.exception;

public class NoEnPasserTakerExcep extends Exception {

    public NoEnPasserTakerExcep(String intermediateSquare) {
        super(String.format("No taker found for intermediate en-passer %s", intermediateSquare));
    }
}
