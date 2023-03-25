package tanzi.app;

/**
 * A factory implementation of the PieceBitmap for PieceBitmapPool usage. This class
 * takes in a PieceBitmapFactory.Generator argument to produce PieceBitmap for the
 * pool.
 * <p>
 * This factory removes the responsibility of creating a PieceBitmap of platform dependent
 * type from the PieceBitmapPool by asking the user of PieceBitmapPool to supply a factory
 * implementation of this class.
 *
 * @param <T> Here T is {@code PieceBitmap<B> } which is produced by this factory. B represents the
 *            platform dependent bitmap type to be used by the graphics context to draw the
 *            image on a canvas.
 */

public class PieceBitmapFactory<T> {

    private final Generator<T> generator;

    public PieceBitmapFactory(Generator<T> generator) {
        this.generator = generator;
    }

    /**
     * It calls the produce method of the setup generator with the specified
     * type and color to create a PieceBitmap of a generic type.
     */
    public T produce(int type, int color) {
        return generator.produce(type, color);
    }

    /**
     * The generator class is used by the PieceBitmapFactory to produce a
     * PieceBitmap of a generic type upon request from PieceBitmapPool.
     */
    public interface Generator<T> {
        /**
         * This method is called by the PieceBitmapFactory to produce the
         * PieceBitmap of specified type and color and the PieceBitmap is
         * eventually returned to the PieceBitmapPool.
         */
        T produce(int type, int color);
    }

}