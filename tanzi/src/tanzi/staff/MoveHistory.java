package tanzi.staff;

import tanzi.model.MoveChange;
import tanzi.model.Piece;

import java.util.ArrayList;

public class MoveHistory {

    public static class Change {
        public int moveIndex;
        public MoveChange moveChange;

        public Change(int moveIndex, MoveChange moveChange) {
            this.moveIndex = moveIndex;
            this.moveChange = moveChange;
        }
    }

    private final ArrayList<Change> backChange;

    public MoveHistory() {
        backChange = new ArrayList<>();
    }

    public void saveSingleChange(int moveIndex, String newSquare, Piece pieceBeforeMoving) {
        MoveChange moveChange = MoveChange.createChange(newSquare, pieceBeforeMoving);
        backChange.add(new Change(moveIndex, moveChange));
    }

    public void saveBinaryChange(int moveIndex, String nsA, Piece pieceA, String nsB, Piece pieceB) {
        MoveChange moveChangeA, moveChangeB;

        moveChangeA = MoveChange.createChange(nsA, pieceA);
        moveChangeB = MoveChange.createChange(nsB, pieceB);

        moveChangeA.tail = true;

        backChange.add(new Change(moveIndex, moveChangeB));
        backChange.add(new Change(moveIndex, moveChangeA));
    }

    public MoveChange[] getLastChange() {

        if (backChange.isEmpty()) return new MoveChange[]{};

        // get the last change
        Change change = backChange.remove(backChange.size() - 1);

        // extract the MoveChange object
        MoveChange moveChange = change.moveChange;
        if (moveChange.tail) {
            // also get the tail change
            Change tailChange = backChange.remove(backChange.size() - 1);
            MoveChange tail = tailChange.moveChange;
            return new MoveChange[]{moveChange, tail};
        }

        return new MoveChange[]{moveChange};
    }

    public MoveChange peek() {
        if (backChange.isEmpty()) return null;
        return backChange.get(backChange.size() - 1).moveChange;
    }

    public Change popChange() {
        if (backChange.isEmpty()) return null;
        return backChange.remove(backChange.size() - 1);
    }

    public int currentIndexOnStack() {
        if (backChange.isEmpty()) return -1;
        return backChange.get(backChange.size() - 1).moveIndex;
    }

    public void __clear() {
        backChange.clear();
    }

}
