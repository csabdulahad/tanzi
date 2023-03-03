package tanzi.staff;

import tanzi.algorithm.*;
import tanzi.model.EnPasser;
import tanzi.model.Piece;
import tanzi.model.Square;

public class PGNBuilder {

    private PGNBuilder() {

    }

    interface PromotionHandler {
        int getPromotion();
    }


    public static String pgn(int indexA, int indexB, Arbiter ar, PromotionHandler handler) {

        BoardRegistry br = ar.getBR();

        String destSquare = Square.forIndex(indexB);
        if (destSquare == null) return null;

        // before we start translating PGN, try to see whether the move is going to be a promotion move
        int promotion = -1;
        Piece piece = br.getPiece(Square.forIndex(indexA));
        if (piece.isPawn()) {
            boolean promoWhite = piece.isWhite() && Square.getRankAsInt(destSquare) == 8;
            boolean promoBlack = piece.isBlack() && Square.getRankAsInt(destSquare) == 1;
            if (promoWhite || promoBlack) {
                promotion = handler.getPromotion();
            }
        }

        return pgn(indexA, indexB, promotion, ar);
    }

    /*
     * for any move there are two indices. one is where the move starts and the other is where it
     * goes to. the starting index for the move thus must contain a non-null valid piece for
     * calculating the PGN. the move must be playable by the chess rules. in other words, it assumes
     * that the move is validated by the arbiter for current BR.
     *
     * for normal moves including takes, the promotionType should use the type of the piece to which
     * the piece is to be promoted.
     *
     * this method can't calculate the castle moves. if the method is failed to calculate the unique
     * name for the move(either the unique name or an empty string) then null value is returned. for
     * a valid PGN out of this function, program should check for returned value by this method.
     * */
    public static String pgn(int indexA, int indexB, int promotionType, Arbiter ar) {

        BufferedBR bbr = ar.getBR().getCopy();

        String srcSquare = Square.forIndex(indexA);
        String destSquare = Square.forIndex(indexB);

        // define moving piece properties
        Piece srcPiece = bbr.getPiece(srcSquare);
        int type = srcPiece.type;
        int color = srcPiece.color;
        String movingPieceName = srcPiece.getShortName();

        // if it is a castle move then see if the move made any check to the enemy king
        if (srcPiece.isKing() && King.wantToCastle(srcSquare, destSquare)) {
            int canCastle = King.canCastle(indexA, indexB, ar, bbr);
            if (canCastle == -1) return null;
            String castle = (canCastle == 1) ? "O-O" : "O-O-O";

            // now check if we play the castle move then the enemy king is in check or not.
            // so, make this castle move
            String[] cMeta = King.getCastleMeta(canCastle, color);

            bbr.movePiece(cMeta[0], cMeta[2]);
            bbr.movePiece(cMeta[1], cMeta[3]);

            // now check whether I have given check to enemy
            String mateStatus = PGNBuilder.mateAnnotation(color, destSquare, ar, bbr);
            bbr.recycle();

            if (mateStatus.isEmpty()) return castle;
            return castle + "" + mateStatus;
        }

        // get the enPasser if there is any and see whether that can be taken by this move
        EnPasser enPasser = ar.getEnPasserFor(color);
        if (srcPiece.isPawn() && EnPassant.amIEnpasserTaker(srcSquare, enPasser) && EnPassant.amITakingEnPasser(destSquare, enPasser)) {
            // found an enPasser. put it on intermediate square for being available to take
            bbr.movePiece(enPasser.nowSquare, enPasser.intermediateSquare);
        }

        // am I being an en-passer?
        EnPasser meAsEnpasser = EnPassant.amIEnpasser(srcSquare, destSquare, bbr);
        if (type == Piece.PIECE_PAWN && meAsEnpasser != null) {
            ar.setEnpasserForEnemy(meAsEnpasser);
        }

        // check for taking
        boolean take = bbr.anyPieceOn(destSquare);
        bbr.deleteEntry(destSquare);

        // check for promotion
        String promotion = "";
        String capture = take ? "x" : promotion;
        if (promotionType != -1) {
            promotion = "=" + Piece.getShortName(promotionType, srcPiece.getFile());
        }

        // calculate any annotation like + for check, # for checkmate
        String annotation = PGNBuilder.mateStatus(srcPiece, destSquare, promotionType, ar, bbr);

        if (srcPiece.isPawn()) {
            bbr.recycle();

            if (take)
                return movingPieceName + capture + destSquare + promotion + annotation;
            return destSquare + promotion + annotation;
        } else if (srcPiece.isKing()) {
            bbr.recycle();
            return movingPieceName + capture + destSquare + annotation;
        } else {
            String uniqueDesignation = UniqueName.getUniqueName(type, color, srcSquare, destSquare, ar, bbr);
            bbr.recycle();
            if (uniqueDesignation != null)
                return movingPieceName + uniqueDesignation + capture + destSquare + promotion + annotation;

            return null;
        }
    }

    /*
     * this method first make the changes to the BR to reflect that the move has been played. because
     * the check and checkmate algorithms assume that the move has played to start the algorithms.
     *
     * it returns "#" or "+" as mate annotation based on the output from isCheck and isCheckMate
     * of arbiter.
     *
     * Note: this board registry is a buffered registry. the pgn translate method already
     * */
    private static String mateStatus(Piece srcPiece, String destSquare, int promotionType, Arbiter ar, BoardRegistry br) {

        BufferedBR bufferedBR = br.getCopy();

        // get the moving piece from the buffered BR and make the move to the dest square
        Piece piece = bufferedBR.movePiece(srcPiece.getCurrentSquare(), destSquare);

        // set promotion type
        if (promotionType != -1) piece.type = promotionType;

        // check for check and checkmate status
        String annotation = PGNBuilder.mateAnnotation(piece.color, destSquare, ar, bufferedBR);
        bufferedBR.recycle();
        return annotation;
    }

    private static String mateAnnotation(int checkerColor, String square, Arbiter ar, BufferedBR bbr) {
        // now check whether I have given check to enemy
        boolean checkMate = false;

        boolean check = Check.didIGiveCheck(checkerColor, ar, bbr);
        if (check) checkMate = Checkmate.isMate(square, ar, bbr);

        if (checkMate) return "#";
        return check ? "+" : "";
    }

}
