package tanzi.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Each move played in the BR is represented as BRHistory consisting changes. Changes are precisely described
 * using this class. It has all the information related to a move. The information allows the full undo/redo
 * the move properly in the BR.
 */

public class BRChange {

    /**
     * Holds the en-passer object the way it was available after making the move or how
     * it should be when we undo the move.
     **/
    public EnPasser enPasser;

    /**
     * The list of square which need to be removed in order to undo/redo this BRChange.
     */
    public List<String> squares = new ArrayList<>();

    /**
     * The list of cloned pieces during the changes were being made in the BR, so that on undo/redo
     * such pieces can be placed back on the board to fully reflect the undo/redo move.
     */
    public List<Piece> pieces = new ArrayList<>();


    /**
     * Saves the square which needs to be removed during undoing/redoing the move.
     */
    public void square(String square) {
        squares.add(square);
    }

    /**
     * Saves the cloned piece for undoing/redoing the move.
     */
    public void piece(Piece piece) {
        pieces.add(piece);
    }

}
