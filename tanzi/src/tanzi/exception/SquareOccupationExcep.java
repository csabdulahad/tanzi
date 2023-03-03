package tanzi.exception;

public class SquareOccupationExcep extends Exception {

    public SquareOccupationExcep(String square) {
        super(String.format("Square %s must be empty", square));
    }

    public SquareOccupationExcep() {
        super("The square was not found unoccupied");
    }

}
