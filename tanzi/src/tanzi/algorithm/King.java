package tanzi.algorithm;

import tanzi.model.MoveMeta;
import tanzi.model.Piece;
import tanzi.model.Square;
import tanzi.staff.Arbiter;
import tanzi.staff.BoardRegistry;
import tanzi.staff.BufferedBR;
import tanzi.staff.GeometryEngineer;

import java.util.ArrayList;

public abstract class King {

    public static boolean canGo(String srcSquare, String destSquare, Arbiter ar, BoardRegistry br) {

        Piece king = br.getPiece(srcSquare);

        // prove the king move by chess geometry
        ArrayList<String> validKingSquares = GeometryEngineer.validSquare(king.type, king.color, king.getCurrentSquare(), true, br);
        if (!validKingSquares.contains(destSquare)) return false;

        // then see whether there is KingKingClash on the destination square
        if (kingClashOn(destSquare, br)) return false;

        // now here at this point, we are sure that the opposite king has no influence over
        // the destination square. now move the king to the destination square by deleting
        // any enemy piece if any. then see whether the king is safe there. if so then the
        // king can go to the destination square otherwise not.

        BufferedBR bufferedBR = br.getCopy();

        // if it is an enemy piece on the destination square then remove it
        bufferedBR.deleteEntry(destSquare);

        // make the king move
        king = bufferedBR.movePiece(srcSquare, destSquare);

        // after king made the move, let's see whether the king can be attacked there
        boolean canGo = Arbiter.amISafe(king.color, king.getCurrentSquare(), ar, bufferedBR);

        bufferedBR.recycle();
        return canGo;
    }

    /*
     * this method incorporates all the checks that are required to calculate whether the king can
     * castle or not. it first validates if either the king or the rook has moved. then it checks if
     * there is any piece in between the king and rook. finally it calculates whether the squares
     * that the king will use to castle via are guarded by enemy pieces or not.
     *
     * based on the above checks this method can return 1 for short castle, 2 for long castle and -1
     * for illegal castle.
     *
     * for short castle the file argument must be "g" and for long castle it must be "c".
     * */
    public static int canCastle(int fromIndex, int toIndex, Arbiter ar, BoardRegistry br) {

        BufferedBR bufferedBR = br.getCopy();

        String kingSquare = Square.forIndex(fromIndex);
        String destSquare = Square.forIndex(toIndex);

        if (destSquare == null) return -1;

        Piece king = bufferedBR.getPiece(kingSquare);
        int kingColor = king.color;
        char file = Square.getFileAsChar(destSquare);

        // king already made move previously so return -1 to say we can't have a castle move
        if (king.hasMoved()) return -1;

        String rookSquare = getRookSquare(file, kingColor);
        if (rookSquare == null) return -1;
        Piece rook = bufferedBR.getPiece(rookSquare);
        /*
         * if no rook found there then the rook has moved somewhere which concludes that it can't be
         * castled or if the rook was found at position but the has move as it hasMoved says so.
         */
        if (rook == null || rook.hasMoved()) return -1;

        /*
         * for a castle move the king uses an intermediate square to castle. so according to chess
         * rules the square must be free and can't be guarded by enemy pieces. this method calculate
         * that square the king uses to castle which is known as passingSquare in the code-base.
         * */
        char passingFile = file == 'g' ? 'f' : 'd';
        int passingRank = king.isWhite() ? 1 : 8;
        String passingSquare = String.valueOf(passingFile) + passingRank;

        // make sure there is no piece in between the king and the rook
        String[] squareBetweenKingRook = getSquareInKingRook(passingRank, file);
        for (String square : squareBetweenKingRook) if (bufferedBR.anyPieceOn(square)) return -1;

        ArrayList<String> validKingSquare = GeometryEngineer.validSquare(Piece.PIECE_KING, kingColor, kingSquare, true, bufferedBR);
        if (!validKingSquare.contains(passingSquare)) return -1;

        // check for passing via square first
        boolean kingCanGo = canGo(kingSquare, passingSquare, ar, bufferedBR);
        if (!kingCanGo) return -1;

        /*
         * make the king for the "via" square temporarily and then see if the king can finally reach
         * the dest square which will check for situation that if king can reach the square and that
         * square is guarded by enemy piece.
         */
        bufferedBR.movePiece(kingSquare, passingSquare);
        bufferedBR.updateOSSquare(kingColor, passingSquare);

        kingCanGo = canGo(passingSquare, destSquare, ar, bufferedBR);

        int result = 0;
        if (!kingCanGo) result = -1;

        // recycle the BufferedBR
        bufferedBR.recycle();

        // at this position, we can say that is a castle move. let's see which castle it is
        return result == -1 ? result : (file == 'g' ? 1 : 2);
    }

    public static int canCastle(String from, String to, Arbiter arbiter, BoardRegistry br) {
        return canCastle(Square.index(from), Square.index(to), arbiter, br);
    }

    /*
     * based on the castle direction(by file) and rank(by color) this method returns square where a
     * rook should be for a legal castle by the standard chess game.
     *
     * it returns null if file & rank can't be resolved to calculate the right square for the rook.
     * */
    private static String getRookSquare(char file, int kingColor) {
        if (file == 'g' && kingColor == Piece.COLOR_WHITE) return "h1";
        if (file == 'c' && kingColor == Piece.COLOR_WHITE) return "a1";
        if (file == 'g' && kingColor == Piece.COLOR_BLACK) return "h8";
        if (file == 'c' && kingColor == Piece.COLOR_BLACK) return "a8";
        return null;
    }

    /*
     * for given the rank and file(to decide which type of castle) it can generate squares that would
     * be checked to make sure that there is no pieces on them to have a valid castle.
     * */
    private static String[] getSquareInKingRook(int rank, char file) {
        return file == 'g' ? new String[]{"f" + rank, "g" + rank} : new String[]{"d" + rank, "c" + rank, "b" + rank};
    }

    /*
     * for a king move, it can tell whether the king wants to castle
     * based on the square the king wants to go to by chess geometry.
     * */
    public static boolean wantToCastle(String from, String to) {
        if (from == null || to == null) return false;

        if (from.equals("e1"))
            return to.equals("g1") || to.equals("c1");

        if (from.equals("e8"))
            return to.equals("g8") || to.equals("c8");

        return false;
    }

    /*
     * for a given king and its destination square along with board registry, this method can tell
     * which type of castle king wants to make. it returns -1 for invalid situations where either
     *       * king has moved
     *       * rook has moved,
     * and it returns 2 for long castle and 1 for short castle.
     * */
    public static int getCastleType(Piece king, String destSquare, BoardRegistry br) {

        if (king.hasMoved()) return -1;

        String whichRook;
        int castleType;

        if (king.isWhite()) {
            if (destSquare.equals("g1")) {
                whichRook = "h1";
                castleType = 1;
            } else if (destSquare.equals("c1")) {
                whichRook = "a1";
                castleType = 2;
            } else {
                return -1;
            }
        } else {
            if (destSquare.equals("g8")) {
                whichRook = "h8";
                castleType = 1;
            } else if (destSquare.equals("c8")) {
                whichRook = "a8";
                castleType = 2;
            } else {
                return -1;
            }
        }

        // if rook has moved then return -1
        if (br.getPiece(whichRook).hasMoved()) return -1;

        return castleType;
    }

    /*
     * this method for given move meta can return the dest square for both the king and the rook
     * based on their current squares and the destination squares.
     *
     * the returned array looks like the following:
     * [currentKingSquare, currentRookSquare, destKingSquare, destRookSquare]
     * */
    public static String[] getCastleMeta(MoveMeta moveMeta) {

        // king-rook current file
        char currentKingFile = 'e';
        char currentRookFile = moveMeta.shortCastle ? 'h' : 'a';

        // king-rook dest file
        char destKingFile = moveMeta.shortCastle ? 'g' : 'c';
        char destRookFile = moveMeta.shortCastle ? 'f' : 'd';

        // king-rook rank
        int kingRank = moveMeta.color == Piece.COLOR_WHITE ? 1 : 8;
        int rookRank = moveMeta.color == Piece.COLOR_WHITE ? 1 : 8;

        // current square for both king-rook
        String currentKingSquare = currentKingFile + "" + kingRank;
        String currentRookSquare = currentRookFile + "" + rookRank;

        // dest square for both king-rook
        String destKingSquare = destKingFile + "" + kingRank;
        String destRookSquare = destRookFile + "" + rookRank;

        return new String[]{currentKingSquare, currentRookSquare, destKingSquare, destRookSquare};
    }

    public static String[] getCastleMeta(int castleType, int color) {
        MoveMeta meta = new MoveMeta();
        meta.shortCastle = castleType == 1;
        meta.color = color;
        return getCastleMeta(meta);
    }

    // for a given square, this method first get the octal square list to degree one
    // then for those square it tries to see whether there are two kings found or not.
    // if so that means there are two kings having influence over the given square thus
    // it returns true
    public static boolean kingClashOn(String square, BoardRegistry br) {
        // get the octal square to degree one
        ArrayList<String> octalSquareList = GeometryEngineer.octalSquareTo1(square);

        int count = 0;
        for (String octalSquare : octalSquareList) {
            // figure out whether the piece is empty
            Piece piece = br.getPiece(octalSquare);
            if (piece == null) continue;

            // make sure the piece is a king
            if (piece.isKing()) count++;

            // for a square to guarded by two kings, the count will be two.
            // if so then true
            if (count == 2) return true;
        }
        return false;
    }

}
