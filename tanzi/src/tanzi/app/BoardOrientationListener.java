package tanzi.app;

/**
 * A chess board can be oriented from the perspective of either white or black player.
 * Any GUI application should be able to support this board flipping by rendering the
 * necessary changes on the screen.
 * <p><br>
 * A board flipping or orientation change is nothing but the changes of x,y coordinates
 * of the pieces are being drawn on the screen. The GUI should listener to this change
 * in the Game object and update the coordinates of the pieces to reflect the new
 * orientation.
 */

public interface BoardOrientationListener {

    /**
     * The Game object will call this on the orientation change listener with the new
     * orientation configuration to tell that the graphical representation of the BR
     * should be updated to visualize the change in orientation.
     */
    void onOrientationChange(Board.Orientation orientation);

}
