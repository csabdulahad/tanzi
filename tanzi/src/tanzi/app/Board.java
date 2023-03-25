package tanzi.app;

import tanzi.gridman.GPoint;
import tanzi.gridman.GridMan;
import tanzi.model.Square;

public class Board {

    public enum Orientation {Black, White}

    private Orientation orientation;

    private final GridMan gMan;

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Board(double boardSize) {
        double gridSize = boardSize / 8;
        this.gMan = new GridMan(boardSize, boardSize, gridSize, gridSize);
    }

    /**
     * It returns the index for the specified square based on the board orientation by simply
     * calling {@link Square#index(String, Orientation)} method.
     */
    public int index(String square) {
        return Square.index(square, orientation);
    }

    /**
     * For a xy coordinate, this method can calculate, the mapping grid index of the chess
     * board. It calls the {@link GridMan#getGridIndex(double, double)} internally to get
     * the grid index from white's perspective and returns it if the board is in that
     * orientation. If not, then it gets the square index for that coordinate with orientation
     * calculation from the {@link Square#index(String, Orientation)} method.
     */
    public int index(double x, double y) {
        int index = gMan.getGridIndex(x, y);
        if (index == -1) return -1;

        // if the orientation is from white's perspective then just return the index
        if (orientation == Orientation.White) return index;

        // we need to translate the index from black's perspective
        return Square.index(Square.forIndex(index), Orientation.Black);
    }

    /**
     * For a square, this method can calculate the GPoint based on the board orientation.
     * Internally, it calls {@link Board#index(String)} to get the grid index
     * for the square and then gets the GPoint from the GridMan instance.
     */
    public GPoint gpoint(String square) {
        return gMan.getPoint(index(square));
    }

    public Orientation orientation() {
        return orientation;
    }

}
