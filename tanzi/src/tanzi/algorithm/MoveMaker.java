package tanzi.algorithm;

import tanzi.model.*;
import tanzi.staff.BoardRegistry;

/**
 * Any analyzed move, in MoveMeta form, can be tested to make sure whether it can be played
 * by all chess rules and be executed in to the BR. This class does this operation. With
 * a move meta and which br to check against, it can return the history of changes the move
 * will make into the BR.
 * <p>
 * If case of any failure or on invalid move, it returns null which means that the move is
 * not valid to play.
 */

public abstract class MoveMaker {

    /**
     * This move method, takes in a meta, and a BR to validate whether it is playable or not.
     * On valid calculation, it returns a BRHistory containing the details about the changes
     * this move will make if executed in the specified, with undo and redo BRChange objects.
     * <p>
     * Note: This method doesn't alter the BR at all. The execution of the changes to the BR
     * will require the setting of en-passer object if there will be any, clearing available
     * en-passer object, updating the OS square if it is a king move.
     * <p>
     * For this all, there is a helper method in the BRHistory which allows both saving the
     * changes to a specified BRHistorian and execution to a specified BR with all the
     * en-passer management, king OS square update etc.
     */
    public static BRHistory move(MoveMeta meta, BoardRegistry br) {

        if (meta.castle) return MoveMaker.playCastleMove(meta, br);

        // can I take down any enemy EnPasser?
        if (meta.type == Piece.PAWN) {
            EnPasser enemyEnPasser = br.restoreEnPasser(meta.color);
            if (enemyEnPasser != null && enemyEnPasser.intermediateSquare.equals(meta.destSquare)) {
                meta.enPasserTaker = EnPassant.whoIsEnPasserTaker(enemyEnPasser.taker, meta);
                if (meta.enPasserTaker == null) return null;

                meta.enPassant = true;
                meta.simpleMove = false;
                meta.enPasserNow = enemyEnPasser.nowSquare;
                meta.enPasserIntermediate = enemyEnPasser.intermediateSquare;

                return MoveMaker.playEnPassantMove(meta, br);
            }
        }

        Piece piece = Arbiter.getPiece(meta, br);
        if (piece == null) return null;

        String srcSquare = piece.currentSquare();
        String destSquare = meta.destSquare;

        // Am I being an EnPasser? If yes, then this EnPasser should be included with the redo object
        EnPasser meBeingEnPasser = null;
        if (piece.type == Piece.PAWN) {
            meBeingEnPasser = EnPassant.amIEnpasser(srcSquare, destSquare, br);
        }

        BRHistory.Builder hisBuilder = BRHistory.Builder.forMove(meta.moveIndex);

        boolean simpleMove = MoveMaker.simpleMove(meta, srcSquare, br, hisBuilder);
        boolean takeMove = MoveMaker.capture(meta, srcSquare, br, hisBuilder);
        boolean promotionMove = MoveMaker.promotion(meta, srcSquare, br, hisBuilder);
        boolean uniqueMove = MoveMaker.unique(meta, srcSquare, br, hisBuilder);

        if (!simpleMove && !takeMove && !promotionMove && !uniqueMove) return null;

        // if the move is being an en-passer then when we redo this, then the en-passer
        // object should be reflected in the BR. So save it in the redo en-passer variable
        // which will allow the next move to take this en-passer, if they wish.
        if (meBeingEnPasser != null) hisBuilder.enPasserToRedo(meBeingEnPasser);

        return hisBuilder.build();
    }

    private static BRHistory playCastleMove(MoveMeta meta, BoardRegistry br) {
        if (!meta.castle) return null;

        // get where the king & rook are and where they want to go
        String[] castleMeta = King.getCastleMeta(meta);

        int canGo = King.canCastle(castleMeta[0], castleMeta[2], br);
        if (canGo == -1) return null;

        // no piece should be on the squares where the king and the rook will be after castling
        if (br.anyPieceOn(castleMeta[2]) || br.anyPieceOn(castleMeta[3])) return null;

        // start by getting the king and the rook from the BR
        Piece rdKing = br.clonedPiece(castleMeta[0]);
        Piece rdRook = br.clonedPiece(castleMeta[1]);

        // clone the original king & rook
        Piece udKing = Piece.clone(rdKing);
        Piece udRook = Piece.clone(rdRook);

        // modify current king & rook
        if (rdKing == null || rdRook == null) return null;
        rdKing.setCurrentSquare(castleMeta[2]);
        rdRook.setCurrentSquare(castleMeta[3]);

        // opposite position setup for backward navigation and animation
        udKing.setPreviousSquare(castleMeta[2]);
        udRook.setPreviousSquare(castleMeta[3]);

        // build history for this change
        return BRHistory.Builder
                .forMove(meta.moveIndex)
                .squareToUndo(castleMeta[2], castleMeta[3])
                .pieceToUndo(udKing, udRook)
                .squareToRedo(castleMeta[0], castleMeta[1])
                .pieceToRedo(rdKing, rdRook)
                .build();
    }

    private static BRHistory playEnPassantMove(MoveMeta meta, BoardRegistry br) {
        if (!meta.enPassant) return null;

        // en-passer intermediate square must be empty
        if (br.anyPieceOn(meta.enPasserIntermediate)) return null;

        // first delete both the enPasser & the taker
        Piece udTaker = br.clonedPiece(meta.enPasserTaker);
        Piece unEnPasser = br.clonedPiece(meta.enPasserNow);

        // both the taker and enPasser mustn't be null
        if (udTaker == null || unEnPasser == null) return null;

        Piece rdTaker = br.clonedPiece(meta.enPasserTaker);
        rdTaker.setCurrentSquare(meta.destSquare);

        // save into move history and also save the enPasser object for back navigation
        return BRHistory.Builder
                .forMove(meta.moveIndex)
                .squareToUndo(meta.destSquare)
                .pieceToUndo(udTaker, unEnPasser)
                .squareToRedo(udTaker.currentSquare(), meta.enPasserNow)
                .pieceToRedo(rdTaker)
                .enPasserToUndo(null)
                .build();
    }

    private static boolean simpleMove(MoveMeta meta, String srcSquare, BoardRegistry br, BRHistory.Builder hisBuilder) {
        if (!meta.simpleMove) return false;
        return MoveMaker.basicMove(meta, srcSquare, br, hisBuilder);
    }

    /**
     * As it turned that both unique and simple move are common in terms changes they make to BR
     * except the check for the flag for whether it is simple or unique move. Hence, we have this
     * extracted method.
     */
    private static boolean basicMove(MoveMeta meta, String srcSquare, BoardRegistry br, BRHistory.Builder hisBuilder) {

        // create redo Piece and validate it if it is not null
        Piece rdPiece = br.clonedPiece(srcSquare);
        if (rdPiece == null) return false;
        rdPiece.setCurrentSquare(meta.destSquare);

        // delete the piece from the BR and do opposite position setup for backward animation
        Piece udPiece = br.clonedPiece(srcSquare);
        udPiece.setPreviousSquare(meta.destSquare);

        // save this change into move history
        hisBuilder
                .squareToUndo(meta.destSquare)
                .pieceToUndo(udPiece)
                .squareToRedo(srcSquare)
                .pieceToRedo(rdPiece);

        return true;
    }

    private static boolean capture(MoveMeta meta, String srcSquare, BoardRegistry br, BRHistory.Builder hisBuilder) {
        if (!meta.takes) return false;

        if (!br.anyPieceOn(meta.destSquare)) return false;

        // delete the taker and delete the taken piece from BR, store opposite animation position


        Piece udTakerPiece = br.clonedPiece(srcSquare);
        Piece udTakenPiece = br.clonedPiece(meta.destSquare);
        if (udTakenPiece == null || udTakerPiece == null) return false;

        // update redo taker piece position to destination square
        Piece rdTakerPiece = br.clonedPiece(srcSquare);
        rdTakerPiece.setCurrentSquare(meta.destSquare);

        // on redo, the taken piece is already on the spot for opposite animation
        udTakenPiece.setPreviousSquare(meta.destSquare);

        // on undo, for taker it should animate from the captured square to previous square
        udTakerPiece.setPreviousSquare(meta.destSquare);

        hisBuilder
                .squareToRedo(srcSquare, meta.destSquare)
                .pieceToRedo(rdTakerPiece)
                .squareToUndo(meta.destSquare)
                .pieceToUndo(udTakerPiece, udTakenPiece);

        return true;
    }

    private static boolean promotion(MoveMeta meta, String srcSquare, BoardRegistry br, BRHistory.Builder hisBuilder) {
        if (!meta.promotion) return false;

        /*
         * If it is a promotion and take move at the same time, then the history builder has already
         * built the redo pawn piece onto the promoted square. Here we just need to update that piece
         * type to reflect the promotion.
         * */
        if (meta.takes) {
            Piece piece = hisBuilder.singleRedoPiece();
            if (piece == null) return false;

            // set new promoted type
            piece.type = meta.promoteType;
            return true;
        }

        // Here we know that, it is a simple pawn promotion move so do the necessary operation.

        Piece udPawn = br.clonedPiece(srcSquare);
        if (udPawn == null) return false;

        // update previous square for opposite animation effect
        udPawn.setPreviousSquare(meta.destSquare);

        Piece rdPromotedPiece = br.clonedPiece(srcSquare);
        rdPromotedPiece.type = meta.promoteType;
        rdPromotedPiece.setCurrentSquare(meta.destSquare);

        hisBuilder
                .squareToUndo(meta.destSquare)
                .pieceToUndo(udPawn)
                .squareToRedo(srcSquare)
                .pieceToRedo(rdPromotedPiece);
        return true;
    }

    private static boolean unique(MoveMeta meta, String srcSquare, BoardRegistry br, BRHistory.Builder hisBuilder) {
        if (!meta.uniqueName) return false;
        return basicMove(meta, srcSquare, br, hisBuilder);
    }

}
