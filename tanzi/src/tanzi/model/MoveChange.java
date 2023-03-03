package tanzi.model;

/*
* Each PGN move is represented as MoveChange object in the MoveHistory stack. Using this
* class, MoveHistory can alter the changes in the Board Registry.
* */

public class MoveChange {

    // hold the en-passer which was available to capture on making this move change
    public EnPasser enPasser;

    // this square tells where the changed piece on; so that we can delete the piece with previous piece
    public String newSquare;

    // the piece before moving to new square or for taken piece, how it was before taken by
    public Piece previousPiece;

    // represent whether the change is single or binary change;
    // binary changes happens on when it is castle move or capture move etc.
    public boolean tail;

    private MoveChange(String newSquare, Piece previousPiece) {
        this.newSquare = newSquare;
        this.previousPiece = previousPiece;
    }

    public static MoveChange createChange(String newSquare, Piece movingPiece) {
        // clone the piece before it moves to capture where it was before moving
        Piece previousPiece = Piece.clone(movingPiece);

        // using opposite-animation technique
        // previousPiece.setCurrentSquare(newSquare);

        return new MoveChange(newSquare, previousPiece);
    }

    @Override
    public String toString() {
        return newSquare + " from " + previousPiece.getCurrentSquare() + ", which has tail " + tail;
    }

}
