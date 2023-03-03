package tanzi.model;

/*
 * The data structure for representing a piece. A fully described piece including its position on the
 * board, previous square, whether it has moved or not and so on.
 *
 * For determining square index, it uses BoardRegistry class to find it. There is a static app.database.helper
 * method to clone itself.
 * */

import tanzi.pool.PiecePool;

public class Piece {

    public static final int COLOR_WHITE = 0;
    public static final int COLOR_BLACK = 1;

    public static final int PIECE_KING = 0;
    public static final int PIECE_QUEEN = 1;
    public static final int PIECE_ROOK = 2;
    public static final int PIECE_BISHOP = 3;
    public static final int PIECE_KNIGHT = 4;
    public static final int PIECE_PAWN = 5;

    public int color = -1;
    public int type = -1;
    private String currentSquare = null;
    private String previousSquare = null;
    private boolean hasMoved = false;

    public Piece() {

    }

    public Piece(int type, int color, String currentSquare, String previousSquare) {
        this.type = type;
        this.color = color;
        this.currentSquare = currentSquare;
        this.previousSquare = previousSquare;
    }

    public static Piece clone(Piece piece) {
        // get a piece from piece app.pool which will clone the passed piece
        Piece clonedPiece = PiecePool.getInstance().get();

        // set values
        clonedPiece.type = piece.type;
        clonedPiece.color = piece.color;
        clonedPiece.currentSquare = piece.currentSquare;
        clonedPiece.previousSquare = piece.previousSquare;
        clonedPiece.hasMoved = piece.hasMoved;

        return clonedPiece;
    }

    public static int getOppositeColor(int color) {
        return color == COLOR_WHITE ? COLOR_BLACK : COLOR_WHITE;
    }

    public void setCurrentSquare(String currentSquare) {
        this.previousSquare = this.currentSquare;
        this.currentSquare = currentSquare;
        hasMoved = true;
    }

    public void setPreviousSquare(String previousSquare) {
        this.previousSquare = previousSquare;
    }

    public String getCurrentSquare() {
        return currentSquare;
    }

    public String getPreviousSquare() {
        return previousSquare == null ? currentSquare : previousSquare;
    }

    public int getRank() {
        return Square.getRankAsInt(currentSquare);
    }

    public char getFile() {
        return Square.getFileAsChar(currentSquare);
    }

    public static int getType(char piece) {
        return switch (piece) {
            case 'K' -> PIECE_KING;
            case 'Q' -> PIECE_QUEEN;
            case 'R' -> PIECE_ROOK;
            case 'B' -> PIECE_BISHOP;
            case 'N' -> PIECE_KNIGHT;
            case 'O' -> -1;
            default -> PIECE_PAWN;
        };
    }

    public static char getShortName(int type, char file) {
        return switch (type) {
            case PIECE_KING -> 'K';
            case PIECE_QUEEN -> 'Q';
            case PIECE_ROOK -> 'R';
            case PIECE_BISHOP -> 'B';
            case PIECE_KNIGHT -> 'N';
            default -> file;
        };
    }

    public String getShortName() {
        return String.valueOf(getShortName(type, getFile()));
    }

    public static String getFullName(int type) {
        return switch (type) {
            case PIECE_KING -> "King";
            case PIECE_QUEEN -> "Queen";
            case PIECE_ROOK -> "Rook";
            case PIECE_BISHOP -> "Bishop";
            case PIECE_KNIGHT -> "Knight";
            default -> "Pawn";
        };
    }

    public static String getColorName(int color) {
        return color == COLOR_WHITE ? "white" : "black";
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public int enemyColor() {
        return color == COLOR_WHITE ? COLOR_BLACK : COLOR_WHITE;
    }

    public boolean isKing() {
        return type == PIECE_KING;
    }

    public boolean isQueen() {
        return type == PIECE_QUEEN;
    }

    public boolean isRook() {
        return type == PIECE_ROOK;
    }

    public boolean isBishop() {
        return type == PIECE_BISHOP;
    }

    public boolean isKnight() {
        return type == PIECE_KNIGHT;
    }

    public boolean isPawn() {
        return type == PIECE_PAWN;
    }

    public boolean isWhite() {
        return color == COLOR_WHITE;
    }

    public boolean isBlack() {
        return color == COLOR_BLACK;
    }

    public int brIndex() {
        return Square.index(currentSquare);
    }

}
