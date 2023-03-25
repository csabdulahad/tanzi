package tanzi.app;

/**
 * Piece Drawable(PD) holds various information related to drawing a chess piece onto the
 * canvas. It has two important methods namely update & paint method which are always needed
 * when it comes to draw any object onto the screen.
 * <p>
 * Piece Drawable is designed to be working with generic types. It is because that the default
 * Tanzi implementation has been designed to be as generic as possible.
 * <p>
 * It extends the {@link Drawable} abstract class to support structured graphical implementation.
 * <p>
 * In the constructor, it takes a BitmapPainter object in which the app will implement the
 * actual piece drawing code using the graphics context provided by the platform.
 *
 * @param <G> The graphics context for the canvas.
 * @param <B> The bitmap representing the piece to be drawn on the canvas.
 */

public class PieceDrawable<G, B> extends Drawable<G> {
    private final float frameRate = 17f;

    private final int animSpeed = 10;

    // variable that controls how often we should update the animation properties
    private float tick = 0.0f;

    private final PieceBitmap<B> pieceBitmap;

    private final double destX;
    private final double destY;

    private final boolean forwardMotionX, forwardMotionY;

    private final double stepX, stepY;

    private boolean isAtDest = false;

    private final BitmapPainter<G, B> bitmapPainter;

    public PieceDrawable(double x, double y, double destX, double destY, PieceBitmap<B> pieceBitmap, BitmapPainter<G, B> bitmapPainter) {
        super(x, y);

        // set variables
        this.pieceBitmap = pieceBitmap;
        this.bitmapPainter = bitmapPainter;

        this.destX = destX;
        this.destY = destY;

        // calculate the forward motion along X-axis
        forwardMotionX = destX > x;
        stepX = (Math.max(destX, x) - Math.min(destX, x)) / (float) animSpeed;

        // calculate the forward motion along Y-axis
        forwardMotionY = destY > y;
        stepY = (Math.max(destY, y) - Math.min(destY, y)) / (float) animSpeed;
    }

    @Override
    public void update(double delta) {

        // if we have done with animation already then return
        if (isAtDest) return;

        // increase the counter to count the delta
        tick += delta;

        // we are updating animation after each nanosecond is passed
        if (tick < frameRate) return;

        // update the x and y based on motion forward or backward
        x += forwardMotionX ? stepX : -stepX;
        y += forwardMotionY ? stepY : -stepY;

        // adjust x based on motion after the increment by stepX above
        if (forwardMotionX) {
            if (Double.compare(x, destX) > 0) x = destX;
        } else if (Double.compare(x, destX) < 0) {
            // it is backward motion & above decrement made the piece at destSquare already
            x = destX;
        }

        // adjust y based on motion after the increment by stepY above
        if (forwardMotionY) {
            if (Double.compare(y, destY) > 0) y = destY;
        } else if (Double.compare(y, destY) < 0) {
            // it is backward motion & above decrement made the piece at destSquare already
            y = destY;
        }

        // check whether the piece is at dest square from both x and y-axis
        if (Double.compare(x, destX) == 0 && Double.compare(y, destY) == 0) isAtDest = true;

        // reset the counter
        tick = 0.0f;
    }

    @Override
    public void paint(G context) {
        bitmapPainter.paint(context, pieceBitmap.bitmap);
    }

}
