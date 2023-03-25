package tanzi.app;

import org.jetbrains.annotations.Nullable;
import tanzi.algorithm.*;
import tanzi.gridman.GPoint;
import tanzi.model.BRHistory;
import tanzi.model.MoveMeta;
import tanzi.model.Piece;
import tanzi.model.Square;
import tanzi.protocol.BRChangeListener;
import tanzi.protocol.PromotionHandler;
import tanzi.staff.BRHistorian;
import tanzi.staff.BoardRegistry;
import tanzi.staff.MoveRepo;

import java.util.ArrayList;
import java.util.List;

/**
 * Game class is an implementation of Tanzi chess parser engine. It facilitates many aspect of
 * parsing a chess game, managing various entities such as BoardRegistry (in-memory chess board),
 * MoveRepo (move list of the game), BRHistorian (changes to BR, undo/redo access), GameSound etc.
 * easily.
 * <p>
 * For creating a game instance, there is a helper builder class exists. The builder helps create
 * game with various configurations, listeners etc. In order to get a builder, app should call
 * {@link #ofMoves(String)} method.
 */

public class Game {

    public enum State {
        CHECKMATE,
        GAME_CONTINUES,
        EXCEPTION,
        PGN_BEGINNING, PGN_END, STALEMATE
    }

    private State gameState;

    private Exception exception;

    private Board board = null;

    private final BoardRegistry br;

    private final MoveRepo repo;

    private final BRHistorian historian;

    private final GameSound gameSound;

    private boolean muteSound;

    private StateListener stateListener;

    private LastMoveListener lastMoveListener;

    private BoardOrientationListener orientationListener;

    public Game(String moves, MoveRepo.Type repoType, double boardSize) {
        br = new BoardRegistry();

        repo = MoveRepo.of(repoType, moves);
        historian = new BRHistorian(repo, br);

        gameSound = new GameSound(null);

        if (Double.compare(boardSize, 0) > 0) board = new Board(boardSize);
    }

    /**
     * This creates a game object with no moves and the repo type REPO_GROWING.
     * The board size is set to 0 as there will be no visual consumer empty game
     * doesn't bind itself to any GUI thread.
     */
    public static Game createEmptyGame() {
        return new Game(null, MoveRepo.Type.REPO_GROWING, 0);
    }

    public void setBoardOrientation(Board.Orientation orientation) {
        if (board != null) board.setOrientation(orientation);
        if (orientationListener != null) orientationListener.onOrientationChange(orientation);
    }

    public void setGameSoundPlayer(SoundPlayer player) {
        gameSound.setPlayer(player);
    }

    public void setStateListener(StateListener listener) {
        this.stateListener = listener;
    }

    public void setMoveFocusListener(LastMoveListener listener) {
        this.lastMoveListener = listener;
    }

    public void setBRChangeListener(BRChangeListener listener) {
        br.setChangeListener(listener);
    }

    public void setBoardOrientationListener(BoardOrientationListener listener) {
        this.orientationListener = listener;
    }

    /**
     * Navigate from the current move repo index to the next available move.
     *
     * @return MoveMeta on successfully navigating to the next move. Null on
     * failure.
     */
    public boolean nextMove() {
        return gotoMove(repo.currentIndex() + 1);
    }

    /**
     * Navigate from the current move repo index to the previous available move.
     *
     * @return MoveMeta on successfully navigating to the previous move. Null on
     * failure.
     */
    public boolean previousMove() {
        return gotoMove(repo.currentIndex() - 1);
    }

    /**
     * A game can be navigated to any random position using the move index. This method
     * firstly checks whether the index is inbound with the MoveRepo and whether it is
     * already at the requested index. If it is out of repo bound, then false is returned
     * and the move meta at current move repo index is returned if it is already at the
     * index.
     * <p>
     * It internally calls {@link BRHistorian#goTo(int)} to navigate to the index which
     * rollbacks on any failure to the previous index. The BR reflection is paused if it
     * can detect that there are going to be more than one change in the BR with this move
     * navigation.
     * <p>
     * This also take sound playing and last move change highlighting into account.
     * Appropriate listeners are notified based on the configuration and method outcome.
     */
    public boolean gotoMove(int index) {
        // make sure that index is within the repository range
        if (index >= repo.moveCount() || index < -1) {
            playGameSound(null);
            return false;
        }

        int repoAtIndex = repo.currentIndex();

        // make sure we are not already on the spot
        if (index == repoAtIndex) {
            MoveMeta meta = repo.metaAt(index);

            // set state based on whether we have 'next' available in the repo after the index
            updateGameStatus(meta);

            playGameSound(meta);
            return true;
        }

        // here we know that we need to navigate

        // calculate the num of move required to meet the index
        int diff = repoAtIndex - index;


        // handle the exception if it would return any, set proper game state then rethrow it
        MoveMeta meta = null;
        try {
            // pause the BR change reflection if we are going to make a series of changes
            boolean resumeAndReflect = Math.abs(diff) > 1;
            if (resumeAndReflect) br.pauseReflection();

            meta = historian.goTo(index) ? repo.metaAt(index) : null;
            if (meta == null) throw new Exception("Failed to make move for index " + index);
            updateGameStatus(meta);

            if (resumeAndReflect) br.resumeAndReflect();
        } catch (Exception e) {
            exception = e;
            gameState = State.EXCEPTION;
            if (stateListener != null) stateListener.onGameStateChange(gameState);

            br.resumeReflection();
        }

        playGameSound(meta);

        // let the last move change listener about this move navigation
        if (meta != null && lastMoveListener != null) {
            GPoint[] moveChangeIndex = lastMoveChangeIndexes();
            if (moveChangeIndex.length == 2)
                lastMoveListener.onLastMoveFocused(moveChangeIndex[0], moveChangeIndex[1]);
        }

        return meta != null;
    }

    /**
     * If the move repository allows the move to be played then when a player plays a move,
     * it has to go through a lot of algorithms to make sure that the move is legitimate to
     * play and compliant with all the chess rules. This method first translate the coordinates
     * as per board orientation, and it is the piece of right color making the move. Then it
     * calls {@link PGN#translate(int, int, BoardRegistry, PromotionHandler)} on those points
     * with passed-in promotion handler to calculate the move in PGN.
     * <p>
     * After successfully calculating the move, it then calculates the BRHistory objects
     * for executing the move into the BR. If this history calculation goes all well, only
     * then the whole move gets executed into the BR. It also checks for stalemate status.
     * <p>
     * All types of listeners such BRChangeListener, SoundPlayer etc. gets called in order.
     * The game status is calculated at the end of this method to notify listener about the
     * state the game has gone to now.
     */
    public boolean play(GPoint from, GPoint to, PromotionHandler promoHandler) {

        // Get the indexes where the move is making from and to.
        int fromIndex = board.index(from.x, from.y);
        int toIndex = board.index(to.x, to.y);

        // Let's see whether it is the piece of right color making the move.
        Piece piece = br.piece(Square.forIndex(fromIndex));
        if (piece == null || piece.color != repo.whoseTurn()) return false;

        String move = PGN.translate(fromIndex, toIndex, br, promoHandler);
        if (move == null || !repo.add(move)) return false;

        MoveMeta meta = repo.currentMeta();
        BRHistory history = MoveMaker.move(meta, br);

        if (history == null) return false;
        history.saveAndExecute(historian, br);

        updateGameStatus(meta);
        playGameSound(meta);

        return true;
    }

    /**
     * Last move change can be highlighted using two GPoint, one is from square
     * and the other is to square. The MoveMeta can tell where the move went to
     * (dest square) and then the BR can tell us where the piece(on the  dest square
     * after the move made) came from.
     *
     * @return Array of {@link GPoint}s which tell the starting xy coordinates of the
     * last-move-change squares. Empty array is returned if there is no meta found for
     * the current move index of the move repo.
     */
    public GPoint[] lastMoveChangeIndexes() {

        MoveMeta meta = repo.metaAt(repo.currentIndex());
        if (meta == null) return new GPoint[]{};

        String a, b;
        // handle for castle move
        if (meta.castle) {
            String[] castleMeta = King.getCastleMeta(meta);
            a = castleMeta[0];
            b = castleMeta[2];
        } else {
            // this handles for simple, take, promotion move
            a = meta.destSquare;
            b = br.piece(a).previousSquare();
        }

        return new GPoint[]{board.gpoint(a), board.gpoint(b)};
    }

    /**
     * When this method is invoked then the in-memory game is reset to the initial position
     * meaning the BoardRegistry, last MoveMeta, enPassant etc. get destroyed and reset.
     * The associated MoveRepo then gets reloaded with the specified moves and repo type and
     * BRHistorian in turn gets request to clear its histories by the repo reloading.
     * <p>
     * On successful reloading, it returns true, false otherwise.
     */
    public boolean reload(String moves, MoveRepo.Type repoType) {
        br.__clearAndSetup();
        return repo.reload(moves, repoType);
    }

    /**
     * Call to this method resets the whole game including BRHistorian, various BR states,
     * with the moves specified in the argument. This internally calls the Game.reload()
     * with repo type of the current game.
     * <p>
     * On successful reloading, it returns true, false otherwise.
     */
    public boolean reload(String moves) {
        return reload(moves, repo.type());
    }

    /**
     * Any implementation of Tanzi will require to draw BR registered pieces onto the canvas.
     * This method gets the registered pieces from the BR and after calculating their xy
     * coordinates based on the board orientation configuration, associated PieceDrawables of
     * platform dependent type graphics context (G) and bitmap (B) are crated.These PieceDrawables
     * are then returned as a list. This hides the references to the actual pieces in the BR and
     * saves them from any accidental changes from outside the engine.
     * <p>
     * The PieceBitmapPool and BitmapPainter are necessary as they know how to create a bitmap for
     * specific piece and how that bitmap can actually be painted.
     *
     * @param <G>             The graphics context for the canvas.
     * @param <B>             The bitmap representing the piece to be drawn on the canvas.
     * @param pieceBitmapPool The pool knows how to produce a platform dependent bitmap type according to
     *                        the piece type and color.
     * @param bitmapPainter   The implementation of BitmapPainter interface which knows how the bitmap is
     *                        painted onto the canvas using platform dependent graphics context and bitmap
     *                        type.
     */
    public <G, B> List<PieceDrawable<G, B>> drawablePieceList(PieceBitmapPool<B> pieceBitmapPool, BitmapPainter<G, B> bitmapPainter) {

        List<Piece> list = br.registeredPiece();

        List<PieceDrawable<G, B>> sduList = new ArrayList<>();
        for (Piece piece : list) {

            GPoint current = board.gpoint(piece.currentSquare());
            GPoint previous = board.gpoint(piece.previousSquare());

            PieceDrawable<G, B> sdu = new PieceDrawable<>(
                    current.x, current.y, previous.x, previous.y,
                    pieceBitmapPool.get(piece.type, piece.color),
                    bitmapPainter
            );
            sduList.add(sdu);
        }
        return sduList;
    }

    /**
     * For a click event on a piece, we get xy coordinates which can be used to identify the
     * square the click was performed on. Then the piece can be fetched from the BR to calculate
     * whether it is to make move by {@link MoveRepo#currentIndex()} and to calculate the possible
     * square list in {@link CircleDrawable} from which the piece is allowed to move to.
     *
     * @param x   X coordinate of the click event.
     * @param y   Y coordinate of the click event.
     * @param <G> The graphics context for the canvas.
     * @return It returns a list of {@code CircularDrawable<G>} which can be used by GUI thread to
     * print circles on the board to show the player where the piece can make move next. Null is
     * returned if it not the clicked piece to make move by the MoveRepo or there was no piece when
     * the click was performed.
     */
    public <G> List<CircleDrawable<G>> drawableSquaresToMove(double x, double y, Painter<G> painter) {
        int index = board.index(x, y);
        String square = Square.forIndex(index);
        Piece piece = br.piece(square);
        if (piece == null || piece.color != repo.whoseTurn()) return null;

        // get the list of squares where the piece can go to and check if it is null/empty
        List<String> squares = Arbiter.possibleSquareFor(square, br);
        if (squares == null || squares.isEmpty()) return null;

        List<CircleDrawable<G>> list = new ArrayList<>();
        for (String s : squares) {
            GPoint point = board.gpoint(s);
            list.add(new CircleDrawable<>(point.x, point.y, painter));
        }
        return list;
    }

    /**
     * This method returns the game's move repository as numbered move PGN format.
     * Internally it fist calls the {@link MoveRepo#asPGN()} and then calls
     * {@link PGN#format(String)} on it to build that PGN.
     */
    public String exportToPGN() {
        return PGN.format(repo.asPGN());
    }

    /**
     * Helper method for building a game object with a predefined set of moves.
     * The moves can be null if the game is starting from the beginning. However,
     * the repo type must be {@code MoveRepo.Type.REPO_GROWING} as it is only
     * permitted for moves to be null if it is a growing repo. Otherwise, an
     * exception will be thrown.
     *
     * @param moves The game will be of. It can be null, if an empty game is
     *              required.
     * @return It returns the {@link Game.Builder} with the moves.
     */
    public static Builder ofMoves(@Nullable String moves) {
        return new Builder(moves);
    }

    public State state() {
        return gameState;
    }

    public MoveRepo repo() {
        return repo;
    }

    /**
     * Returns the game's BoardRegistry. Any direct changes to the BR is not
     * encouraged. Modification should happen through the game class and
     * the Tanzi engine only. However, a caution should be carried out when
     * any alteration is made to the BR.
     */
    public BoardRegistry br() {
        return br;
    }

    /**
     * Returns the last exception, if happened by any of the game's method.
     * Null can be thrown if there didn't happen any already.
     */
    public Exception exception() {
        return exception;
    }

    /**
     * Returns the last exception message, if there is any. Otherwise, an
     * empty string is returned.
     */
    public String excepMsg() {
        if (exception == null) return "";
        return exception.getMessage();
    }

    public void muteSound() {
        this.muteSound = true;
    }

    public void unmuteSound() {
        this.muteSound = false;
    }

    private void playGameSound(MoveMeta meta) {
        if (muteSound || gameSound == null) return;
        gameSound.metaToSound(meta);
    }

    private void updateGameStatus(MoveMeta meta) {
        int repoIndex = repo.currentIndex();

        if (meta.checkMate) gameState = State.CHECKMATE;
        else if (repoIndex == -1) gameState = State.PGN_BEGINNING;
        else if (repoIndex == repo.moveCount() - 1) gameState = State.PGN_END;
        else if (StaleMate.isStaleMate(repo.whoseTurn(), br)) gameState = State.STALEMATE;
        else gameState = State.GAME_CONTINUES;

        if (stateListener != null) stateListener.onGameStateChange(gameState);
    }

    public interface StateListener {
        void onGameStateChange(State gameState);
    }

    /**
     * Helper class for building a Tanzi game with various configurations and listeners
     * such as game state listener, BRChangeListener, SoundPlayer etc.
     */
    public static class Builder {
        private final String moves;
        private MoveRepo.Type type = MoveRepo.Type.REPO_GROWING;
        private StateListener stateListener;
        private SoundPlayer soundPlayer;
        private LastMoveListener lastMoveListener;
        private BRChangeListener brChangeListener;
        private BoardOrientationListener boardOrientationListener;
        private Board.Orientation orientation;

        private Builder(String moves) {
            this.moves = moves;
        }

        /**
         * Build the game with the specified repository type. If this is not
         * configured during building then {@code MoveRepo.Type.REPO_GROWING}
         * is the default repo type.
         */
        public Builder repoType(MoveRepo.Type type) {
            this.type = type;
            return this;
        }

        /**
         * Add StateListener to the game. The listener gets notified of state
         * changes after every move is executed into the BR.
         */
        public Builder stateListener(StateListener listener) {
            this.stateListener = listener;
            return this;
        }

        /**
         * Each move action can be output as sound. It sets a sound playing
         * implementation to the game so that after every move, appropriate
         * sound can be played.
         */
        public Builder gameSoundPlayer(SoundPlayer player) {
            this.soundPlayer = player;
            return this;
        }

        /**
         * The GUI implementation is allowed to listen for changes as they happen
         * on the board so that the move change can be highlighted to the player.
         */
        public Builder lastMoveFocusListener(LastMoveListener listener) {
            this.lastMoveListener = listener;
            return this;
        }

        /**
         * On each move played, any changes in the in-memory BR should be reflected
         * in the GUI of the game. It sets that GUI part to the game who is responsible
         * for drawing the pieces on the screen to the game.
         */
        public Builder brChangeListener(BRChangeListener listener) {
            this.brChangeListener = listener;
            return this;
        }

        /**
         * The game can be built with the specified orientation either form
         * white or black's perspective. It is helpful for cases where the
         * user has a preference for the board orientation always to be in
         * one configuration.
         */
        public Builder orientation(Board.Orientation orientation) {
            this.orientation = orientation;
            return this;
        }

        /**
         * Builds and returns the game object as it has been configured by this
         * builder with a graphical board size. The board size is necessary
         * when the game is going to allow player interactions via GUI.
         */
        public Game create(double boardSize) {
            Game game = new Game(moves, type, boardSize);
            game.setBoardOrientation(orientation);
            game.setBoardOrientationListener(boardOrientationListener);
            game.setBRChangeListener(brChangeListener);
            game.setStateListener(stateListener);
            game.setGameSoundPlayer(soundPlayer);
            game.setMoveFocusListener(lastMoveListener);
            return game;
        }

        /**
         * Builds and returns the game object as it has been configured by this
         * builder without a graphical board size. This type of building is helpful
         * when the game is built for testing or debugging.
         */
        public Game create() {
            return create(0);
        }

    }

}
