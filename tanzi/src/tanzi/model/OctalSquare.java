package tanzi.model;

import tanzi.staff.GeometryEngineer;

import java.util.ArrayList;

/*
 * the squares that are aligned with the king square are very crucial in order to detect pin,
 * discover attack, check, checkmate, illegal moves and so on. this class abstracts those functions
 * to provide more clear understanding and clarity. since around a piece there are eight piece
 * hence it gets the name OctalSquare, and it is also known as 'OS' within the project.
 *
 * this OS is also helpful for other piece. we may find situation where we want to see if a
 * piece can be attacked from the Eight-Squares.
 *
 * when you create an instance of this class, it calculates OS for the square you pass in the
 * constructor eagerly. for king, each time the king moves, this method subsequently needs to be
 * called. we are using greedy algorithm to avoid calculation over & over for the king after
 * each move on the board to calculate pin, discover attack etc.
 * */

public class OctalSquare {

    /*
     * hold the list of squares that are aligned with the king. these squares are particularly
     * crucial in detecting pin, discover attack, check & checkmate
     * */
    private ArrayList<String> kingAlignedSquare;

    private String kingSquare;

    public OctalSquare(String kingSquare) {
        this.kingSquare = kingSquare;
        updateOS(kingSquare);
    }

    // this method returns previously calculated king aligned squares of an army's king
    public ArrayList<String> getOSSquare() {
        return kingAlignedSquare;
    }

    /**
     * it discards others squares from king aligned squares by fitting the given square into king
     * aligned squares along a direction.
     * <p>
     * USAGE : you should use this function when you want to validate for discover attack, check,
     * pin etc. as king aligned squares are to be checked for such mentioned situation in chess
     * board.
     *
     * @param fittingSquare the square that is to be found in the king aligned square list to
     *                      discard other unnecessary king aligned squares for performance
     */
    public ArrayList<String> getFilteredKAS(String fittingSquare) {

        /*
         * we know that aligned squares are separated & enclosed by '$' value. so get the index
         * of the square which fits in the group. from there we will use backward and forward
         * indexing to calculate aligned squares by the given square
         * */

        int piecePos = kingAlignedSquare.indexOf(fittingSquare);
        if (piecePos == -1) return null;

        ArrayList<String> alignedSquareList = new ArrayList<>();

        // find squares up to null point backward from the piecePos
        for (int i = piecePos - 1; i > 0; i--) {
            String square = kingAlignedSquare.get(i);

            // if we reach the starting point of the desired section/segment
            if (square.equals(GeometryEngineer.OS_SEPARATOR)) break;

            alignedSquareList.add(square);
        }

        // find squares up to null point forward from the piecePos
        for (int i = piecePos + 1; i < kingAlignedSquare.size(); i++) {
            String square = kingAlignedSquare.get(i);

            // if we reach the starting point of the desired section/segment
            if (square.equals(GeometryEngineer.OS_SEPARATOR)) break;

            alignedSquareList.add(square);
        }

        return alignedSquareList;
    }

    /**
     * this method updates king aligned squares & keeps in a ArrayList to avoid unnecessary future
     * calculation as king often moves less compared to piece moves. you should call this method
     * each time there a king moves on the chess board.
     *
     * @param kingSquare the square the king has moved or currently on
     */
    public void updateOS(String kingSquare) {
        this.kingSquare = kingSquare;
        kingAlignedSquare = GeometryEngineer.calculateKingAlignedSquare(kingSquare);
    }

    public void __clear() {
        kingAlignedSquare.clear();
    }

    public String getKingSquare() {
        return kingSquare;
    }

}
