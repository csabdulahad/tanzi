package tanzi.app;

/**
 * Anything can be drawn onto the screen, must have extended this class. Any
 * implementation of how an object gets painted on the screen really depends on two
 * basic methods namely update with a delta argument and a paint method with a graphics
 * context to draw.
 * <p>
 * Tanzi app implementation enforces these rules so that graphical implementation can be
 * more structured and clear.
 *
 * @param <G> The platform dependent graphics context to be used to draw on the canvas.
 */

public abstract class Drawable<G> {

    protected double x, y;

    public Drawable(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Any drawing entity will be updated with a delta difference in time frame. The entity
     * will update its coordinates, animation, visibility etc. here in this method.
     */
    abstract void update(double delta);

    /**
     * This method will be called by the platform UI drawing thread with platform dependent
     * graphics context canvas object so that the entity gets a chance to draw itself on the
     * screen after an update.
     */
    abstract void paint(G context);

}
