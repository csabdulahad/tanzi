package tanzi.model;

public class Promotion {

    /**
     * This method can return associated promotion type from the above of a PGN move.
     * Method can be useful to automate the promotion when testing PGN moves.
     */
    public static int getType(String move) {
        int index = move.indexOf('=');
        if (index == -1) return -1;

        char promo = move.charAt(index + 1);
        if (promo == 'Q') return Piece.QUEEN;
        else if (promo == 'R') return Piece.ROOK;
        else if (promo == 'B') return Piece.BISHOP;
        return Piece.KNIGHT;
    }

}
