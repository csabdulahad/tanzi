package tanzi.model;

/**
 * A data structure for holding information about the moves as they are made on the board in order to
 * be able to do undo, backward, forward move.
 * <p>
 * this class also holds information about castle, promotion, check or checkmate status by the move.
 */

public class MoveMeta {

    // which identifies the move within the game so that we can calculate whether it is white or black
    // to move
    public int moveIndex;

    // move properties
    public int type = -1;
    public String move;
    public String normalizedMove = null;
    public int color = -1;

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
        return String.format(
                """
                        MoveMeta:           %s
                        moveIndex:          %d
                        normalizedMove:     %s
                        piece:              %s %s
                        simpleMove:         %s
                        destSquare:         %s
                        takes:              %s
                        castle:             %s
                        shortCastle:        %s
                        longCastle:         %s
                        promotion:          %s to %s
                        uniqueName:         %s
                        uniqueFile:         %s \tuniqueFileName: \t\t%s
                        uniqueRank:         %s \tuniqueRankName: \t\t%s
                        uniqueSquare:       %s \tuniqueSquareName: \t\t%s
                """,
                move, moveIndex, normalizedMove, Piece.colorName(color), Piece.fullName(type), simpleMove, destSquare,
                takes, castle, shortCastle, longCastle, promotion, Piece.fullName(promoteType),
                uniqueName, uniqueFile, uniqueFileName, uniqueRank, uniqueRankName, uniqueSquare, uniqueSquareName
        );
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