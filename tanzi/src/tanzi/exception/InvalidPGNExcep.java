package tanzi.exception;

public class InvalidPGNExcep extends Exception {

    public InvalidPGNExcep(String move) {
        super("Invalid PGN at move " + move);
    }

}
