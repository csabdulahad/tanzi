package tanzi.staff;

import tanzi.model.Piece;

import java.util.ArrayList;
import java.util.Arrays;

/*
 * MoveRepo class holds the PGN of the game. there are three types of repository can be created with
 * this MoveRepo class. you can create repository of readonly, growing(with takeBack guard or not).
 * there are three static methods are for three types of repositories.
 * */

public class MoveRepo {

    public static final int REPO_READ_ONLY = 624;
    public static final int REPO_GROWING = 343;
    public static final int REPO_GUARDED = 124;

    // variables accounted for how repository will behave
    private boolean readOnly;
    private boolean growUp;
    private boolean takeBackGuard;

    // the index up to which limit, the list it to be kept unchanged
    private int safeIndex = -1;

    // index that is used to run through the move list
    private int index = -1;

    private ArrayList<String> moves;

    private MoveRepo() {
        moves = new ArrayList<>();
    }

    /*
     * it returns MoveRepo which is read only meaning no moves can be added or deleted. thus take
     * backs are not possible. this type MoveRepo can be used when the app will be used as PGN
     * viewer.
     * */
    public static MoveRepo readOnlyRepo(String[] moves) {
        MoveRepo moveRepo = new MoveRepo();
        moveRepo.readOnly = true;
        moveRepo.moves.addAll(Arrays.asList(moves));
        return moveRepo;
    }

    /*
     * the MoveRepo it returns, starts from an empty slate which allows growing and take backs can
     * be made to repository. such MoveRepo is useful for situation when you play against computer
     * or human opponent where take backs are allowed.
     * */
    public static MoveRepo freshRepo() {
        MoveRepo moveRepo = new MoveRepo();
        moveRepo.growUp = true;
        return moveRepo;
    }

    /*
     * MoveRepos that are created by using this constructor permits growing and take backs but
     * can't allow overrides to moves that are passed as initial moves. such move repository is
     * applicable where the game starts from a position and continues but the starting position can't
     * be altered.
     * */
    public static MoveRepo guardedRepo(String[] moves) {
        MoveRepo moveRepo = new MoveRepo();
        moveRepo.takeBackGuard = true;
        moveRepo.growUp = true;
        moveRepo.safeIndex = moves.length - 1;
        moveRepo.moves.addAll(Arrays.asList(moves));
        return moveRepo;
    }

    /*
     * this method works almost like an ordinary stack. if the repo is readOnly then, it returns
     * false and doesn't add the move to the repo.
     *
     * for takeBackGuard(doesn't allow to override initial moves), this method further checks if
     * the move is requested to be added at current index is in range of guarded moves. if so, then
     * move isn't added and returns false. if in range, then it override the moves at current
     * index and discard others from the current index to the end. remember that, the ending value
     * for "subList" is not inclusive.
     *
     * for growing up, no guarded moves, it just simple overrides and discards to the end.
     * */
    public boolean override(String move) {

        // if repo is a readOnly or a not growing up repo then we can't add any move
        if (readOnly || !growUp) return false;

        int startingIndex = index;
        if (takeBackGuard) {
            /*
             * if the move we are going to add is in guarded moves range then say -1 not to override
             * the repo otherwise increment the index itself to allowing adding at the move
             * */
            startingIndex = index < safeIndex ? -1 : index;
        }

        if (startingIndex == -1) return false;

        if (moves.size() > 0) moves = new ArrayList<>(moves.subList(0, startingIndex + 1));
        moves.add(move);

        // adjust the index
        index = moves.size() - 1;

        return true;
    }

    // here keep counter determines whether adding move update the index to the end of
    // the move list or not
    public boolean add(String move, boolean keepCounter) {
        // if repo is a readOnly or a not growing up repo then we can't add any move
        if (readOnly || !growUp) return false;

        // add the move at the end of the list
        moves.add(moves.size(), move);

        // adjust the index
        if (!keepCounter) index = moves.size() - 1;

        return true;
    }

    /*
     * this method can tell whose turn it is to play the move based on the position where the board
     * is at by repository. we know the even moves are played by white and odd moves are played by
     * black.
     * */
    public int whoseTurn() {
        boolean even = (index % 2 == 0);
        return even ? Piece.COLOR_WHITE : Piece.COLOR_BLACK;
    }

    /*
     * these methods move the index value by one and returns the move at that index. if you reach at
     * the start or end of the list, then it returns null to signify that you can't go any further.
     * */

    public String nextMove() {
        return incrementIndex() != -1 ? moves.get(index) : null;
    }

    public String previousMove() {
        return decrementIndex() != -1 ? moves.get(index) : null;
    }


    /*
     * we increment or decrement index only if we are in the range of the list to avoid error. index
     * can only be incremented by add method if the repo configuration permits.
     * */
    private int incrementIndex() {
        return (index + 1 < moves.size()) ? ++index : -1;
    }

    private int decrementIndex() {
        return (index - 1 != -2) ? --index : -1;
    }

    public int currentIndex() {
        return index;
    }

    public String currentMove() {
        if (moves.size() < 1) return null;
        return moves.get(index);
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        for (String move : moves) {
            stringBuilder.append(move);
            stringBuilder.append(" ");
        }

        return stringBuilder.toString();
    }

    public int getRepoType() {
        if (readOnly) return REPO_READ_ONLY;
        if (takeBackGuard) return REPO_GUARDED;
        return REPO_GROWING;
    }

    // TODO: 27/06/2020 - implement iterator pattern

    public boolean hasNext() {
        return (index + 1 < moves.size());
    }

    public void resetIndex(int index) {
        this.index = index;
    }


    public static final int EFFECT_OVERRIDE = 231;
    public static final int EFFECT_ADD = 111;
    public static final int EFFECT_VOID = 999;

    public int isOverriding() {

        if (takeBackGuard && index < safeIndex) return EFFECT_VOID;

        boolean result = index >= safeIndex && index < (moves.size() - 1);
        return result ? EFFECT_OVERRIDE : EFFECT_ADD;
    }

    public int moveCount() {
        return moves.size();
    }

}
