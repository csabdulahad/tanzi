package tanzi.app;

/**
 * The default Tanzi app implementation has been designed as generically as
 * possible. The two graphics related methods update and paint are always
 * required. Even though the update method can be coded without being
 * dependent on actual types as it mainly involves calculating various
 * coordinates values, however, paint method requires platform's graphics
 * implementation.
 * <p>
 * This interface encapsulates this implementation and allows the default
 * Tanzi app implementation work with those platform dependent types easily.
 *
 * @param <G> The graphics context which is used to draw objects onto the canvas.
 * @param <B> The bitmap representing the piece to be drawn on the canvas.
 */

public interface BitmapPainter<G, B> {

    /**
     * When a canvas gets painted, the SquareDrawables(SD) need to be painted. The canvas
     * painting, calls this method via {@link PieceDrawable#paint(G context)} method
     * to draw piece onto the canvas using platform dependent type. When the list of
     * SD are created from the BR using the game object, a bitmap painter implementation
     * has to be provided to be able to draw objects onto the canvas and this does that.
     *
     * @param context The platform dependent graphics context of the canvas.
     * @param bitmap  The bitmap representing the piece to be drawn on the canvas.
     */
    void paint(G context, B bitmap);

}
