package tanzi.staff;

import org.jetbrains.annotations.Nullable;
import tanzi.model.*;
import tanzi.pool.BufferedBRPool;
import tanzi.protocol.BRChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BoardRegistry {

    // board registry change listener
    private BRChangeListener listener;

    // this controls whether changes made to the BR should propagate to the BR change listener
    // it can be useful in cases where you want to make a series of changes to the BR but update
    // the listener(UI) only about the last change.
    private boolean changeReflection = true;

    /**
     * Here enPasser holds the en-passer objects of opposite army. The en-passer previously
     * en-passed it's army.
     * <p>
     * For example, an en-passer object is created when a pawn en-pass the enemy pawn. so here
     * the enPasserForWhite holds the en-passer of black army and vice-versa. In white's next
     * move it can get the en-passer of black and be able to take it down if white wants.
     */
    protected EnPasser enpasserWhite, enpasserBlack;

    // knows which index holds which piece of which color with square name
    protected HashMap<String, Piece> squarePiece;

    // this data-structures holds OS and helpful methods to efficiently deal with King
    protected OctalSquare osWhite;
    protected OctalSquare osBlack;

    public BoardRegistry() {
        initSquarePieceRegistry();
        osWhite = new OctalSquare(Square.forIndex(Square.E1));
        osBlack = new OctalSquare(Square.forIndex(Square.E8));
    }

    /**
     * Returns the number of pieces in the BR.
     */
    public int pieceCount() {
        return squarePiece.size();
    }

    public synchronized void add(Piece piece) {
        String squareKey = piece.currentSquare();
        if (squarePiece.containsKey(squareKey)) return;
        squarePiece.put(squareKey, piece);

        // also let the change listener about this update
        if (listener != null && changeReflection) listener.onPieceAddedToBR(squareKey, Piece.clone(piece));
    }

    public synchronized Piece delete(String square) {
        Piece piece = squarePiece.remove(square);

        // notify the listener about this deletion
        if (listener != null && changeReflection) listener.onPieceDeletedFromBR(square, Piece.clone(piece));

        return piece;
    }

    /**
     * A piece can be moved from one square to another square by first deleting the piece
     * from the BR using the current square and then changing its current square to
     * destination square and finally adding it back to the BR under the key new destination
     * square.
     * <p>
     * This method internally calls {@link BoardRegistry#delete(String)} and
     * {@link BoardRegistry#add(Piece)} methods to do this operation.
     *
     * @return Returns the moved pieces. Null is returned if the BR couldn't delete the piece
     * first. Because an empty piece can't move.
     **/
    public Piece movePiece(String from, String to) {
        Piece piece = delete(from);
        if (piece == null) return null;
        piece.setCurrentSquare(to);
        add(piece);
        return piece;
    }

    /*
     * for a move specified by destination square and army color, this method can tell whether it
     * is killing its own army or not
     * */
    public boolean killingOwnArmy(String square, int ownColor) {
        Piece piece = piece(square);

        // no piece is on the destination square the piece wants to go
        if (piece == null) return false;

        // if there is a piece where the piece wants to go, then see whether it is from his army
        return piece.color == ownColor;
    }

    public BufferedBR copy() {
        // get a fresh copy from the app.pool
        BufferedBR br = BufferedBRPool.get();

        // update properties of new board registry accordingly
        BufferedBR.copySquarePieceMap(squarePiece, br.squarePiece);
        br.osWhite.updateOS(osWhite.getKingSquare());
        br.osBlack.updateOS(osBlack.getKingSquare());

        if (enpasserWhite != null) br.storeEnPasser(enpasserWhite.copy());

        if (enpasserBlack != null) br.storeEnPasser(enpasserBlack.copy());

        return br;
    }

    /*
     * WARNING : SENSITIVE METHOD. YOU KNOW WHAT YOU ARE DOING.
     *
     * this method clears the board registry with no piece records on the board.
     * when this method is invoked then the in-memory game gets reset meaning the
     * Board Registry, Last Move Meta, enPassant etc. get destroyed and reset.
     * */
    public void __clear() {
        squarePiece.clear();
        osWhite.__clear();
        osBlack.__clear();

        enpasserWhite = null;
        enpasserBlack = null;
    }

    public void __clearAndSetup() {
        __clear();
        initSquarePieceRegistry();
        osWhite = new OctalSquare("e1");
        osBlack = new OctalSquare("e8");

        // notify the listener about the board reset
        if (listener != null) listener.onBRClear();
    }

    /**
     * this method updates OS square for the king & keeps in an ArrayList to avoid unnecessary
     * future calculation as king often moves less compared to other piece moves. you should
     * call this method each time there a king move on the chess board.
     *
     * @param color      the king color
     * @param kingSquare the square the king has moved or currently on
     */
    public void updateOSSquare(int color, String kingSquare) {
        OctalSquare octalSquare = (color == Piece.COLOR_WHITE) ? osWhite : osBlack;
        octalSquare.updateOS(kingSquare);
    }

    // this method returns previously calculated king's octal squares for an army
    public ArrayList<String> getOSSquare(int color) {
        OctalSquare octalSquare = (color == Piece.COLOR_WHITE) ? osWhite : osBlack;
        return octalSquare.getOSSquare();
    }

    public String getEnemyKingSquare(int color) {
        int oppositeKingColor = color == Piece.COLOR_WHITE ? Piece.COLOR_BLACK : Piece.COLOR_WHITE;
        Piece oppositeKing = piece(Piece.KING, oppositeKingColor);
        return oppositeKing.currentSquare();
    }

    public String getKingSquare(int color) {
        Piece king = piece(Piece.KING, color);
        return king.currentSquare();
    }

    public boolean anyPieceOn(String squareKey) {
        return squarePiece.containsKey(squareKey);
    }

    /*
     * for a specified army color, it returns the ArrayList containing the squares for all of its
     * army including the King himself.
     * */
    public ArrayList<String> squaresOfArmy(int color) {
        ArrayList<String> squares = new ArrayList<>();
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.color == color) squares.add(piece.currentSquare());
        }
        return squares;
    }

    public Piece piece(String squareKey) {
        return squarePiece.getOrDefault(squareKey, null);
    }

    /**
     * returns a single piece of specified type & color. if there are multiple pieces then it would
     * return the first one it gets.
     */
    public Piece piece(int type, int color) {
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.type == type && piece.color == color) return piece;
        }
        return null;
    }

    /*
     * these methods return the list of pieces as specified by the type, color, file and rank.
     * they will search through the registry to match pieces against the given arguments.
     * */

    public ArrayList<Piece> registeredPiece() {
        ArrayList<Piece> pieceArrayList = new ArrayList<>();
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            pieceArrayList.add(entry.getValue());
        }
        return pieceArrayList;
    }

    public ArrayList<Piece> pieceOf(int type) {
        ArrayList<Piece> list = new ArrayList<>();
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.type == type) list.add(piece);
        }
        return list;
    }

    public ArrayList<Piece> pieceOf(int type, int color) {
        ArrayList<Piece> pieceArrayList = new ArrayList<>();
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.color == color && piece.type == type) pieceArrayList.add(piece);
        }
        return pieceArrayList;
    }

    public ArrayList<Piece> pieceByFile(char file, int type, int color) {
        ArrayList<Piece> pieceArrayList = new ArrayList<>();
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.getFile() == file && piece.color == color && piece.type == type) pieceArrayList.add(piece);
        }
        return pieceArrayList;
    }

    public ArrayList<Piece> pieceByRank(int rank, int type, int color) {
        ArrayList<Piece> pieceArrayList = new ArrayList<>();
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.getRank() == rank && piece.color == color && piece.type == type) pieceArrayList.add(piece);
        }
        return pieceArrayList;
    }

    private void initSquarePieceRegistry() {
        squarePiece = new HashMap<>(64);

        squarePiece.put("a8", new Piece(Piece.ROOK, Piece.COLOR_BLACK, "a8", "a8"));
        squarePiece.put("b8", new Piece(Piece.KNIGHT, Piece.COLOR_BLACK, "b8", "b8"));
        squarePiece.put("c8", new Piece(Piece.BISHOP, Piece.COLOR_BLACK, "c8", "c8"));
        squarePiece.put("d8", new Piece(Piece.QUEEN, Piece.COLOR_BLACK, "d8", "d8"));
        squarePiece.put("e8", new Piece(Piece.KING, Piece.COLOR_BLACK, "e8", "e8"));
        squarePiece.put("f8", new Piece(Piece.BISHOP, Piece.COLOR_BLACK, "f8", "f8"));
        squarePiece.put("g8", new Piece(Piece.KNIGHT, Piece.COLOR_BLACK, "g8", "g8"));
        squarePiece.put("h8", new Piece(Piece.ROOK, Piece.COLOR_BLACK, "h8", "h8"));

        squarePiece.put("a7", new Piece(Piece.PAWN, Piece.COLOR_BLACK, "a7", "a7"));
        squarePiece.put("b7", new Piece(Piece.PAWN, Piece.COLOR_BLACK, "b7", "b7"));
        squarePiece.put("c7", new Piece(Piece.PAWN, Piece.COLOR_BLACK, "c7", "c7"));
        squarePiece.put("d7", new Piece(Piece.PAWN, Piece.COLOR_BLACK, "d7", "d7"));
        squarePiece.put("e7", new Piece(Piece.PAWN, Piece.COLOR_BLACK, "e7", "e7"));
        squarePiece.put("f7", new Piece(Piece.PAWN, Piece.COLOR_BLACK, "f7", "f7"));
        squarePiece.put("g7", new Piece(Piece.PAWN, Piece.COLOR_BLACK, "g7", "g7"));
        squarePiece.put("h7", new Piece(Piece.PAWN, Piece.COLOR_BLACK, "h7", "h7"));

        squarePiece.put("a2", new Piece(Piece.PAWN, Piece.COLOR_WHITE, "a2", "a2"));
        squarePiece.put("b2", new Piece(Piece.PAWN, Piece.COLOR_WHITE, "b2", "b2"));
        squarePiece.put("c2", new Piece(Piece.PAWN, Piece.COLOR_WHITE, "c2", "c2"));
        squarePiece.put("d2", new Piece(Piece.PAWN, Piece.COLOR_WHITE, "d2", "d2"));
        squarePiece.put("e2", new Piece(Piece.PAWN, Piece.COLOR_WHITE, "e2", "e2"));
        squarePiece.put("f2", new Piece(Piece.PAWN, Piece.COLOR_WHITE, "f2", "f2"));
        squarePiece.put("g2", new Piece(Piece.PAWN, Piece.COLOR_WHITE, "g2", "g2"));
        squarePiece.put("h2", new Piece(Piece.PAWN, Piece.COLOR_WHITE, "h2", "h2"));

        squarePiece.put("a1", new Piece(Piece.ROOK, Piece.COLOR_WHITE, "a1", "a1"));
        squarePiece.put("b1", new Piece(Piece.KNIGHT, Piece.COLOR_WHITE, "b1", "b1"));
        squarePiece.put("c1", new Piece(Piece.BISHOP, Piece.COLOR_WHITE, "c1", "c1"));
        squarePiece.put("d1", new Piece(Piece.QUEEN, Piece.COLOR_WHITE, "d1", "d1"));
        squarePiece.put("e1", new Piece(Piece.KING, Piece.COLOR_WHITE, "e1", "e1"));
        squarePiece.put("f1", new Piece(Piece.BISHOP, Piece.COLOR_WHITE, "f1", "f1"));
        squarePiece.put("g1", new Piece(Piece.KNIGHT, Piece.COLOR_WHITE, "g1", "g1"));
        squarePiece.put("h1", new Piece(Piece.ROOK, Piece.COLOR_WHITE, "h1", "h1"));
    }

    /**
     * Here en-passer object is saved for enemy army. Because the enemy can get this as
     * en-passer object and be able to take down this en-passer on their next move.
     */
    public void storeEnPasser(EnPasser enPasser) {
        if (enPasser == null) return;
        if (enPasser.takerColor == Piece.COLOR_BLACK) enpasserBlack = enPasser;
        else enpasserWhite = enPasser;
    }

    public EnPasser restoreEnPasser(int takerColor) {
        if (takerColor == Piece.COLOR_BLACK) return enpasserBlack;
        return enpasserWhite;
    }

    /**
     * Any en-passer(enemy pawn) object which was available for an army to take must be cleared
     * after the move has been made regardless whether the en-passer was captured or not. This
     * method clears en-passer for an army (EnPasser.takerColor) so that no more en-passer remains
     * in the BR for that army in their next moves unless their enemy make another en-passant move.
     */
    public void clearEnPasserFor(int color) {
        if (color == Piece.COLOR_WHITE) enpasserWhite = null;
        else enpasserBlack = null;
    }

    public void setChangeListener(BRChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Any change listener to the BR should only call this method, when there is a series of
     * changes are coming and the listener should only be notified when all the changes have
     * already been made to the BR so that it can avoid unnecessary computation such as UI
     * update.
     * <p>
     * The caller must make sure that it also invokes the resumeReflection() afterwards so that
     * it can be notified of the reflection. Otherwise, no notification is propagated.
     */
    public void pauseReflection() {
        changeReflection = false;
    }

    /**
     * It just simply change the change reflection flag meaning that last changes aren't being notified
     * to the listener thus it just silently resume the reflection which is available on the next change.
     * <p>
     * For immediate reflection, use resumeAndReflect() method.
     */
    public void resumeReflection() {
        changeReflection = true;
    }

    /**
     * When this method gets called, it means that there have been a lot of changes made to the BR thus any
     * dependent on the BR must discard the previous cache and update itself with the fresh state of the BR.
     * That is why this method calls onInvalidation() method on the listener to inform that.
     */
    public void resumeAndReflect() {
        changeReflection = true;
        if (listener != null) listener.onInvalidation();
    }

    public Piece clonedPiece(String square) {
        return Piece.clone(piece(square));
    }

}
