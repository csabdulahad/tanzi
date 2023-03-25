package tanzi.model;

import tanzi.pool.PiecePool;

/**
 * The data structure for representing a piece. A fully described piece including its position on the
 * board, previous square, whether it has moved or not and so on.
 * <p>
 * For determining square index, it uses BoardRegistry class to find it. There is a static helper
 * method to clone itself.
 */

public class Piece {

    public static final int COLOR_WHITE = 0;
    public static final int COLOR_BLACK = 1;

    public static final int KING = 0;
    public static final int QUEEN = 1;
    public static final int ROOK = 2;
    public static final int BISHOP = 3;
    public static final int KNIGHT = 4;
    public static final int PAWN = 5;

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
        if (piece == null) return null;

        // get a piece from piece app.pool which will clone the passed piece
        Piece clonedPiece = PiecePool.get();

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

    public String currentSquare() {
        return currentSquare;
    }

    public String previousSquare() {
        return previousSquare == null ? currentSquare : previousSquare;
    }

    public int getRank() {
        return Square.rankAsInt(currentSquare);
    }

    public char getFile() {
        return Square.fileAsChar(currentSquare);
    }

    public static int getType(char piece) {
        return switch (piece) {
            case 'K' -> KING;
            case 'Q' -> QUEEN;
            case 'R' -> ROOK;
            case 'B' -> BISHOP;
            case 'N' -> KNIGHT;
            case 'O' -> -1;
            default -> PAWN;
        };
    }

    public static char getShortName(int type, char file) {
        return switch (type) {
            case KING -> 'K';
            case QUEEN -> 'Q';
            case ROOK -> 'R';
            case BISHOP -> 'B';
            case KNIGHT -> 'N';
            default -> file;
        };
    }

    public String getShortName() {
        return String.valueOf(getShortName(type, getFile()));
    }

    public static String fullName(int type) {
        return switch (type) {
            case -1 -> "Unknown";
            case KING -> "King";
            case QUEEN -> "Queen";
            case ROOK -> "Rook";
            case BISHOP -> "Bishop";
            case KNIGHT -> "Knight";
            default -> "Pawn";
        };
    }

    public static String colorName(int color) {
        return color == COLOR_WHITE ? "white" : "black";
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public int enemyColor() {
        return color == COLOR_WHITE ? COLOR_BLACK : COLOR_WHITE;
    }

    public boolean isKing() {
        return type == KING;
    }

    public boolean isQueen() {
        return type == QUEEN;
    }

    public boolean isRook() {
        return type == ROOK;
    }

    public boolean isBishop() {
        return type == BISHOP;
    }

    public boolean isKnight() {
        return type == KNIGHT;
    }

    public boolean isPawn() {
        return type == PAWN;
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
