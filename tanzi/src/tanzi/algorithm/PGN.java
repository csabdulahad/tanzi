package tanzi.algorithm;

import tanzi.model.*;
import tanzi.protocol.PromotionHandler;
import tanzi.staff.BRHistorian;
import tanzi.staff.BoardRegistry;
import tanzi.staff.BufferedBR;
import tanzi.staff.MoveRepo;

/**
 * A chess game can be captured using PGN(Portable Game Notation) syntax. This class has
 * very helpful methods which allow translating of a chess move from one square to another,
 * executing a game to in-memory BoardRegistry with various options such as move repository
 * configuration, BR change history preservation in a BRHistorian etc.
 * <p>
 * Unprocessed pgn string moves can be processing using various helper method such as  format,
 * split etc.
 */

public abstract class PGN {

    /**
     * For any move there are two indices. one is where the move starts and the other is where it
     * goes to. The starting index for the move thus must contain a non-null valid piece for
     * calculating the PGN. the move must be playable by the chess rules. In other words, it assumes
     * that the move is validated by all the chess rules for the current BR.
     * <p>
     * For normal moves including takes, the promotionType should use the type of the piece to which
     * the piece is to be promoted to.
     * <p>
     * If the method fails to calculate the unique name for the move(either the unique name or an empty string)
     * then null value is returned. For a valid PGN out of this function, program should check for returned
     * value by this method.
     */
    public static String translate(int indexA, int indexB, int promotionType, BoardRegistry br) {

        BufferedBR bbr = br.copy();

        String srcSquare = Square.forIndex(indexA);
        String destSquare = Square.forIndex(indexB);

        // define moving piece properties
        Piece srcPiece = bbr.piece(srcSquare);
        int type = srcPiece.type;
        int color = srcPiece.color;
        String movingPieceName = srcPiece.getShortName();

        // if it is a castle move then see if the move made any check to the enemy king
        if (srcPiece.isKing() && King.wantToCastle(srcSquare, destSquare)) {
            int canCastle = King.canCastle(indexA, indexB, bbr);
            if (canCastle == -1) return null;
            String castle = (canCastle == 1) ? "O-O" : "O-O-O";

            // now check if we play the castle move then the enemy king is in check or not.
            // so, make this castle move
            String[] cMeta = King.getCastleMeta(canCastle, color);

            bbr.movePiece(cMeta[0], cMeta[2]);
            bbr.movePiece(cMeta[1], cMeta[3]);

            // now check whether I have given check to enemy
            String mateStatus = Check.mateStatus(color, destSquare, bbr);
            bbr.recycle();

            if (mateStatus.isEmpty()) return castle;
            return castle + "" + mateStatus;
        }

        // get the enPasser if there is any and see whether that can be taken by this move
        EnPasser enPasser = bbr.restoreEnPasser(color);
        if (srcPiece.isPawn() && EnPassant.amIEnpasserTaker(srcSquare, enPasser) && EnPassant.amITakingEnPasser(destSquare, enPasser)) {
            // found an enPasser. put it on intermediate square for being available to take
            bbr.movePiece(enPasser.nowSquare, enPasser.intermediateSquare);
        }

        // am I being an en-passer?
        EnPasser meAsEnpasser = EnPassant.amIEnpasser(srcSquare, destSquare, bbr);
        if (type == Piece.PAWN && meAsEnpasser != null) {
            // save this en-passer object to the buffered BR so that the other algorithms from this point can
            // make use of this and can correctly work out the PGN calculation
            bbr.storeEnPasser(meAsEnpasser);
        }

        // check for taking
        boolean take = bbr.anyPieceOn(destSquare);
        bbr.delete(destSquare);

        // check for promotion
        String promotion = "";
        String capture = take ? "x" : promotion;
        if (promotionType != -1) {
            promotion = "=" + Piece.getShortName(promotionType, srcPiece.getFile());
        }

        // calculate any annotation like + for check, # for checkmate
        String annotation = Check.mateStatusAfterPromo(srcPiece, destSquare, promotionType, bbr);

        if (srcPiece.isPawn()) {
            bbr.recycle();

            if (take)
                return movingPieceName + capture + destSquare + promotion + annotation;

            return destSquare + promotion + annotation;
        } else if (srcPiece.isKing()) {
            bbr.recycle();
            return movingPieceName + capture + destSquare + annotation;
        } else {
            String uniqueDesignation = UniqueName.getUniqueName(type, color, srcSquare, destSquare, bbr);
            bbr.recycle();

            if (uniqueDesignation != null)
                return movingPieceName + uniqueDesignation + capture + destSquare + promotion + annotation;

            return null;
        }
    }

    /**
     * PGN translation of any pawn moving to the highest rank requires promotion which during the game
     * would prompt the player for the promotion. This method just delegates the whole translation to the
     * PGN.translate(int, int, int, BoardRegistry) method, but it takes a lambda argument where it will ask
     * for the promotion from the user.
     */
    public static String translate(int indexA, int indexB, BoardRegistry br, PromotionHandler handler) {

        String destSquare = Square.forIndex(indexB);
        if (destSquare == null) return null;

        // before we start translating PGN, try to see whether the move is going to be a promotion move
        int promotion = -1;
        Piece piece = br.piece(Square.forIndex(indexA));
        if (piece.isPawn()) {
            boolean promoWhite = piece.isWhite() && Square.rankAsInt(destSquare) == 8;
            boolean promoBlack = piece.isBlack() && Square.rankAsInt(destSquare) == 1;
            if (promoWhite || promoBlack) {
                promotion = handler.askPromotionType();
            }
        }

        return translate(indexA, indexB, promotion, br);
    }

    /**
     * A chess game in PGN format can be split into an array of moves. The moves must be separated by
     * commas.
     */
    public static String[] splitMoves(String moves) {
        if (moves == null) return null;
        return moves.split(",");

    }

    /**
     * This method splits a chess moves separated by comma. The moves in the array are trimmed.
     */
    public static String[] splitMovesNoSpace(String moves) {
        String[] splitMoves = PGN.splitMoves(moves);
        String[] noSpace = new String[splitMoves.length];

        for (int i = 0; i < splitMoves.length; i++)
            noSpace[i] = splitMoves[i].trim();

        return noSpace;
    }

    /**
     * A list of moves can be formatted into actual in-file PGN notations using this method.
     * Such functionality comes in handy when moves in a repository need to be serialized for further
     * processing or to be saved as a PGN file.
     */
    public static String format(String[] moves) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < moves.length; i += 2) {

            int turnIndex = (i / 2) + 1;

            String moveA = moves[i];
            builder.append(turnIndex);
            builder.append(". ");
            builder.append(moveA);

            if (i + 1 < moves.length) {
                builder.append(" ");
                String moveB = moves[i + 1];
                builder.append(moveB);
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    /**
     * It splits the moves using comma separator and then passes the array into the PGNWriter.format()
     * to format the moves.
     */
    public static String format(String moves) {
        return format(moves.split(","));
    }

    /**
     * A list of PGN moves can be executed in a BoardRegistry. The returned MoveRepo can be
     * configured with repo type and the specified BRHistorian keeps track of changes made to
     * the BoardRegistry.
     * <p>
     * It throws IllegalArgumentException on either of the moves or the BR being null.
     */
    public static MoveRepo writeToBR(String moves, BoardRegistry br, MoveRepo.Type type, BRHistorian his) throws Exception {
        if (moves == null) throw new IllegalArgumentException("Moves can't be null and empty.");
        if (br == null) throw new IllegalArgumentException("The BoardRegistry can't be null.");

        MoveRepo repo = MoveRepo.of(type, moves);

        for (MoveMeta meta : repo.metaIterable()) {
            BRHistory history = MoveMaker.move(meta, br);
            if (history == null) throw new IllegalStateException("Can't move for " + meta);
            history.saveAndExecute(his, br);
//            MoveMaker.move(meta, br, his);
        }

        return repo;
    }

    /**
     * A MoveRepository of executed moves in the specified BoardRegistry is returned.
     * The repository is type of REPO_GROWING. This PGN.writeToBR() variation doesn't
     * save BR changes in any BRHistorian.
     */
    public static MoveRepo writeToBR(String moves, BoardRegistry br) throws Exception {
        return PGN.writeToBR(moves, br, MoveRepo.Type.REPO_GROWING, null);
    }

    /**
     * A shorter helpful alternative to fully qualified PGN.writeToBR() method. The repo
     * type can be configured however, no BR changes are saved in any BRHistorian as this
     * internally calls on the fully qualified PGN.writeToBR() method with null as argument
     * for BRHistorian.
     */
    public static MoveRepo writeToBR(String moves, BoardRegistry br, MoveRepo.Type type) throws Exception {
        return writeToBR(moves, br, type, null);
    }

    /**
     * This method works like other variations of PGN.writeToBR() method but the repository
     * it creates of type REPO_GROWING. All the changes happens to the BR are kept in the
     * specified BRHistorian object.
     */
    public static MoveRepo writeToBR(String moves, BoardRegistry br, BRHistorian his) throws Exception {
        return writeToBR(moves, br, MoveRepo.Type.REPO_GROWING, his);
    }

    /**
     * For a move index this method can calculate which color it to make a move.
     */
    public static int indexToColor(int index) {
        return index % 2 == 0 ? Piece.COLOR_WHITE : Piece.COLOR_BLACK;
    }

}
