package tanzi.algorithm;

import tanzi.model.Piece;
import tanzi.staff.BoardRegistry;

import java.util.ArrayList;
import java.util.List;

import static tanzi.algorithm.GeometryEngineer.BY_FILE;
import static tanzi.algorithm.GeometryEngineer.BY_RANK;

/**
 * each move in PGN(Portal Game Notation) must be represented in unambiguous manner so that it
 * doesn't confuse both the human and computer for later analysis or recording the game.
 * <p>
 * this class captures those related methods which can calculate unique names for moves if necessary
 * based on the situation of the game and the move played. for any unique name situation, there at
 * max pieces can collide with each other either in 2, 3 or more than 3 in quantity.
 * <p>
 * algorithms here in this class optimize the number of checks by finding association between source
 * and destination square and how many pieces of the same type need to be considered in calculating
 * unique name.
 * <p>
 * HalfDiscarding algorithm (which uses association technique) can only be applied to in calculating
 * unique name for rooks. on the other hand, Bidirectional algorithm can be applied to queen, bishop,
 * knight as they can be more than 2 or 3 in number.
 * <p>
 * algorithms use various methods from Geometry Engineer class such as
 * <ul>
 * <li>possibleSidewaysTo</li>
 * <li>possibleDiagonalTo(for queen and bishop)</li>
 * <li>possibleKnightTo</li>
 * <li>getUnique(for what is unique in square name. is it file or rank?)</li>
 * <li>countAlong(how many pieces of specified type along a specified side/direction)</li>
 * <li>associationSideways(how two squares linked with each other. by file or rank?)</li>
 * </ul>
 */

public abstract class UniqueName {

    /**
     * FUNCTION : for a given piece move, it can return unique designation if required. If it fails
     * to calculate unique name then it returns null or for pawn and king; returns empty string for
     * unnecessary unique name.
     */
    public static String getUniqueName(int type, int color, String srcSquare, String destSquare, BoardRegistry br) {
        return switch (type) {
            case Piece.QUEEN -> getBidirectionalUniqueName(Piece.QUEEN, color, srcSquare, destSquare, br);
            case Piece.ROOK -> getHalfDiscardingUniqueName(Piece.ROOK, color, srcSquare, destSquare, br);
            case Piece.BISHOP -> getBidirectionalUniqueName(Piece.BISHOP, color, srcSquare, destSquare, br);
            case Piece.KNIGHT -> getBidirectionalUniqueName(Piece.KNIGHT, color, srcSquare, destSquare, br);
            default -> null;
        };
    }

    /**
     * FUNCTION : this function leverages the BIDIRECTIONAL Algorithm created by Abdul Ahad. for a
     * given source & destination square, it calculates regarding how many of same type
     * of pieces are there by file & ranks and returns the calculated unique name for
     * source piece to represent. this function works for both Knight & Bishop only.
     * <p>
     * if this function can't calculate the unique name then it returns null; it returns
     * empty string for unnecessary unique name.
     * <p>
     * for further clarification, look for BIDIRECTIONAL algorithm within this project.
     */
    private static String getBidirectionalUniqueName(int type, int color, String srcSquare, String destSquare, BoardRegistry br) {
        ArrayList<String> squareList = null;

        // get list of squares containing the same type of piece that can make move to destSquare

        if (type == Piece.QUEEN) {
            /* for queen, we know that, it can play like rook & bishop, so calculate possible queen
             * coming squares for both sideways & diagonal squares
             * */
            squareList = GeometryEngineer.possibleSidewaysTo(destSquare, Piece.QUEEN, color, br);
            squareList.addAll(GeometryEngineer.possibleDiagonalTo(destSquare, Piece.QUEEN, color, br));
        }

        if (type == Piece.BISHOP)
            squareList = GeometryEngineer.possibleDiagonalTo(destSquare, type, color, br);

        if (type == Piece.KNIGHT)
            squareList = GeometryEngineer.possibleKnightTo(destSquare, color, br);

        if (squareList == null) return null;

        discardPinnedPiece(destSquare, squareList, br);

        squareList.remove(srcSquare);

        // No unique designation is required
        if (squareList.size() == 0)
            return "";

        // unique name can be either the file name or the rank of the source square
        if (squareList.size() == 1)
            return GeometryEngineer.getUnique(srcSquare, squareList.get(0));

        /*
         * we need to test how pieces from different sides of different numbers can prefer which
         * unique designation such as file, rank or exclusive square name.
         * */

        char file = srcSquare.charAt(0);
        char rank = srcSquare.charAt(1);

        int countAlongFile = GeometryEngineer.countAlong(BY_FILE, file, squareList);
        int countAlongRank = GeometryEngineer.countAlong(BY_RANK, rank, squareList);

        /*
         * the order of checking for evaluating count variables for file and rank is important. by
         * any means they can't be reordered according to the app.algorithm.
         * */

        if (countAlongFile == 0 && countAlongRank == 0)
            return String.valueOf(file);

        // exclusive square name can only resolve the unique name
        if (countAlongFile >= 1 && countAlongRank >= 1)
            return srcSquare;

        // file has more than one pieces that can make move; so rank is unique here
        if (countAlongFile >= 1)
            return String.valueOf(Character.getNumericValue(rank));

        // rank has more than one pieces that can make move; so file is unique here
        if (countAlongRank >= 1)
            return String.valueOf(file);

        // for unresolved unique name
        return null;
    }

    /**
     * FUNCTION : this method calculates unique name where it doesn't consider the all four sides
     * namely top, left, right, bottom. it discards half of the sides by finding
     * association between source & destination squares.
     * <p>
     * USAGE : this algorithm is particularly applicable for figuring rook unique name. other
     * applications are unknown.
     */
    private static String getHalfDiscardingUniqueName(int type, int color, String srcSquare, String destSquare, BoardRegistry br) {

        /* HALF-DISCARDING ALGORITHM :
         * 1. first we acquire a list of squares of given piece of the army that can go the dest square
         * 2. then we remove the moving piece square to remove itself from calculating
         * 3. if list has 0 square, then there is no need for unique designation; return empty string
         * 4. if list has 1, then calculate the difference; return difference
         * 5. if more than 1, then we can discard other squares just by calculating file or
         * rank as appropriate between moving square & destination square. And then return the unique
         * name
         * */

        // step 1
        ArrayList<String> possiblePieceSquare = GeometryEngineer.possibleSidewaysTo(destSquare, type, color, br);

        discardPinnedPiece(destSquare, possiblePieceSquare, br);

        // step 2
        possiblePieceSquare.remove(srcSquare);

        // step 3; No unique designation is required
        if (possiblePieceSquare.size() == 0)
            return "";

        // step 4
        if (possiblePieceSquare.size() == 1)
            return GeometryEngineer.getUnique(srcSquare, possiblePieceSquare.get(0));

        // step 5; find the association between source square & destination square
        int associationDirection = GeometryEngineer.associationSideways(srcSquare, destSquare);

        if (associationDirection == -1) return null;
        int uniqueDesignationIndex = associationDirection == BY_FILE ? BY_RANK : BY_FILE;

        return String.valueOf(srcSquare.charAt(uniqueDesignationIndex));
    }

    // for a list of squares of potential pieces , they can be pinned or have some
    // way of restriction to make the move. let's check for it. we will discard those
    // who are pinned.
    private static void discardPinnedPiece(String destSquare, List<String> possibleSquareListToDest, BoardRegistry br) {
        ArrayList<String> bufferedList = new ArrayList<>(possibleSquareListToDest);
        for (String square : bufferedList)
            if (!Arbiter.pieceCanGo(square, destSquare, true, br))
                possibleSquareListToDest.remove(square);
    }

}
