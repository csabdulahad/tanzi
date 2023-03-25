package tanzi.app;

import tanzi.gridman.GPoint;

/**
 * A Tanzi implementation would require to highlight the last move made on the board.
 * This is a basic feature of a chess application. This interface allows an app to
 * acquire that squares to be highlighted on each move made on the board.
 */

public interface LastMoveListener {

    /**
     * After a changes has been made to the BR, this method gets called by the game
     * object to notify any listener to allow them highlight the last move played
     * on the board.
     * <p>
     * Here, square indexes are calculated based on the orientation configuration,
     * the game at.
     *
     * @param a One of squares that needs highlighting.
     * @param b Another square that needs highlighting.
     */
    void onLastMoveFocused(GPoint a, GPoint b);

}
