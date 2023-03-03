package tanzi.staff;

import tanzi.algorithm.King;
import tanzi.algorithm.Pin;
import tanzi.model.*;

import java.util.ArrayList;
import java.util.List;

public class Arbiter {

    // an arbiter has a board registry to manage
    private final BoardRegistry boardRegistry;

    /*
     * here enPasserForWhite & enPasserForBlack hold the en-passer objects of opposite army.
     * The en-passer previously en-passed it's army.
     *
     * For example, an en-passer object is created when a pawn en-pass the enemy pawn. so here
     * the enPasserForWhite holds the en-passer of black army and vice-versa. In white's next
     * move it can get the en-passer of black and be able to take it down if white wants.
     * */
    private EnPasser enPasserForWhite, enPasserForBlack;

    private MoveMeta lastMoveMetaBlack;
    private MoveMeta lastMoveMetaWhite;

    public Arbiter(BoardRegistry boardRegistry) {
        this.boardRegistry = boardRegistry;
    }

    /*
     * it returns the en passer of opposite army who previously en-passed the army of given color
     * */
    public EnPasser getEnPasserFor(int takerColor) {
        if (takerColor == Piece.COLOR_BLACK) return this.enPasserForBlack;
        return this.enPasserForWhite;
    }

    /*
     * here en-passer object is saved for enemy army. because the enemy can get this as
     * en-passer object and be able to take down this en-passer on next move.
     * */
    public void setEnpasserForEnemy(EnPasser enPasser) {
        if (enPasser.takerColor == Piece.COLOR_BLACK) enPasserForBlack = enPasser;
        else enPasserForWhite = enPasser;
    }

    public void clearEnPasserFor(int color) {
        if (color == Piece.COLOR_WHITE) enPasserForWhite = null;
        else enPasserForBlack = null;
    }

    public MoveMeta getLastMove(int color) {
        return color == Piece.COLOR_WHITE ? this.lastMoveMetaWhite : this.lastMoveMetaBlack;
    }

    public void setLastMoveMeta(MoveMeta moveMeta) {
        if (moveMeta.color == Piece.COLOR_WHITE) this.lastMoveMetaWhite = moveMeta;
        else this.lastMoveMetaBlack = moveMeta;
    }

    /*
     * this method works on OSS. an OSS is a list of squares around 8 sides to each end of a
     * piece, however it can be any piece meaning we can have OSS of a queen too.
     *
     * it returns true if the center square of OSS can't be attacked by any enemy piece, otherwise
     * it returns false.
     * */
    public static boolean amISafe(int myColor, String mySquare, Arbiter ar, BoardRegistry br) {

        // I think I'm safe. They don't believe!  Let's see :)
        boolean safe = true;

        // first generate octal square for my square and associate octal square segment
        OctalSquare octalSquare = new OctalSquare(mySquare);
        OctalSquareSegment octalSquareSegment = new OctalSquareSegment(octalSquare.getOSSquare());

        boolean segmentLoopSwitch = true;

        int totalSegment = octalSquareSegment.getTotalSegment();
        for (int i = 0; i < totalSegment; i++) {
            // make sure if future segments need to be checked as told by previous iteration
            if (!segmentLoopSwitch) break;

            // for OSS, get a segment to calculate squares where possible enemy can come from
            List<String> segment = octalSquareSegment.getSegment(i);
            for (String square : segment) {

                Piece piece = br.getPiece(square);
                if (piece == null) continue; // empty square; go ahead

                /*
                 * since it is from own army, there is no way that an enemy piece can come along
                 * the segment path; so ignore this segment
                 * */
                if (piece.color == myColor) break;

                /*
                 * got an enemy piece. now check if that enemy piece can come to that square
                 * regardless of pin
                 * */
                if (ar.pieceCanGo(piece.getCurrentSquare(), mySquare, false, br)) {
                    safe = false;
                    /*
                     * we no longer need to see any further as we have already discovered that
                     * the square is guarded by enemy piece. discard this segment & further
                     * segment looping
                     * */
                    segmentLoopSwitch = false;
                    break;
                } else {
                    /*
                     * we say break greedily, as we have found an enemy piece, and it can't reach
                     * the king's dest square so, there is no possible way any other enemy piece
                     * can come to the king square along this segment path. discard the segment;
                     * but loop the next segment.
                     * */
                    break;
                }

            }

        }

        // now check for knight attacks on the dest square if there is any
        if (safe) {
            int enemyColor = Piece.getOppositeColor(myColor);
            int numOfKnight = GeometryEngineer.possibleKnightTo(mySquare, enemyColor, br).size();
            safe = numOfKnight == 0;
        }

        return safe;
    }

    /*
     * for a move, from source square to destination square, this method firstly checks if it is legal
     * to move by chess geometry with own army check, secondly it checks whether the piece is pinned.
     *
     * for king, it also checks whether king can go the destination square as the square could be
     * guarded by enemy pieces.
     * */
    public boolean pieceCanGo(String from, String to, boolean pinChecking, BoardRegistry br) {

        Piece piece = br.getPiece(from);
        if (piece == null) return false;

        int type = piece.type;
        int color = piece.color;

        // first get valid squares from GeometryEngineer and checks if we have valid squares at all
        ArrayList<String> validSquares = GeometryEngineer.validSquare(type, color, from, true, br);
        if (validSquares == null || validSquares.size() == 0) return false;

        // if it is a pawn then check if it can take any available enPasser
        EnPasser enPasser = getEnPasserFor(color);
        if (type == Piece.PIECE_PAWN && enPasser != null) {
            // get the list of enPassant taker who can take the passer
            String[] enPasserTaker = enPasser.taker;
            for (String taker : enPasserTaker) {
                /* if the srcSquare is found in the enPasserTaker then this move can take enPassant
                 * piece, so add this source square as a valid square too
                 */
                if (taker.equals(from)) {
                    validSquares.add(enPasser.intermediateSquare);
                    break;
                }
            }
        }

        if (!validSquares.contains(to)) return false;

        // make sure whether the squares are guarded by enemy for the king
        if (type == Piece.PIECE_KING)
            return King.canGo(from, to, this, br);

        if (pinChecking)
            // if it is pinned then negate the return value to say it can't
            return !Pin.isPinned(from, to, this, br);

        return true;
    }

    // this method tries to calculate the list of the squares of a given army can go to a focus
    // square with pin check for a given board registry. if first generates the OS for the focus
    // square that then starts counting along each segment to find out whether it finds an enemy.
    // for any enemy, it then asks the arbiter whether the enemy piece can go the focus square
    // with given pin checking flag or not.
    //
    // after that, it also tries to calculate possible knights to the focus square.
    public ArrayList<String> whoCanGo(String focusSquare, int whichArmy, boolean pinCheck, BoardRegistry br) {
        ArrayList<String> whoCanGo = new ArrayList<>();

        OctalSquare octalSquare = new OctalSquare(focusSquare);
        OctalSquareSegment segment = new OctalSquareSegment(octalSquare.getOSSquare());

        for (int i = 0; i < segment.getTotalSegment(); i++) {
            List<String> squareList = segment.getSegment(i);
            for (String square : squareList) {
                // get the piece
                Piece piece = br.getPiece(square);

                // found an empty piece
                if (piece == null) continue;

                // found an enemy; however we would like to see whether any piece from
                // our army can make move to focusSquare regarding pin or not. we discard
                // the segment path because there is no way that any of our piece can reach
                // the focusSquare by CHESS RULES
                if (piece.color != whichArmy) break;

                // found a piece from our own army; let's see if it can go to destSquare
                // regarding pin or not
                boolean canGo = pieceCanGo(square, focusSquare, pinCheck, br);
                if (canGo) {
                    // found a piece which can make the move
                    whoCanGo.add(square);

                    // we then discard the segment because no more than one piece from our
                    // army can go the focusSquare. so along this segment path, we have got
                    // that piece
                    break;
                }
            }
        }

        // now check for Knights; if any of our knights can jump to the focusSquare
        ArrayList<String> possibleKnight = GeometryEngineer.possibleKnightTo(focusSquare, whichArmy, br);
        for (String square : possibleKnight) {
            // if the knight can go regarding pin, then add it
            if (pieceCanGo(square, focusSquare, true, br)) whoCanGo.add(square);
        }

        return whoCanGo;
    }

    // this method for a piece, can calculate a list of squares that it can make move to
    // regarding PIN status.
    public ArrayList<String> possibleMoveFor(String pieceSquare, BoardRegistry br) {
        ArrayList<String> possibleMoveList = new ArrayList<>();

        Piece piece = br.getPiece(pieceSquare);
        if (piece == null) return possibleMoveList;

        ArrayList<String> validSquareList = GeometryEngineer.validSquare(piece.type, piece.color, pieceSquare, true, br);

        for (String square : validSquareList) {
            if (pieceCanGo(pieceSquare, square, true, br))
                possibleMoveList.add(square);
        }

        return possibleMoveList;
    }

    /*
     * for a given move meta, this function can calculate which piece of your army can satisfy the
     * move as specified by the move meta.
     *
     * it can return null. when null is returned that means no piece can make move for given moveMeta
     * */
    public Piece getPiece(MoveMeta moveMeta, BoardRegistry br) {
        ArrayList<Piece> candidatePieceList = getCandidatePieceList(moveMeta, br);

        // search and see whether any of candidate pieces can match the passed-in move meta
        for (Piece piece : candidatePieceList) {
            ArrayList<String> validMoves = GeometryEngineer.validSquare(piece.type, piece.color, piece.getCurrentSquare(), true, br);
            for (String validMove : validMoves) {
                if (validMove == null) continue;
                if (validMove.equals(moveMeta.destSquare)) {
                    boolean canGoWithoutPinned = pieceCanGo(piece.getCurrentSquare(), moveMeta.destSquare, true, br);
                    if (canGoWithoutPinned) return piece;
                }
            }
        }
        return null;
    }

    /*
     * this method can estimate which piece/s of a given moveMeta can make the move as described by
     * moveMeta. it uses the various methods of board registry such as getPieceByFile,
     * getPieceByColor and getPieceByRank to find/match pieces that satisfies the moveMeta
     * requirements.
     *
     * if none of the pieces of an army can make move for that moveMeta, then the returned list will
     * be empty.
     * */
    public static ArrayList<Piece> getCandidatePieceList(MoveMeta moveMeta, BoardRegistry br) {
        ArrayList<Piece> candidatePieceList = new ArrayList<>();

        // get all the pawns on a specified file, because there can be 2 pawns named 'e' for instance
        if (moveMeta.type == Piece.PIECE_PAWN) {
            candidatePieceList = br.getPieceByFile(moveMeta.normalizedMove.charAt(0), moveMeta.type, moveMeta.color);
            return candidatePieceList;
        }

        // if unique name is found then look pieces exactly by that unique qualifier
        if (moveMeta.uniqueName) {
            if (moveMeta.uniqueFile)
                candidatePieceList = br.getPieceByFile(moveMeta.uniqueFileName, moveMeta.type, moveMeta.color);
            if (moveMeta.uniqueRank)
                candidatePieceList = br.getPieceByRank(moveMeta.uniqueRankName, moveMeta.type, moveMeta.color);
            return candidatePieceList;
        }

        // since it is not a pawn or unique move, then it must be a regular piece type move
        candidatePieceList = br.getPieceByType(moveMeta.type, moveMeta.color);
        return candidatePieceList;
    }

    /*
     * when this method is invoked then the in-memory game reset to the initial position
     * meaning the Board Registry, Last Move Meta, enPassant etc. get destroyed and reset .
     * */
    public void __clearAndSetup() {
        enPasserForWhite = null;
        enPasserForBlack = null;
        lastMoveMetaWhite = null;
        lastMoveMetaBlack = null;
        boardRegistry.__clearAndSetup();
    }

    public BoardRegistry getBR() {
        return boardRegistry;
    }

}
