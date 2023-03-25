package tanzi.gridman;

/**
 * Data class for holding point to be used by GridMan library. Each point object represent starting
 * x & y coordinate of a grid in the rectangle.
 * <p>
 * Member variables are kept public for easier access for library usages.
 */

public class GPoint {
    public double x, y;

    public GPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("GPoint: (%.2f, %.2f)", x, y);
    }

}