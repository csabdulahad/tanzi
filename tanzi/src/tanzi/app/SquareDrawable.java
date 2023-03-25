package tanzi.app;

/**
 * It is often required to highlight squares on a chess board such as when a move
 * has been performed then it is common to show which squares the move happened to
 * & from. This class can draw on those squares to highlight them visually.
 * <p>
 * Any listener to {@link Game}'s move change listener, can acquire list of
 * SquareDrawable to highlight the move on the board.
 * <p>
 * Note: It seems exactly identical in terms of functionality and data it represents
 * with {@link CircleDrawable} class. The purpose it exists because the implementation
 * can have clear idea & purpose of each drawable.
 *
 * @param <G> The graphics context for the canvas.
 */
public class SquareDrawable<G> extends Drawable<G> {

    private final Painter<G> painter;


    /**
     * The SquareDrawable constructor takes in the xy coordinates for highlighting
     * that square.
     *
     * @param x       X coordinate of the square to be highlighted.
     * @param y       Y coordinate of the square to be highlighted.
     * @param painter An implementation of {@link Painter} interface which knows how to
     *                and what to draw to highlight the square.
     */
    public SquareDrawable(double x, double y, Painter<G> painter) {
        super(x, y);
        this.painter = painter;
    }

    @Override
    void update(double delta) {

    }

    @Override
    void paint(G context) {
        painter.paint(context);
    }

}
