package tanzi.staff;

import tanzi.algorithm.SquareFilter;
import tanzi.model.Piece;
import tanzi.model.Square;

import java.util.ArrayList;

public class GeometryEngineer {

    // constants are used for representing directions for calculating association, counting etc.
    public static final int BY_FILE = 0;
    public static final int BY_RANK = 1;

    // separator to be used between OS to mark beginning & ending of a segment
    public static final String OS_SEPARATOR = "$";

    private GeometryEngineer() {

    }

    /*
     * this method returns valid squares for a given piece to move specified by its current square
     * on the board, color, and type with optional armyCheck. valid moves are only proved
     * by chess geometry. no pin detection, illegal move checking etc. are involved in the
     * calculation.
     * */
    public static ArrayList<String> validSquare(int type, int color, String currentSquare, boolean armyCheck, BoardRegistry br) {
        return switch (type) {
            case Piece.PIECE_KING -> kingSquare(currentSquare, color, armyCheck, br);
            case Piece.PIECE_QUEEN -> queenSquare(currentSquare, color, armyCheck, br);
            case Piece.PIECE_ROOK -> rookSquare(currentSquare, color, armyCheck, br);
            case Piece.PIECE_BISHOP -> bishopSquare(currentSquare, color, armyCheck, br);
            case Piece.PIECE_KNIGHT -> knightSquare(currentSquare, color, armyCheck, br);
            default -> pawnSquare(currentSquare, color, br);
        };
    }

    /*
     * these methods calculate squares for pieces from a given square by only chess geometry rules.
     * it supports the own army check to get more valid squares for the current board registry.
     * */

    private static ArrayList<String> rookSquare(String from, int color, boolean ownArmyCheck, BoardRegistry br) {

        ArrayList<String> squareList = new ArrayList<>();

        int counter = 1;

        boolean topBlock = false;
        boolean bottomBlock = false;
        boolean leftBlock = false;
        boolean rightBlock = false;

        boolean running = true;
        while (running) {

            String square;

            // top file
            if (!topBlock) {
                square = getSquareAt(from, 0, counter);
                topBlock = SquareFilter.filterForIterativeSquareFinding(color, square, ownArmyCheck, squareList, br);
            }

            // bottom file
            if (!bottomBlock) {
                square = getSquareAt(from, 0, -counter);
                bottomBlock = SquareFilter.filterForIterativeSquareFinding(color, square, ownArmyCheck, squareList, br);
            }

            // left file
            if (!leftBlock) {
                square = getSquareAt(from, -counter, 0);
                leftBlock = SquareFilter.filterForIterativeSquareFinding(color, square, ownArmyCheck, squareList, br);
            }

            // right file
            if (!rightBlock) {
                square = getSquareAt(from, counter, 0);
                rightBlock = SquareFilter.filterForIterativeSquareFinding(color, square, ownArmyCheck, squareList, br);
            }

            counter++;

            if (topBlock && bottomBlock && leftBlock && rightBlock) running = false;
        }

        return squareList;
    }

    private static ArrayList<String> bishopSquare(String from, int color, boolean ownArmyCheck, BoardRegistry br) {

        ArrayList<String> squareList = new ArrayList<>();

        // flags that identify whether a diagonal direction is off for the bishop to move along
        boolean topLeftBlock = false;
        boolean topRightBlock = false;
        boolean bottomLeftBlock = false;
        boolean bottomRightBlock = false;

        int topStep = 1;
        int bottomStep = 1;

        boolean running = true;
        while (running) {

            String diagonal;

            // top-left diagonal
            if (!topLeftBlock) {
                diagonal = getSquareAt(from, -topStep, topStep);
                topLeftBlock = SquareFilter.filterForIterativeSquareFinding(color, diagonal, ownArmyCheck, squareList, br);
            }

            // top-right diagonal
            if (!topRightBlock) {
                diagonal = getSquareAt(from, topStep, topStep);
                topRightBlock = SquareFilter.filterForIterativeSquareFinding(color, diagonal, ownArmyCheck, squareList, br);
            }

            // bottom-left
            if (!bottomLeftBlock) {
                diagonal = getSquareAt(from, -bottomStep, -bottomStep);
                bottomLeftBlock = SquareFilter.filterForIterativeSquareFinding(color, diagonal, ownArmyCheck, squareList, br);
            }

            // bottom-right
            if (!bottomRightBlock) {
                diagonal = getSquareAt(from, bottomStep, -bottomStep);
                bottomRightBlock = SquareFilter.filterForIterativeSquareFinding(color, diagonal, ownArmyCheck, squareList, br);
            }

            topStep++;
            bottomStep++;

            if (topLeftBlock && topRightBlock && bottomLeftBlock && bottomRightBlock)
                running = false;
        }
        return squareList;
    }

    private static ArrayList<String> queenSquare(String from, int color, boolean ownArmyCheck, BoardRegistry br) {
        ArrayList<String> validSquareList = rookSquare(from, color, ownArmyCheck, br);
        validSquareList.addAll(bishopSquare(from, color, ownArmyCheck, br));
        return validSquareList;
    }

    private static ArrayList<String> knightSquare(String from, int color, boolean ownArmyCheck, BoardRegistry br) {

        ArrayList<String> squareList = new ArrayList<>();

        String destSquare;

        // top-left
        destSquare = getSquareAt(from, -1, 2);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, squareList, br);

        // left-top-diagonal
        destSquare = getSquareAt(from, -2, 1);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, squareList, br);

        // top-right
        destSquare = getSquareAt(from, 1, 2);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, squareList, br);

        // right-top-diagonal
        destSquare = getSquareAt(from, 2, 1);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, squareList, br);

        // bottom-left
        destSquare = getSquareAt(from, -1, -2);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, squareList, br);

        // left-bottom-diagonal
        destSquare = getSquareAt(from, -2, -1);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, squareList, br);

        // bottom-right
        destSquare = getSquareAt(from, 1, -2);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, squareList, br);

        // right-bottom-diagonal
        destSquare = getSquareAt(from, 2, -1);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, squareList, br);

        return squareList;
    }

    private static ArrayList<String> pawnSquare(String from, int color, BoardRegistry br) {

        ArrayList<String> validSquareList = new ArrayList<>();

        int rankStep = (color == Piece.COLOR_WHITE) ? 1 : -1;

        // first learn if the top square is available for the pawn
        String topSquare1 = getSquareAt(from, 0, rankStep);
        boolean anyPieceOnTop = br.anyPieceOn(topSquare1);
        if (!anyPieceOnTop) validSquareList.add(topSquare1);

        // check to see whether any enemy piece is on top-right or top-left square, where the pawn can go
        String topLeftSquare = getSquareAt(from, -1, rankStep);
        String topRightSquare = getSquareAt(from, 1, rankStep);

        boolean leftDiagonalAvail = !br.killingOwnArmy(topLeftSquare, color) && br.anyPieceOn(topLeftSquare);
        boolean rightDiagonalAvail = !br.killingOwnArmy(topRightSquare, color) && br.anyPieceOn(topRightSquare);

        if (leftDiagonalAvail) validSquareList.add(topLeftSquare);
        if (rightDiagonalAvail) validSquareList.add(topRightSquare);

        // figure out whether the pawn is at home for home pawn move
        if (!anyPieceOnTop) {
            int pawnRank = Character.getNumericValue(from.charAt(1));
            boolean atHome = pawnRank == 2 || pawnRank == 7;
            if (atHome) {
                rankStep = (color == Piece.COLOR_WHITE) ? 2 : -2;
                String topSquare2 = getSquareAt(from, 0, rankStep);
                // make sure that no piece is on the topSquare2
                boolean topSquare2Avail = !br.anyPieceOn(topSquare2);
                if (topSquare2Avail) validSquareList.add(topSquare2);
            }
        }

        return validSquareList;
    }

    private static ArrayList<String> kingSquare(String from, int color, boolean ownArmyCheck, BoardRegistry br) {

        // a king can go up to 8 squares from current square

        ArrayList<String> validSquareList = new ArrayList<>();
        String destSquare;

        // top
        destSquare = getSquareAt(from, 0, 1);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, validSquareList, br);

        // top-left
        destSquare = getSquareAt(from, -1, 1);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, validSquareList, br);

        // top-right
        destSquare = getSquareAt(from, 1, 1);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, validSquareList, br);

        // left
        destSquare = getSquareAt(from, -1, 0);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, validSquareList, br);

        // right
        destSquare = getSquareAt(from, 1, 0);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, validSquareList, br);

        // bottom
        destSquare = getSquareAt(from, 0, -1);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, validSquareList, br);

        // bottom-left
        destSquare = getSquareAt(from, -1, -1);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, validSquareList, br);

        // bottom-right
        destSquare = getSquareAt(from, 1, -1);
        SquareFilter.filterSquare(color, destSquare, ownArmyCheck, validSquareList, br);


        return validSquareList;
    }

    /**
     * FUNCTION : for a given type & color, this function can estimate squares from where pieces can
     * go to the given focus square SIDE-WISE(top, left, bottom, right).
     */
    public static ArrayList<String> possibleSidewaysTo(String focusSquare, int type, int color, BoardRegistry br) {

        ArrayList<String> squareList = new ArrayList<>();

        int counter = 1;

        boolean topBlock = false;
        boolean bottomBlock = false;
        boolean leftBlock = false;
        boolean rightBlock = false;

        boolean running = true;
        while (running) {

            String square;

            // top file
            if (!topBlock) {
                square = getSquareAt(focusSquare, 0, counter);
                topBlock = SquareFilter.filterPossibleSquareToFocusSquare(type, color, square, squareList, br);
            }

            // bottom file
            if (!bottomBlock) {
                square = getSquareAt(focusSquare, 0, -counter);
                bottomBlock = SquareFilter.filterPossibleSquareToFocusSquare(type, color, square, squareList, br);
            }

            // left file
            if (!leftBlock) {
                square = getSquareAt(focusSquare, -counter, 0);
                leftBlock = SquareFilter.filterPossibleSquareToFocusSquare(type, color, square, squareList, br);
            }

            // right file
            if (!rightBlock) {
                square = getSquareAt(focusSquare, counter, 0);
                rightBlock = SquareFilter.filterPossibleSquareToFocusSquare(type, color, square, squareList, br);
            }

            counter++;

            if (topBlock && bottomBlock && leftBlock && rightBlock) running = false;
        }

        return squareList;
    }

    /**
     * FUNCTION : for a given type & color, this function can generate squares from where pieces can
     * go to the given focus square DIAGONALLY(top-left, top-right, bottom-left, bottom-right).
     */
    public static ArrayList<String> possibleDiagonalTo(String focusSquare, int type, int color, BoardRegistry br) {

        ArrayList<String> squareList = new ArrayList<>();

        // flags that identify if a diagonal direction is off for bishops to move along/come from
        boolean topLeftBlock = false;
        boolean topRightBlock = false;
        boolean bottomLeftBlock = false;
        boolean bottomRightBlock = false;

        int topStep = 1;
        int bottomStep = 1;

        boolean running = true;
        while (running) {

            String square;

            // top-left diagonal
            if (!topLeftBlock) {
                square = getSquareAt(focusSquare, -topStep, topStep);
                topLeftBlock = SquareFilter.filterPossibleSquareToFocusSquare(type, color, square, squareList, br);
            }

            // top-right diagonal
            if (!topRightBlock) {
                square = getSquareAt(focusSquare, topStep, topStep);
                topRightBlock = SquareFilter.filterPossibleSquareToFocusSquare(type, color, square, squareList, br);
            }

            // bottom-left
            if (!bottomLeftBlock) {
                square = getSquareAt(focusSquare, -bottomStep, -bottomStep);
                bottomLeftBlock = SquareFilter.filterPossibleSquareToFocusSquare(type, color, square, squareList, br);
            }

            // bottom-right
            if (!bottomRightBlock) {
                square = getSquareAt(focusSquare, bottomStep, -bottomStep);
                bottomRightBlock = SquareFilter.filterPossibleSquareToFocusSquare(type, color, square, squareList, br);
            }

            topStep++;
            bottomStep++;

            if (topLeftBlock && topRightBlock && bottomLeftBlock && bottomRightBlock)
                running = false;
        }
        return squareList;
    }

    /**
     * This method returns the squares from where knights can jump to a specified focus square. Here
     * the color is used to filter only knights of an army for finding how many knights from the
     * army can go from that focus square.
     *
     * @param focusSquare where knights can go to
     * @param color       the color of the army
     */
    public static ArrayList<String> possibleKnightTo(String focusSquare, int color, BoardRegistry br) {

        ArrayList<String> validSquareList = new ArrayList<>();
        String destSquare;

        // top-left
        destSquare = getSquareAt(focusSquare, -1, 2);
        if (destSquare != null)
            SquareFilter.filterPossibleKnightSquareToFocusSquare(color, destSquare, validSquareList, br);

        // left-top-diagonal
        destSquare = getSquareAt(focusSquare, -2, 1);
        if (destSquare != null)
            SquareFilter.filterPossibleKnightSquareToFocusSquare(color, destSquare, validSquareList, br);

        // top-right
        destSquare = getSquareAt(focusSquare, 1, 2);
        if (destSquare != null)
            SquareFilter.filterPossibleKnightSquareToFocusSquare(color, destSquare, validSquareList, br);

        // right-top-diagonal
        destSquare = getSquareAt(focusSquare, 2, 1);
        if (destSquare != null)
            SquareFilter.filterPossibleKnightSquareToFocusSquare(color, destSquare, validSquareList, br);

        // bottom-left
        destSquare = getSquareAt(focusSquare, -1, -2);
        if (destSquare != null)
            SquareFilter.filterPossibleKnightSquareToFocusSquare(color, destSquare, validSquareList, br);

        // left-bottom-diagonal
        destSquare = getSquareAt(focusSquare, -2, -1);
        if (destSquare != null)
            SquareFilter.filterPossibleKnightSquareToFocusSquare(color, destSquare, validSquareList, br);

        // bottom-right
        destSquare = getSquareAt(focusSquare, 1, -2);
        if (destSquare != null)
            SquareFilter.filterPossibleKnightSquareToFocusSquare(color, destSquare, validSquareList, br);

        // right-bottom-diagonal
        destSquare = getSquareAt(focusSquare, 2, -1);
        if (destSquare != null)
            SquareFilter.filterPossibleKnightSquareToFocusSquare(color, destSquare, validSquareList, br);

        return validSquareList;
    }

    /**
     * FUNCTION : for a given king square, this method calculates those squares that are aligned
     * with the king square. king aligned squares are enclosed & separated by notation '$' to make
     * further calculation easier & faster.
     *
     * @param kingSquare the king square for which you want to calculate the aligned squares
     */
    public static ArrayList<String> calculateKingAlignedSquare(String kingSquare) {

        ArrayList<String> alignedSquares = new ArrayList<>();

        char file = Square.getFileAsChar(kingSquare);
        int rank = Square.getRankAsInt(kingSquare);

        // get the top squares
        alignedSquares.add(OS_SEPARATOR);
        for (int r = rank + 1; r <= 8; r++) {
            String square = file + "" + r;
            alignedSquares.add(square);
        }

        // get the bottom squares
        alignedSquares.add(OS_SEPARATOR);
        for (int r = rank - 1; r >= 1; r--) {
            String square = file + "" + r;
            alignedSquares.add(square);
        }

        // get the left squares
        alignedSquares.add(OS_SEPARATOR);
        for (char f = (char) (file - 1); f >= 'a'; f--) {
            String square = f + "" + rank;
            alignedSquares.add(square);
        }

        // get the right squares
        alignedSquares.add(OS_SEPARATOR);
        for (char f = (char) (file + 1); f <= 'h'; f++) {
            String square = f + "" + rank;
            alignedSquares.add(square);
        }

        // get the top-left
        alignedSquares.add(OS_SEPARATOR);
        char f = file;
        int r = rank;
        while (true) {
            f--;
            r++;
            if (f < 'a' || r > 8) break;

            String square = f + "" + r;
            alignedSquares.add(square);
        }

        // get the top-right
        alignedSquares.add(OS_SEPARATOR);
        f = file;
        r = rank;
        while (true) {
            f++;
            r++;
            if (f > 'h' || r > 8) break;

            String square = f + "" + r;
            alignedSquares.add(square);
        }


        // get the bottom-left
        alignedSquares.add(OS_SEPARATOR);
        f = file;
        r = rank;
        while (true) {
            f--;
            r--;
            if (f < 'a' || r < 1) break;

            String square = f + "" + r;
            alignedSquares.add(square);
        }


        // get the bottom-right
        alignedSquares.add(OS_SEPARATOR);
        f = file;
        r = rank;
        while (true) {
            f++;
            r--;
            if (f > 'h' || r < 1) break;

            String square = f + "" + r;
            alignedSquares.add(square);
        }
        alignedSquares.add(OS_SEPARATOR);
        return alignedSquares;
    }

    public static ArrayList<String> getOctalSquareFor(String square, int knightColor, BoardRegistry br) {
        ArrayList<String> octalSquareList = calculateKingAlignedSquare(square);

        // now add possible Knights jump to the square
        octalSquareList.addAll(possibleKnightTo(square, knightColor, br));

        return octalSquareList;
    }

    // this method just calculate the 8 squares around a center square using geometric
    // calculation
    public static ArrayList<String> octalSquareTo1(String from) {

        // an octal square has  up to 8 squares from current square

        ArrayList<String> octalSquareList = new ArrayList<>();
        String destSquare;

        // top
        destSquare = getSquareAt(from, 0, 1);
        if (destSquare != null) octalSquareList.add(destSquare);

        // top-left
        destSquare = getSquareAt(from, -1, 1);
        if (destSquare != null) octalSquareList.add(destSquare);

        // top-right
        destSquare = getSquareAt(from, 1, 1);
        if (destSquare != null) octalSquareList.add(destSquare);

        // left
        destSquare = getSquareAt(from, -1, 0);
        if (destSquare != null) octalSquareList.add(destSquare);

        // right
        destSquare = getSquareAt(from, 1, 0);
        if (destSquare != null) octalSquareList.add(destSquare);

        // bottom
        destSquare = getSquareAt(from, 0, -1);
        if (destSquare != null) octalSquareList.add(destSquare);

        // bottom-left
        destSquare = getSquareAt(from, -1, -1);
        if (destSquare != null) octalSquareList.add(destSquare);

        // bottom-right
        destSquare = getSquareAt(from, 1, -1);
        if (destSquare != null) octalSquareList.add(destSquare);


        return octalSquareList;
    }

    /*
     * from a given square, by using file step and rank step, this function can calculate chess
     * coordinate in string notation like c4 to g5 where rank step is +1 and file step is +4
     * */
    public static String getSquareAt(String from, int fileStep, int rankStep) {
        char inputFile = Square.getFileAsChar(from);
        char inputRank = Square.getRankAsChar(from);

        char destFile = (char) (inputFile + fileStep);
        char destRank = (char) (inputRank + rankStep);

        boolean destFileRange = destFile >= 'a' && destFile <= 'h';
        boolean destRankRange = destRank >= '1' && destRank <= '8';

        String square = null;
        if (destFileRange && destRankRange)
            square = destFile + "" + destRank;
        return square;
    }

    /**
     * FUNCTION : this method calculates the relationship of two given squares. it supports side-wise
     * association of two squares such as by file & by rank. if it can't identify the
     * relationship then it returns -1.
     */
    public static int associationSideways(String squareA, String squareB) {
        if (Square.getFileAsChar(squareA) - Square.getFileAsChar(squareB) == 0) return BY_FILE;
        if (Square.getRankAsChar(squareA) - Square.getRankAsChar(squareB) == 0) return BY_RANK;
        return -1;
    }

    /**
     * FUNCTION : this method returns number of specified pieces along a side as specified.
     * <p>
     * USAGE : say a knight is on c3, then we can find the number of other knights on the C file
     * and the 3rd rank of an army.
     *
     * @param countDirection the direction either by file or ranks
     * @param commonProperty matches squares with this common property in counting
     */
    public static int countAlong(int countDirection, char commonProperty, ArrayList<String> squares) {
        int count = 0;
        for (String square : squares)
            if (square.charAt(countDirection) == commonProperty) count++;
        return count;
    }

    /**
     * FUNCTION : this method takes two square to compare with each other. it calculates the
     * difference between two squares and return the distinguishing property of the first argument.
     * if there is no difference found then it returns null.
     */
    public static String getUnique(String a, String b) {
        if (Square.getFileAsChar(a) - Square.getFileAsChar(b) != 0)
            return Square.getFileAsString(a);

        if (Square.getRankAsChar(a) - Square.getRankAsChar(b) != 0)
            return Square.getRankAsString(a);

        return null;
    }

}
