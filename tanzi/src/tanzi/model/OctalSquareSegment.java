package tanzi.model;

import tanzi.staff.GeometryEngineer;

import java.util.ArrayList;
import java.util.List;

/*
 * this class breaks the OS into segments. we can ask this class to give us a segment by index. so
 * while checking for threats towards for example a king's destination square, we can discard a
 * segment if we find the segment is not being eligible to be considered to be checked for possible
 * threat to the king as no square from the segment(side-way or diagonal direction) matches the
 * king's destination square direction.
 *
 * $ d5 d6 d7 d8 $ d3 d2 d1 $ c4 b4 a4 $ e4 f4 g4 h4 $ c5 b6 a7 $ e5 f6 g7 h8 $ c3 b2 a1 $ e3 f2 g1 $
 *
 * here it calculates that there are 8 segments. the first segment is at 0. so the highest segment
 * number is 8 - 1 = 7.
 *
 * the indexPoint holds the inclusive starting index of a segment after "$" and when getSegment()
 * returns, it fixes the offByOneError to discard the tailing "$" sign.
 *
 * for NO SEGMENT value(totalSegment count is -1) the behaviour is not defined. there is
 * List<String> type which gets returned instead of ArrayList. it needs to be fixed.
 *
 * */

public class OctalSquareSegment {

    private final ArrayList<String> os;
    private final ArrayList<Integer> indexPoint;

    public OctalSquareSegment(ArrayList<String> os) {
        this.os = os;
        this.indexPoint = new ArrayList<>();
        calculateSegmentBoundIndex();
    }

    /*
     * this method returns segment out of OS by segment index. it checks for index range and
     * calculates segment starting and ending index then return the segment.
     * */
    public List<String> getSegment(int segmentIndex) {

        // check whether the segment index that was passed to this function is in valid range
        if (getTotalSegment() == -1) return null;
        if (segmentIndex >= getTotalSegment()) return null;

        // deduct 1 from the next segment's indexPoint to get end index for requested segment
        int offByOneDollar = indexPoint.get(segmentIndex + 1) - 1;

        return os.subList(indexPoint.get(segmentIndex), offByOneDollar);
    }

    /*
     * this function calculates the inclusive-starting index for segments.
     * for the following example OS, it creates an array of  [1, 5] to mark the starting index for
     * each segment.
     *
     * $ d3 d4 d5 $ g3 h2 $
     * */
    private void calculateSegmentBoundIndex() {
        String[] index = os.toArray(new String[]{});
        for (int i = 0; i < index.length; i++) {
            if (index[i].equals(GeometryEngineer.OS_SEPARATOR)) {
                indexPoint.add(i + 1); // add the next point from the $ found at this point
            }
        }
    }

    /*
     * returns -1 if no segment is found
     * */
    public int getTotalSegment() {
        int totalSegment = 0;
        for (String segment : os)
            if (segment.equals(GeometryEngineer.OS_SEPARATOR)) totalSegment++;
        return totalSegment - 1;
    }

}
