package tanzi.model;

/*
 * a data structure for holding information about the moves as they are made on the board in order to
 * be able to do undo, backward, forward move.
 *
 * this class also holds information about castle, promotion, check or checkmate status by the move.
 * */

public class MoveMeta {

    // properties for debugging purpose
    public String pgn;
    public int puzzleSet;
    public int pgnIndex;
    public int moveIndex;

    // move properties
    public int type = -1;
    public String move;
    public String normalizedMove = null;
    public int color = -1;
    public int moveIndexInPGN = -1;

    // desired properties
    public String destSquare = null;

    // define whether the move needs to be identified as unique one such as Nbd4
    public boolean uniqueName = false;
    public boolean uniqueRank = false;
    public boolean uniqueFile = false;
    public boolean uniqueSquare = false;
    public char uniqueFileName = Character.MIN_VALUE;
    public int uniqueRankName = -1;
    public String uniqueSquareName = null;

    // define whether move takes any piece or a simple move
    public boolean simpleMove = false;
    public boolean takes = false;

    // define whether move promotes to any piece
    public boolean promotion = false;
    public int promoteType = -1;

    // define whether it is king castling
    public boolean castle = false;
    public boolean shortCastle = false;
    public boolean longCastle = false;

    // check & checkmate status
    public boolean check = false;
    public boolean checkMate = false;

    // properties for enPassant
    public boolean enPassant = false;
    public String enPasserIntermediate = null;
    public String enPasserNow = null;
    public String enPasserTaker;

    public MoveMeta() {

    }

    public MoveMeta(String move) {
        if (move == null) return;

        this.move = move;
        this.check = move.contains("+");
        this.checkMate = move.contains("#");
        this.normalizedMove = move.replace("+", "").replace("#", "");
    }

    @Override
    public String toString() {
        return "\n" + move + " > MoveMeta : color(" + Piece.getColorName(color) + "), move(" + move + "), normalizedMove(" + normalizedMove + "), type(" + Piece.getFullName(type) + ")\nsimpleMove(" + simpleMove + "), takes(" + takes + "), destSquare(" + destSquare +
                ")\npromotion(" + promotion + "), to(" + promoteType + ")\nuniqueName(" + uniqueName + "), uniqueFile(" + uniqueFile + "), uniqueFileName(" + (uniqueFileName == Character.MIN_VALUE ? "-1" : uniqueFileName) + "), uniqueRank(" + uniqueRank + "), uniqueRankName(" + uniqueRankName + "), uniqueSquare(" + uniqueSquare + "), uniqueSquareName(" + uniqueSquareName + ")\ncastle(" + castle + "), shortCastle(" + shortCastle + "), longCastle(" + longCastle + ")";
    }

    public MoveMeta copy() {
        MoveMeta moveMeta = new MoveMeta();

        // start copying properties to new MoveMeta

        // move properties
        moveMeta.type = type;
        moveMeta.move = move;
        moveMeta.normalizedMove = normalizedMove;
        moveMeta.color = color;

        // desired properties
        moveMeta.destSquare = destSquare;

        // define whether the move needs to be identified as unique one such as Nbd4
        moveMeta.uniqueName = uniqueName;
        moveMeta.uniqueRank = uniqueRank;
        moveMeta.uniqueFile = uniqueFile;
        moveMeta.uniqueSquare = uniqueSquare;
        moveMeta.uniqueFileName = uniqueFileName;
        moveMeta.uniqueRankName = uniqueRankName;
        moveMeta.uniqueSquareName = uniqueSquareName;

        // define whether move takes any piece or a simple move
        moveMeta.simpleMove = simpleMove;
        moveMeta.takes = takes;

        // define whether move promotes to any piece
        moveMeta.promotion = promotion;
        moveMeta.promoteType = promoteType;

        // define whether it is king castling
        moveMeta.castle = castle;
        moveMeta.shortCastle = shortCastle;
        moveMeta.longCastle = longCastle;

        // check & checkmate status
        moveMeta.check = check;
        moveMeta.checkMate = checkMate;

        // properties for enPassant
        moveMeta.enPassant = enPassant;
        moveMeta.enPasserIntermediate = enPasserIntermediate;
        moveMeta.enPasserNow = enPasserNow;
        moveMeta.enPasserTaker = enPasserTaker;

        return moveMeta;
    }

}