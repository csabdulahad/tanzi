package tanzi.app;

/**
 * A POJO represents graphical piece on the board so that the app can
 * draw any piece of the BR. This class works with generic representation
 * of the piece bitmap, depending on the platform implementation.
 *
 * @param <T> Here the type T represents the platform dependent bitmap type
 *            which will be used by the graphics context to draw on canvas.
 */

public class PieceBitmap<T> {

    public int type, color;

    /**
     * The platform dependent graphical representation of the piece.
     */
    public T bitmap;


    /**
     * Create a piece with graphical representation, type and color to be used
     * in the graphical context.
     *
     * @param type   What piece it is.
     * @param color  Of which color.
     * @param bitmap The platform dependent graphical representation of the piece.
     */
    public PieceBitmap(int type, int color, T bitmap) {
        this.type = type;
        this.color = color;
        this.bitmap = bitmap;
    }

}
