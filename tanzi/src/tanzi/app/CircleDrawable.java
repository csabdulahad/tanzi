package tanzi.app;

/**
 * A typical chess GUI application should allow the player to see where a clicked piece can make
 * the next move to. To make that implementation easier, Tanzi default implementation comes with
 * this {@link CircleDrawable} to encapsulate those requirements.
 * <p>
 * The {@link Game#drawableSquaresToMove(double, double, Painter)} creates & returns the list of
 * CircleDrawable so that the GUI part of the implementation can draw the circles on the appropriate
 * locations on the chess board.
 *
 * @param <G> The graphics context for the canvas.
 */

public class CircleDrawable<G> extends Drawable<G> {

    private final Painter<G> painter;

    /**
     * The CircleDrawable constructor takes in the xy coordinates for the circle to be
     * drawn at.
     *
     * @param x       X coordinate of the circle to be drawn.
     * @param y       Y coordinate of the circle to be drawn.
     * @param painter An implementation of {@link Painter} interface which knows how to
     *                draw the circle
     */
    public CircleDrawable(double x, double y, Painter<G> painter) {
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
