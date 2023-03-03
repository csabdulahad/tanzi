package tanzi.staff;

import tanzi.model.MoveChange;
import tanzi.model.OctalSquare;
import tanzi.model.Piece;
import tanzi.model.Square;
import tanzi.pool.BufferedBRPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BoardRegistry {

    // board registry change listener
    private ChangeListener changeListener;

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

    // methods that make changes to squarePiece registry

    public synchronized Piece deleteEntry(String square) {
        Piece piece = squarePiece.remove(square);

        // notify the listener about this deletion
        if (changeListener != null) changeListener.onDeletePiece(square, piece);

        return piece;
    }

    public synchronized void addEntry(Piece piece) {
        String squareKey = piece.getCurrentSquare();
        if (squarePiece.containsKey(squareKey)) return;
        squarePiece.put(squareKey, piece);

        // also let the change listener about this update
        if (changeListener != null) changeListener.onAddPiece(piece);
    }

    public boolean takeBack(MoveChange[] moveChanges) {
        if (moveChanges.length < 1) return false;

        alterMove(moveChanges[0]);
        if (moveChanges[0].tail) {
            // binary Change as two piece were involved in the last move
            alterMove(moveChanges[1]);
        }

        return true;
    }

    private void alterMove(MoveChange moveChange) {
        // undo the change in the BR
        deleteEntry(moveChange.newSquare);
        addEntry(moveChange.previousPiece);
    }

    /*
     * for a move specified by destination square and army color, this method can tell whether it
     * is killing its own army or not
     * */
    public boolean killingOwnArmy(String square, int ownColor) {
        Piece piece = getPiece(square);

        // no piece is on the destination square the piece wants to go
        if (piece == null) return false;

        // if there is a piece where the piece wants to go, then see whether it is from his army
        return piece.color == ownColor;
    }

    public BufferedBR getCopy() {
        // get a fresh copy from the app.pool
        BufferedBR br = BufferedBRPool.getInstance().get();

        // update properties of new board registry accordingly
        BufferedBR.copySquarePieceMap(squarePiece, br.squarePiece);
        br.osWhite.updateOS(osWhite.getKingSquare());
        br.osBlack.updateOS(osBlack.getKingSquare());

        return br;
    }

    /*
     * WARNING : SENSITIVE METHOD. YOU KNOW WHAT YOU ARE DOING.
     *
     * this method clears the board registry with no piece records on the board.
     * */
    public void __clear() {
        squarePiece.clear();
        osWhite.__clear();
        osBlack.__clear();
    }

    public void __clearAndSetup() {
        __clear();
        initSquarePieceRegistry();
        osWhite = new OctalSquare("e1");
        osBlack = new OctalSquare("e8");

        // notify the listener about the board reset
        if (changeListener != null) changeListener.onBRClear();
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

    public String getOppositeKingSquare(int color) {
        int oppositeKingColor = color == Piece.COLOR_WHITE ? Piece.COLOR_BLACK : Piece.COLOR_WHITE;
        Piece oppositeKing = getPiece(Piece.PIECE_KING, oppositeKingColor);
        return oppositeKing.getCurrentSquare();
    }

    public String getKingSquare(int color) {
        Piece king = getPiece(Piece.PIECE_KING, color);
        return king.getCurrentSquare();
    }

    public boolean anyPieceOn(String squareKey) {
        return squarePiece.containsKey(squareKey);
    }

    /*
     * for a specified army color, it returns the ArrayList containing the squares for all of its
     * army including the King himself.
     * */
    public ArrayList<String> getSquareListForArmyOf(int color) {
        ArrayList<String> squares = new ArrayList<>();
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.color == color) squares.add(piece.getCurrentSquare());
        }
        return squares;
    }

    public Piece getPiece(String squareKey) {
        return squarePiece.getOrDefault(squareKey, null);
    }

    /**
     * returns a single piece of specified type & color. if there are multiple pieces then it would
     * return the first one it gets.
     */
    public Piece getPiece(int type, int color) {
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

    public ArrayList<Piece> getRegisteredPiece() {
        ArrayList<Piece> pieceArrayList = new ArrayList<>();
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            pieceArrayList.add(entry.getValue());
        }
        return pieceArrayList;
    }

    public ArrayList<Piece> getPieceByType(int type, int color) {
        ArrayList<Piece> pieceArrayList = new ArrayList<>();
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.color == color && piece.type == type) pieceArrayList.add(piece);
        }
        return pieceArrayList;
    }

    public ArrayList<Piece> getPieceByFile(char file, int type, int color) {
        ArrayList<Piece> pieceArrayList = new ArrayList<>();
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.getFile() == file && piece.color == color && piece.type == type)
                pieceArrayList.add(piece);
        }
        return pieceArrayList;
    }

    public ArrayList<Piece> getPieceByRank(int rank, int type, int color) {
        ArrayList<Piece> pieceArrayList = new ArrayList<>();
        for (Map.Entry<String, Piece> entry : squarePiece.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.getRank() == rank && piece.color == color && piece.type == type)
                pieceArrayList.add(piece);
        }
        return pieceArrayList;
    }

    private void initSquarePieceRegistry() {
        squarePiece = new HashMap<>(64);

        squarePiece.put("a8", new Piece(Piece.PIECE_ROOK, Piece.COLOR_BLACK, "a8", "a8"));
        squarePiece.put("b8", new Piece(Piece.PIECE_KNIGHT, Piece.COLOR_BLACK, "b8", "b8"));
        squarePiece.put("c8", new Piece(Piece.PIECE_BISHOP, Piece.COLOR_BLACK, "c8", "c8"));
        squarePiece.put("d8", new Piece(Piece.PIECE_QUEEN, Piece.COLOR_BLACK, "d8", "d8"));
        squarePiece.put("e8", new Piece(Piece.PIECE_KING, Piece.COLOR_BLACK, "e8", "e8"));
        squarePiece.put("f8", new Piece(Piece.PIECE_BISHOP, Piece.COLOR_BLACK, "f8", "f8"));
        squarePiece.put("g8", new Piece(Piece.PIECE_KNIGHT, Piece.COLOR_BLACK, "g8", "g8"));
        squarePiece.put("h8", new Piece(Piece.PIECE_ROOK, Piece.COLOR_BLACK, "h8", "h8"));

        squarePiece.put("a7", new Piece(Piece.PIECE_PAWN, Piece.COLOR_BLACK, "a7", "a7"));
        squarePiece.put("b7", new Piece(Piece.PIECE_PAWN, Piece.COLOR_BLACK, "b7", "b7"));
        squarePiece.put("c7", new Piece(Piece.PIECE_PAWN, Piece.COLOR_BLACK, "c7", "c7"));
        squarePiece.put("d7", new Piece(Piece.PIECE_PAWN, Piece.COLOR_BLACK, "d7", "d7"));
        squarePiece.put("e7", new Piece(Piece.PIECE_PAWN, Piece.COLOR_BLACK, "e7", "e7"));
        squarePiece.put("f7", new Piece(Piece.PIECE_PAWN, Piece.COLOR_BLACK, "f7", "f7"));
        squarePiece.put("g7", new Piece(Piece.PIECE_PAWN, Piece.COLOR_BLACK, "g7", "g7"));
        squarePiece.put("h7", new Piece(Piece.PIECE_PAWN, Piece.COLOR_BLACK, "h7", "h7"));

        squarePiece.put("a2", new Piece(Piece.PIECE_PAWN, Piece.COLOR_WHITE, "a2", "a2"));
        squarePiece.put("b2", new Piece(Piece.PIECE_PAWN, Piece.COLOR_WHITE, "b2", "b2"));
        squarePiece.put("c2", new Piece(Piece.PIECE_PAWN, Piece.COLOR_WHITE, "c2", "c2"));
        squarePiece.put("d2", new Piece(Piece.PIECE_PAWN, Piece.COLOR_WHITE, "d2", "d2"));
        squarePiece.put("e2", new Piece(Piece.PIECE_PAWN, Piece.COLOR_WHITE, "e2", "e2"));
        squarePiece.put("f2", new Piece(Piece.PIECE_PAWN, Piece.COLOR_WHITE, "f2", "f2"));
        squarePiece.put("g2", new Piece(Piece.PIECE_PAWN, Piece.COLOR_WHITE, "g2", "g2"));
        squarePiece.put("h2", new Piece(Piece.PIECE_PAWN, Piece.COLOR_WHITE, "h2", "h2"));

        squarePiece.put("a1", new Piece(Piece.PIECE_ROOK, Piece.COLOR_WHITE, "a1", "a1"));
        squarePiece.put("b1", new Piece(Piece.PIECE_KNIGHT, Piece.COLOR_WHITE, "b1", "b1"));
        squarePiece.put("c1", new Piece(Piece.PIECE_BISHOP, Piece.COLOR_WHITE, "c1", "c1"));
        squarePiece.put("d1", new Piece(Piece.PIECE_QUEEN, Piece.COLOR_WHITE, "d1", "d1"));
        squarePiece.put("e1", new Piece(Piece.PIECE_KING, Piece.COLOR_WHITE, "e1", "e1"));
        squarePiece.put("f1", new Piece(Piece.PIECE_BISHOP, Piece.COLOR_WHITE, "f1", "f1"));
        squarePiece.put("g1", new Piece(Piece.PIECE_KNIGHT, Piece.COLOR_WHITE, "g1", "g1"));
        squarePiece.put("h1", new Piece(Piece.PIECE_ROOK, Piece.COLOR_WHITE, "h1", "h1"));
    }

    public Piece movePiece(String from, String to) {
        Piece piece = deleteEntry(from);
        piece.setCurrentSquare(to);
        addEntry(piece);
        return piece;
    }

    public void setChangeListener(ChangeListener listener) {
        this.changeListener = listener;
    }

    public interface ChangeListener {
        void onDeletePiece(String square, Piece piece);

        void onAddPiece(Piece piece);

        void onBRClear();
    }

}
