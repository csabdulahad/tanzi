package tanzi.staff;

import org.jetbrains.annotations.Nullable;
import tanzi.algorithm.EnPassant;
import tanzi.algorithm.King;
import tanzi.exception.*;
import tanzi.model.EnPasser;
import tanzi.model.MoveMeta;
import tanzi.model.Piece;

/*
 * MoveMeta of each PGN move can be executed to the board registry associated with an arbiter.
 * Optional move history changing capabilities can be achieved by passing a MoveHistoryBeta object. If
 * is provided then changes to board registry can be undone. It is possible as En-passant action is
 * encapsulated with the arbiter object.
 * */

abstract public class MoveMaker {

    /*
     * This method binds the arbiter, board registry, move history(move navigation capabilities)
     * together for a move meta. when a game is being played, after analysis of pgn which results in
     * a move meta can be executed on the board registry maintained by specified the arbiter.
     *
     * The move history can be null. It is helpful when you just want to analyze PGN moves and execute
     * them in the board registry to continue a game.
     * */
    public static boolean move(MoveMeta moveMeta, Arbiter ar, @Nullable MoveHistory moveHistory) throws Exception {

        if (moveMeta.castle) return playCastleMove(moveMeta, ar, moveHistory);

        // can I take down any enemy EnPasser?
        if (moveMeta.type == Piece.PIECE_PAWN) {
            EnPasser enemyEnPasser = ar.getEnPasserFor(moveMeta.color);
            if (enemyEnPasser != null && enemyEnPasser.intermediateSquare.equals(moveMeta.destSquare)) {

                moveMeta.enPasserTaker = EnPassant.whoIsEnPasserTaker(enemyEnPasser.taker, moveMeta);
                if (moveMeta.enPasserTaker == null) throw new NoEnPasserTakerExcep(enemyEnPasser.intermediateSquare);

                moveMeta.enPassant = true;
                moveMeta.simpleMove = false;
                moveMeta.enPasserNow = enemyEnPasser.nowSquare;
                moveMeta.enPasserIntermediate = enemyEnPasser.intermediateSquare;

                return playEnPassantMove(moveMeta, ar, moveHistory);
            }
        }

        Piece piece = ar.getPiece(moveMeta, ar.getBR());
        if (piece == null) throw new NoPieceCanMoveException("No piece can move");

        String srcSquare = piece.getCurrentSquare();
        String destSquare = moveMeta.destSquare;

        // am I being an EnPasser?
        if (piece.type == Piece.PIECE_PAWN) {
            EnPasser meBeingEnPasser = EnPassant.amIEnpasser(srcSquare, destSquare, ar.getBR());
            if (meBeingEnPasser != null) {
                // let the arbiter know about me being an anonymous EN-PASSER :)
                ar.setEnpasserForEnemy(meBeingEnPasser);
            }
        }

        boolean simpleMove = simpleMove(moveMeta, piece, ar, moveHistory);
        boolean takeMove = capture(moveMeta, piece, ar, moveHistory);
        boolean promotionMove = promotion(moveMeta, piece, ar, moveHistory);
        boolean uniqueMove = unique(moveMeta, piece, ar, moveHistory);
        if (!simpleMove && !takeMove && !promotionMove && !uniqueMove) return false;

        // update the KAS after a successful king move
        if (moveMeta.type == Piece.PIECE_KING) {
            ar.getBR().updateOSSquare(moveMeta.color, moveMeta.destSquare);
        }

        // store last move meta
        ar.setLastMoveMeta(moveMeta);

        ar.clearEnPasserFor(moveMeta.color);
        return true;
    }

    private static boolean playEnPassantMove(MoveMeta moveMeta, Arbiter ar, MoveHistory moveHistory) throws Exception {

        if (!moveMeta.enPassant) return false;

        BoardRegistry br = ar.getBR();

        // en-passer intermediate square must be empty
        if (br.anyPieceOn(moveMeta.enPasserIntermediate))
            throw new SquareOccupationExcep(moveMeta.enPasserIntermediate);

        // first delete both the enPasser & the taker
        Piece taker = br.deleteEntry(moveMeta.enPasserTaker);
        Piece enPasser = br.deleteEntry(moveMeta.enPasserNow);
        if (taker == null || enPasser == null) throw new PieceNotFoundExcep();

        // save into move history and also save the enPasser object for back navigation
        if (moveHistory != null) {
            moveHistory.saveBinaryChange(moveMeta.moveIndexInPGN, moveMeta.destSquare, taker, enPasser.getCurrentSquare(), enPasser);
            moveHistory.peek().enPasser = ar.getEnPasserFor(moveMeta.color);
        }

        // modify the taker and add to BR
        taker.setCurrentSquare(moveMeta.destSquare);
        br.addEntry(taker);

        // clear the enPasser object
        ar.clearEnPasserFor(moveMeta.color);

        return true;
    }

    private static boolean playCastleMove(MoveMeta moveMeta, Arbiter arbiter, MoveHistory moveHistory) throws Exception {
        if (!moveMeta.castle) return false;

        BoardRegistry br = arbiter.getBR();

        // get where the king & rook are and where they want to go
        String[] castleMeta = King.getCastleMeta(moveMeta);

        int canGo = King.canCastle(castleMeta[0], castleMeta[2], arbiter, br);
        if (canGo == -1) throw new KingCanNotCastleExcep();

        // start by removing from the BR
        Piece king = br.deleteEntry(castleMeta[0]);
        Piece rook = br.deleteEntry(castleMeta[1]);

        if (king == null || rook == null) throw new PieceNotFoundExcep();
        if (br.anyPieceOn(castleMeta[2]) || br.anyPieceOn(castleMeta[3]))
            throw new SquareOccupationExcep();

        // clone the original king & rook
        Piece oldKing = Piece.clone(king);
        Piece oldRook = Piece.clone(rook);

        // modify current king & rook
        king.setCurrentSquare(castleMeta[2]);
        rook.setCurrentSquare(castleMeta[3]);

        // opposite position setup for backward navigation and animation
        oldKing.setPreviousSquare(castleMeta[2]);
        oldRook.setPreviousSquare(castleMeta[3]);

        // modify the BR
        br.addEntry(king);
        br.addEntry(rook);

        // clear enPassant accordingly
        arbiter.clearEnPasserFor(moveMeta.color);

        // update KAS for new king square
        br.updateOSSquare(moveMeta.color, castleMeta[2]);

        // save this change in the move history
        if (moveHistory != null)
            moveHistory.saveBinaryChange(moveMeta.moveIndexInPGN, castleMeta[3], oldRook, castleMeta[2], oldKing);

        return true;
    }

    private static boolean promotion(MoveMeta moveMeta, Piece piece, Arbiter ar, MoveHistory moveHistory) throws Exception {
        if (!moveMeta.promotion) return false;

        BoardRegistry br = ar.getBR();

        /*
         * if it is a promotion and take move at the same time, then taking move has already
         * been played meaning that the piece has been moved to the promoted square. just update
         * the piece type to reflect the promotion. otherwise move the piece to the promoted
         * square and change its type as well.
         * */

        if (!moveMeta.takes) {
            // delete the moved taker piece from promoted square
            if (br.deleteEntry(piece.getCurrentSquare()) == null) throw new PieceNotFoundExcep();


            // clone & opposite animation position
            Piece oldPiece = Piece.clone(piece);
            oldPiece.setPreviousSquare(moveMeta.destSquare);

            // update the current square and add to BR
            piece.setCurrentSquare(moveMeta.destSquare);
            br.addEntry(piece);

            // update the move history
            if (moveHistory != null)
                moveHistory.saveSingleChange(moveMeta.moveIndexInPGN, moveMeta.destSquare, oldPiece);
        }

        // set new promoted type
        piece.type = moveMeta.promoteType;

        return true;
    }

    private static boolean unique(MoveMeta moveMeta, Piece piece, Arbiter ar, MoveHistory moveHistory) throws Exception {
        if (!moveMeta.uniqueName) return false;
        return basicMove(moveMeta, piece, ar, moveHistory);
    }

    private static boolean capture(MoveMeta moveMeta, Piece piece, Arbiter ar, MoveHistory moveHistory) throws Exception {
        if (!moveMeta.takes) return false;

        BoardRegistry br = ar.getBR();

        if (!br.anyPieceOn(moveMeta.destSquare))
            throw new IllegalStateException("can't take on empty square");

        // delete the taker and delete the taken piece from BR, store opposite animation position

        Piece takenPiece = br.deleteEntry(moveMeta.destSquare);

        if (takenPiece == null) throw new PieceNotFoundExcep();
        if (br.deleteEntry(piece.getCurrentSquare()) == null) throw new PieceNotFoundExcep();

        takenPiece.setPreviousSquare(moveMeta.destSquare);

        // clone for taker, store opposite animation position
        Piece previousLocation = Piece.clone(piece);
        previousLocation.setPreviousSquare(moveMeta.destSquare);

        // add to BR after new updated square
        piece.setCurrentSquare(moveMeta.destSquare);
        br.addEntry(piece);

        // update move history
        if (moveHistory != null)
            moveHistory.saveBinaryChange(moveMeta.moveIndexInPGN, piece.getCurrentSquare(), previousLocation, takenPiece.getCurrentSquare(), takenPiece);

        return true;
    }

    private static boolean simpleMove(MoveMeta moveMeta, Piece piece, Arbiter arbiter, MoveHistory moveHistory) throws Exception {
        if (!moveMeta.simpleMove) return false;
        return basicMove(moveMeta, piece, arbiter, moveHistory);
    }

    /*
     * As it turned that both unique and simple move are common in terms changes they make to BR
     * except the check for the flag for whether it is simple or unique move. Hence, we have this
     * extracted method.
     * */
    private static boolean basicMove(MoveMeta moveMeta, Piece piece, Arbiter ar, MoveHistory moveHistory) throws PieceNotFoundExcep {
        String currentSquare = piece.getCurrentSquare();
        String newSquare = moveMeta.destSquare;

        BoardRegistry br = ar.getBR();

        // delete the piece from the BR
        if (br.deleteEntry(currentSquare) == null) throw new PieceNotFoundExcep();

        // clone the piece & store opposite position setup for backward animation
        Piece previousLocation = Piece.clone(piece);
        previousLocation.setPreviousSquare(newSquare);

        // modify the piece and add to the BR
        piece.setCurrentSquare(newSquare);
        br.addEntry(piece);

        // save this change into move history
        if (moveHistory != null) moveHistory.saveSingleChange(moveMeta.moveIndexInPGN, newSquare, previousLocation);

        return true;
    }


}
