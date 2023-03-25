package tanzi.model;

import tanzi.staff.BRHistorian;
import tanzi.algorithm.PGN;
import tanzi.staff.BoardRegistry;

/**
 * This class definition captures the changes to the BR with the index of the move from the repository so that
 * we can easily calculate or navigate through the changes we make to the BR.
 * <p>
 * The BRChange object describes exactly which pieces were involved during the change. A change usually consists
 * of removing a piece from the BR and putting another piece on that removed piece's square or a castle move etc.
 */

public class BRHistory {

    /**
     * The move index this BRHistory is associated with
     */
    int moveIndex;

    BRChange undo, redo;

    public BRHistory(int moveIndex, BRChange undo, BRChange redo) {
        this.moveIndex = moveIndex;
        this.undo = undo;
        this.redo = redo;
    }

    /**
     * The history can be undone using this method. It internally calls
     * the BRHistory::reflectChange() method with undo object to properly
     * write and reflects all the associated changes with this history to the BR.
     */
    public void undo(BoardRegistry br) {
        reflectChange(undo, br);
    }

    /**
     * This method redoes the history into the specified BR. This internally
     * calls the BRHistory:reflectChange() with redo object as argument. All
     * the management with this redo such as clearing en-passer, updating
     * king OS squares etc. are carried out by reflectChange() method properly.
     */
    public void redo(BoardRegistry br) {
        reflectChange(redo, br);
    }

    public int moveIndex() {
        return moveIndex;
    }

    public BRChange redoObj() {
        return redo;
    }

    public BRChange undoObj() {
        return undo;
    }

    /**
     * The history change can be reflected into the specified BR. This method
     * either undo/redo a history change as specified by the argument into the BR.
     * It firstly reflects the change object, updates the king OS squares, and clears
     * en-passer object for the color who this history for.
     */
    private void reflectChange(BRChange change, BoardRegistry br) {
        BRHistory.changeObjToBR(change, br);
        BRHistory.updateKingOSSquare(change, br);
        br.clearEnPasserFor(PGN.indexToColor(moveIndex));
    }

    /**
     * For a given change object type, it modifies the BR accordingly to
     * reflect the changes in the BR for that change object.
     */
    private static void changeObjToBR(BRChange brChange, BoardRegistry br) {
        for (String s : brChange.squares) br.delete(s);
        for (Piece p : brChange.pieces) br.add(p);

        // also set any en-passer to the br associated with the change
        br.storeEnPasser(brChange.enPasser);
    }

    /**
     * When the history is undone/redone then if that operation has a king move
     * involved then this updates the king's OS square.
     * <p>
     * The king is identified as it appears in the change object. Because the king
     * can only be included in the change object if it makes move. It is assumed
     * on the fact that the king can't be captured by any piece, there will never
     * be two king in a change object. Thus, after undo/redo operation, this method
     * updates OS square of the correct king.
     */
    private static void updateKingOSSquare(BRChange change, BoardRegistry br) {
        for (Piece p : change.pieces) {
            if (p.type == Piece.KING) {
                br.updateOSSquare(p.color, p.currentSquare());
                break;
            }
        }
    }

    /**
     * A history can be executed into the specified BR and be saved into the specified
     * BRHistorian at all together. Here execution uses the redo object of the history
     * change and does it to the BR.
     */
    public void saveAndExecute(BRHistorian brHistorian, BoardRegistry br) {
        if (brHistorian != null)
            brHistorian.saveChange(this);
        redo(br);
    }

    /**
     * This is a builder class for creating BRHistory objects to capture changes to the BR.
     * Primarily, the MoveMaker class uses this builder to record the changes it makes to the BR.
     **/
    public static class Builder {
        int moveIndex;
        BRChange undo, redo;

        private Builder(int moveIndex) {
            this.moveIndex = moveIndex;
            undo = new BRChange();
            redo = new BRChange();
        }

        public Piece singleRedoPiece() {
            if (redo == null) return null;
            if (redo.pieces.size() != 1) return null;
            return redo.pieces.get(0);
        }

        /**
         * The index of the move within the repository who this BRHistory object for.
         */
        public static Builder forMove(int moveIndex) {
            return new Builder(moveIndex);
        }

        /**
         * During a change, there are some pieces which needs to be removed in order to make that change undo.
         * Squares that are passed in as arguments must be removed in the undoing this change.
         */
        public Builder squareToUndo(String... square) {
            for (String x : square) undo.square(x);
            return this;
        }

        /**
         * After deleting pieces from the listed square on undoing a changes, there must be some pieces which
         * need to be put back on the BR to reflect the change. These pieces are those piece which were on the
         * board before making the changes to the BR while playing the move. Pieces are cloned so that any
         * relevant information such has whether the piece has moved, previous square, type, color etc. can be
         * preserved. properly.
         */
        public Builder pieceToUndo(Piece... piece) {
            for (Piece x : piece) undo.piece(x);
            return this;
        }

        /**
         * Performs the same as squareToUndo() but the squares are marked for redoing the change.
         */
        public Builder squareToRedo(String... square) {
            for (String x : square) redo.square(x);
            return this;
        }

        /**
         * This method does the same as pieceToUndo() method but pieces are used in redoing the change.
         **/
        public Builder pieceToRedo(Piece... piece) {
            for (Piece x : piece) redo.piece(x);
            return this;
        }

        /**
         * When an en-passant move is undone, then the en-passer object no longer remains valid. This
         * method allows a way to indicate that so that after undoing the en-passant move, the MoveMaker
         * can set null as en-passer so that on reflecting this BRHistory object can set null to en-passer
         * for the enemy thus undoing the change properly.
         */
        public Builder enPasserToUndo(EnPasser enPasser) {
            undo.enPasser = enPasser;
            return this;
        }

        /**
         * If the piece who is making the move, is a pawn and being an en-passer, then this will
         * get an en-passer object in the MoveMaker and that en-passer object needs to be saved
         * somehow and restored in redoing the move so that the next enemy move can acquire it
         * and be able to take it if they want.
         * <p>
         * It is the MoveMaker who informs the BRHistorian about the en-passer and the BRHistorian
         * saves that to the most recent BRHistory object on the stack.
         */
        public Builder enPasserToRedo(EnPasser enPasser) {
            redo.enPasser = enPasser;
            return this;
        }

        public BRHistory build() {
            return new BRHistory(moveIndex, undo, redo);
        }

        /*
         * Build BRHistory as configured in the builder chain call and save this history to the given
         * BRHistorian with the corresponding move index.
         * */
        public void saveTo(BRHistorian historian) {
            historian.saveChange(build());
        }

    }

}
