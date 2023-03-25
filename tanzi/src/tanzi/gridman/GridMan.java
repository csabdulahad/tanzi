package tanzi.gridman;

import java.util.HashMap;

/**
 * This library was created by Abdul Ahad. For a  known height, width, number of rows & columns,
 * of a rectangle this library can calculate the starting co-ordinate point for any given grid
 * index. Grids are assumed to be of uniform size meaning each grid of geometric rectangle shape.
 * <p>
 * This library can be very powerful and useful when programs are dealing with grid based shape
 * interaction or processing where program knows the above-mentioned critical values.
 * <p>
 * Grid starts from 1, unlike zero-based array index for making clear assumption about grid counting.
 * <p>
 * Critical variables are those variables which define a rectangle in enough descriptive form so that
 * the library can calculate such rectangle's width, height, number of columns, rows etc.
 *
 * @author Abdul Ahad
 * @version 1.0.0
 * @link <a href="https://abdulahad.net/gridman">GridMan Official Doc</a>
 */

public class GridMan {

    private double rectW, rectH;

    private double gridW, gridH;

    private HashMap<Integer, GPoint> indexPoint;

    private int numOfGrid;

    public GridMan(double rectW, double rectH, double gridW, double gridH) {
        this.rectW = rectW;
        this.rectH = rectH;
        this.gridW = gridW;
        this.gridH = gridH;
        calculateIndexPointTable();
    }

    /**
     * For given critical grid variables, it calculates points. Every time for any change to those
     * variables, it clears out the previously calculated points and adds newly calculated points.
     */
    private void calculateIndexPointTable() {
        numOfGrid = (int) (Math.round(rectW / gridW) * Math.round(rectH / gridH));
        if (indexPoint == null) indexPoint = new HashMap<>(numOfGrid);
        else indexPoint.clear();

        for (int i = 1; i <= numOfGrid; i++) {
            double x = getX(i);
            double y = getY(i);
            GPoint GPoint = new GPoint(x, y);
            indexPoint.put(i, GPoint);
        }
    }

    /**
     * For a given x and y, this method calculates which grid number the point belongs to.
     *
     * @return The grid index for the specified xy coordinates. Returns -1, if the given
     * point can't be fitted within any grid defined by the critical variables.
     */
    public int getGridIndex(double x, double y) {
        for (int i = 1; i <= numOfGrid; i++) {
            GPoint GPoint = indexPoint.get(i);
            if (GPoint == null) continue;

            boolean xRange = x >= GPoint.x && x <= (GPoint.x + gridW);
            boolean yRange = y >= GPoint.y && y <= (GPoint.y + gridH);
            if (xRange && yRange) return i;
        }
        return -1;
    }

    /*
     * this method returns GridManPoint containing the starting point for a given grid index of a
     * grid. if the given grid index is larger than number of grid then it returns null.
     * */
    public GPoint getPoint(int gridIndex) {
        if (gridIndex > numOfGrid) return null;
        double x = getX(gridIndex);
        double y = getY(gridIndex);
        return new GPoint(x, y);
    }

    /*
     * description for these algorithms can be found at respective library website. consult the
     * official doc for more understanding.
     * */

    /**
     * Starting x coordinate of a grid specified by the index can be calculated by this method.
     */
    private double getX(int gridIndex) {
        double step1 = gridIndex * gridW;
        while (step1 > rectW) {
            step1 = step1 - rectW;
        }
        double step2 = step1 / gridW;
        double step3 = step2 * gridW;
        return step3 - gridW;
    }

    /**
     * Starting y coordinate of a grid specified by the index can be calculated by this method.
     */
    private double getY(int gridIndex) {
        double step1 = gridIndex * gridW;
        float step2 = (float) Math.ceil(step1 / rectH);
        double step3 = step2 * gridH;
        return step3 - gridH;
    }

    public void setRectWidth(float rectW) {
        this.rectW = rectW;
        calculateIndexPointTable();
    }

    public void setRectHeight(float rectH) {
        this.rectH = rectH;
        calculateIndexPointTable();
    }

    public void setGridWidth(double gridW) {
        this.gridW = gridW;
        calculateIndexPointTable();
    }

    public void setGridHeight(double gridH) {
        this.gridH = gridH;
        calculateIndexPointTable();
    }

}