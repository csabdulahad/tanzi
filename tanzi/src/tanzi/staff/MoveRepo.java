package tanzi.staff;

import org.jetbrains.annotations.Nullable;
import tanzi.algorithm.MoveAnalyzer;
import tanzi.algorithm.PGN;
import tanzi.model.MoveMeta;
import tanzi.model.Piece;
import tanzi.protocol.RepoListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * MoveRepo class holds the PGN moves of the game. There are three types of repositories can be created with
 * this MoveRepo class. You can create repository of readonly, growing(with takeBack guard or not) and fresh repo (a repo
 * with no restriction on adding moves, overriding moves).
 * <p>
 * Use MoveRepo.of() method to crate the desired move repository.
 */

public class MoveRepo {

    private RepoListener listener;

    public enum Type {
        READ_ONLY, REPO_GROWING, REPO_GUARDED
    }

    // variables accounted for how repository will behave
    private boolean readOnly, growUp, takeBackGuard;

    /**
     * It is used to run through the move list which is a zero based index
     */
    private int index = -1;

    /**
     * The index up to which, the list it to be kept unchanged. It is
     * also a zero based index.
     */
    private int safeIndex = -1;

    private ArrayList<String> moves;

    private MoveRepo() {
        moves = new ArrayList<>();
    }

    /**
     * This helper MoveRepo builder method creates repository based on the type and moves given as argument.
     * The moves argument can be null. However, it throws exception when the moves are empty/null for
     * read-only and guarded repository.
     * <p>
     * For read-only repo, it returns MoveRepo which is read only meaning no moves can be added or deleted.
     * Thus take backs are not possible. This type MoveRepo can be used when the app will be used as PGN viewer.
     * <p>
     * Fresh MoveRepo can starts from an empty slate which allows growing and take backs to repository. Such
     * MoveRepo is useful for situation when you play against computer or human opponent where take backs are
     * allowed.
     * <p>
     * Guarded MoveRepos permits growing and take backs but can't allow overrides to moves that are passed as
     * initial moves. Such move repository is applicable where the game starts from a position and continues
     * but the starting position can't be altered.
     */
    public static MoveRepo of(Type type, @Nullable String moves) {
        String[] splitMoves = PGN.splitMoves(moves);

        if ((type == Type.REPO_GUARDED || type == Type.READ_ONLY) && splitMoves == null)
            throw new IllegalArgumentException("Can't create a repo of empty/null moves of " + type);

        MoveRepo repo = new MoveRepo();

        if (type == Type.REPO_GUARDED) repo.guarded(splitMoves);
        else if (type == Type.READ_ONLY) repo.readOnly(splitMoves);
        else repo.fresh(splitMoves);

        return repo;
    }

    /**
     * A fresh growing repository can be created with this method.
     * The moves can be null. In case of null, there will be no
     * moves in the repository.
     */
    public static MoveRepo growingRepo(String moves) {
        try {
            return MoveRepo.of(Type.REPO_GROWING, moves);
        } catch (Exception e) {
            return new MoveRepo();
        }
    }

    /**
     * MoveRepo is always initialized with a predefined set of moves. The MoveRepo then manages that repository.
     * However, the repository can be reinitialized with new set of moves which can be very helpful to improve
     * overall performance and provides easier APIs to the application.
     * <p>
     * It notifies the listener about such invalidation, if there is any listener already set.
     */
    public boolean reload(String moves, MoveRepo.Type type) {
        String[] splitMoves = PGN.splitMoves(moves);
        if (splitMoves == null || splitMoves.length < 1) return false;

        // clear the current move list and reset the index to initial position
        this.moves.clear();
        index = -1;

        // also updates the safe index if it is a guarded repo
        if (type == Type.REPO_GUARDED) guarded(splitMoves);
        else if (type == Type.READ_ONLY) readOnly(splitMoves);
        else fresh(splitMoves);

        if (listener != null) listener.onRepoInvalided();

        return true;
    }

    /**
     * It reloads the repository with moves specified by the argument, however, the repo type
     * stays as it is. This internally calls the MoveRepo.reload(moves, type) method to reload
     * the repository.
     */
    public boolean reload(String moves) {
        Type type = type();
        return reload(moves, type);
    }

    /**
     * If the repo is readOnly then, it returns false and doesn't add the move to the repo.
     * <p>
     * For takeBackGuard (doesn't allow to override initial moves), this method further checks if the
     * move requested to be added at current index is in range of guarded moves. if not, then
     * move isn't added and false is returned. If so, then it override the moves at current
     * index and discard others from the current index to the end.
     * <p>
     * Growing up, no guarded repos just simply overrides with the move and discards all other moves
     * to the end.
     */
    public boolean override(String move, int atIndex) {

        // if repo is a readOnly or a not growing up repo then we can't add any move
        if (readOnly || !growUp) return false;

        if (takeBackGuard) {
            /*
             * If the move we are going to add is in guarded moves range then say -1, to not override
             * the repo. Otherwise, increment the index itself to allowing adding at the move
             * */
            atIndex = atIndex <= safeIndex ? -1 : index;
        }

        if (atIndex == -1) return false;

        List<String> temp = moves.subList(0, atIndex);
        if (moves.size() > 0) moves = new ArrayList<>(temp);
        moves.add(move);

        // adjust the index after the override
        index = moves.size() - 1;

        if (listener != null) listener.onRepoMoveOverride(index);

        return true;
    }


    /**
     * It overrides the repo with the given move at the current index position which the move
     * repo is pointing at by calling MoveRepo.override(move, index) method internally. If the
     * current index hasn't moved yet (meaning it is -1) then the overriding get ignored and
     * false is returned. On successful overriding, it returns true.
     * <p>
     * It also returns false if the repo configuration doesn't allow the overriding.
     */
    public boolean override(String move) {
        return override(move, currentIndex());
    }

    /**
     * Any move can be added to the repository if the configuration allows it. However, the internal
     * pointing move index will be left unchanged. It will just add the move at the end of the move
     * list and keep the counter intact. For also updating the index, use the add() method.
     */
    public boolean addAndKeepIndex(String move) {
        // if repo is a readOnly or a not growing up repo then we can't add any move
        if (readOnly || !growUp) return false;

        // add the move at the end of the list
        moves.add(move);

        if (listener != null) listener.onRepoMoveAdded(moves.size() - 1);

        return true;
    }

    /**
     * If repo configuration allows then a move can be added at the end of the move list. Unlike
     * MoveRepo.addAndKeepIndex(), after adding the move, it updates the internal move index to
     * point the very last item on the move list.
     */
    public boolean add(String move) {
        if (!addAndKeepIndex(move)) return false;

        // adjust the index
        index = moves.size() - 1;

        if (listener != null) listener.onRepoMoveAdded(index);

        return true;
    }

    /**
     * This method can tell whose turn it is to play the move based on the position where the board
     * is at by repository. we know the even moves are played by white and odd moves are played by
     * black.
     */
    public int whoseTurn(int index) {
        boolean even = (index % 2 == 0);
        return even ? Piece.COLOR_WHITE : Piece.COLOR_BLACK;
    }

    /**
     * Based on the current pointing index of the repo, it can tell who it is to make the next move.
     * This has the same name as the overloaded method {@link MoveRepo#whoseTurn(int)}, however,
     * different in terms of result they produce.
     * <p>
     * The overloaded method with an index can calculate who it is to make move for that current index.
     */
    public int whoseTurn() {
        return index % 2 == 0 ? Piece.COLOR_BLACK : Piece.COLOR_WHITE;
    }

    /**
     * The pointing index of the move list of this repo can be changed using this method. It checks for
     * the index whether it is in the bound. If not, then it just avoids the index setting.
     */
    public void setIndexAt(int index) {
        if (index < -1 || index >= moveCount()) return;
        this.index = index;
        if (listener != null) listener.onRepoMoveIndexSet(this.index);
    }

    /**
     * It returns the move at the specified index. However, it can return null if the index is not in
     * bound of the move list size.
     */
    public String moveAt(int index) {
        if (index >= moves.size() || index < 0) return null;
        return moves.get(index);
    }

    /**
     * This returns an analyzed MoveMeta of the move specified by the index. If the index is out of the
     * bound of the move list then null is returned.
     */
    public MoveMeta metaAt(int index) {
        String move = moveAt(index);
        if (move == null) return null;
        MoveMeta meta = MoveAnalyzer.analyze(move);
        meta.color = whoseTurn(index);
        meta.moveIndex = index;
        return meta;
    }

    /*
     * These methods shift the index value by one and returns the move at that index. If it reaches at
     * the start or end of the list, then returns null to signify that you can't go any further.
     * */

    public String nextMove() {
        return incrementIndex() != -1 ? moves.get(index) : null;
    }

    public String previousMove() {
        return decrementIndex() != -1 ? moves.get(index) : null;
    }

    public String currentMove() {
        if (moves.size() < 1) return null;
        return moves.get(index);
    }

    public MoveMeta nextMeta() {
        int index = incrementIndex();
        if (index == -1) return null;

        return metaAt(currentIndex());
    }

    public MoveMeta previousMeta() {
        int index = decrementIndex();
        if (index == -1) return null;
        return metaAt(currentIndex());
    }

    public MoveMeta currentMeta() {
        if (index == -1) return null;
        return metaAt(index);
    }

    /**
     * This method returns the repository moves in a single string of PGN form,
     * moves are seperated by commas.
     */
    public String asPGN() {
        StringBuilder buffer = new StringBuilder();
        for (String move : moves) {
            buffer.append(move);
            buffer.append(",");
        }
        // Substring till the last comma exclusive and return it.
        return buffer.substring(0, buffer.length());
    }

    /*
     * We increment or decrement index only if we are in the range of the list to avoid error.
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

    public int moveCount() {
        return moves.size();
    }

    /**
     * Returns the maximum index of the repository, possible for moves.
     * This basically returns the {@link #moveCount()} - 1.
     */
    public int maxIndex() {
        return 0;
    }

    /**
     * It changes the underlying repo configuration but keeps the move list intact.
     */
    public void changeType(Type type) {
        if (type == Type.READ_ONLY) readOnly(null);
        else if (type == Type.REPO_GUARDED) guarded(null);
        else fresh(null);

        if (listener != null) listener.onRepoTypeChanged(type);
    }

    /*
     * These methods change the underlying repository configuration. The moves argument can be null here,
     * cause these methods only get called after the repository has been created with either an initial list
     * of moves for guard and read-only repo and empty/a set of moves for fresh repo. So passing null as moves
     * when changing the repo configuration makes sense here.
     */

    private void readOnly(@Nullable String[] moves) {
        readOnly = true;
        takeBackGuard = false;
        growUp = false;
        safeIndex = -1;
        if (moves != null) this.moves.addAll(Arrays.asList(moves));
    }

    private void guarded(@Nullable String[] moves) {
        takeBackGuard = true;
        readOnly = false;
        growUp = true;

        if (moves != null) this.moves.addAll(Arrays.asList(moves));
        safeIndex = this.moves.size() - 1;
    }

    private void fresh(String[] moves) {
        growUp = true;
        readOnly = false;
        takeBackGuard = false;
        if (moves != null) this.moves.addAll(Arrays.asList(moves));
    }

    public Type type() {
        if (readOnly) return Type.READ_ONLY;
        if (takeBackGuard) return Type.REPO_GUARDED;
        return Type.REPO_GROWING;
    }

    /**
     * Returns iterable of MoveMeta so that the implementation can loop through it more easily.
     */
    public Iterable<MoveMeta> metaIterable() {
        return () -> new Iterator<>() {
            int index = -1;
            final ArrayList<String> list = MoveRepo.this.moves;
            String move = null;

            @Override
            public boolean hasNext() {
                if (list == null || list.isEmpty()) return false;
                try {
                    move = list.get(++index);
                    return true;
                } catch (IndexOutOfBoundsException e) {
                    return false;
                }
            }

            @Override
            public MoveMeta next() {
                return metaAt(index);
            }
        };
    }

    public void setListener(RepoListener listener) {
        this.listener = listener;
    }

    @Override
    public String toString() {
        String[] moves = this.moves.toArray(new String[]{});
        return PGN.format(moves);
    }

}
