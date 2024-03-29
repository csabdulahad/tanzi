package tanzi.model;

import tanzi.app.Board;

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds square of a chess board. Each square is associated with a square name and index
 * number. a8 is the 1st square, b8 is the 2nd square and so on.
 */
public abstract class Square {

    // useful easy indexing with squares

    public static final int C3 = 43;
    public static final int E1 = 61;
    public static final int E8 = 5;

    // square-index translation map/table
    public static final HashMap<String, Integer> indexSquareWhite;

    private static final HashMap<String, Integer> indexSquareBlack;

    static {
        indexSquareBlack = new HashMap<>(64);
        indexSquareBlack.put("a8", 64);
        indexSquareBlack.put("b8", 63);
        indexSquareBlack.put("c8", 62);
        indexSquareBlack.put("d8", 61);
        indexSquareBlack.put("e8", 60);
        indexSquareBlack.put("f8", 59);
        indexSquareBlack.put("g8", 58);
        indexSquareBlack.put("h8", 57);
        indexSquareBlack.put("a7", 56);
        indexSquareBlack.put("b7", 55);
        indexSquareBlack.put("c7", 54);
        indexSquareBlack.put("d7", 53);
        indexSquareBlack.put("e7", 52);
        indexSquareBlack.put("f7", 51);
        indexSquareBlack.put("g7", 50);
        indexSquareBlack.put("h7", 49);
        indexSquareBlack.put("a6", 48);
        indexSquareBlack.put("b6", 47);
        indexSquareBlack.put("c6", 46);
        indexSquareBlack.put("d6", 45);
        indexSquareBlack.put("e6", 44);
        indexSquareBlack.put("f6", 43);
        indexSquareBlack.put("g6", 42);
        indexSquareBlack.put("h6", 41);
        indexSquareBlack.put("a5", 40);
        indexSquareBlack.put("b5", 39);
        indexSquareBlack.put("c5", 38);
        indexSquareBlack.put("d5", 37);
        indexSquareBlack.put("e5", 36);
        indexSquareBlack.put("f5", 35);
        indexSquareBlack.put("g5", 34);
        indexSquareBlack.put("h5", 33);
        indexSquareBlack.put("a4", 32);
        indexSquareBlack.put("b4", 31);
        indexSquareBlack.put("c4", 30);
        indexSquareBlack.put("d4", 29);
        indexSquareBlack.put("e4", 28);
        indexSquareBlack.put("f4", 27);
        indexSquareBlack.put("g4", 26);
        indexSquareBlack.put("h4", 25);
        indexSquareBlack.put("a3", 24);
        indexSquareBlack.put("b3", 23);
        indexSquareBlack.put("c3", 22);
        indexSquareBlack.put("d3", 21);
        indexSquareBlack.put("e3", 20);
        indexSquareBlack.put("f3", 19);
        indexSquareBlack.put("g3", 18);
        indexSquareBlack.put("h3", 17);
        indexSquareBlack.put("a2", 16);
        indexSquareBlack.put("b2", 15);
        indexSquareBlack.put("c2", 14);
        indexSquareBlack.put("d2", 13);
        indexSquareBlack.put("e2", 12);
        indexSquareBlack.put("f2", 11);
        indexSquareBlack.put("g2", 10);
        indexSquareBlack.put("h2", 9);
        indexSquareBlack.put("a1", 8);
        indexSquareBlack.put("b1", 7);
        indexSquareBlack.put("c1", 6);
        indexSquareBlack.put("d1", 5);
        indexSquareBlack.put("e1", 4);
        indexSquareBlack.put("f1", 3);
        indexSquareBlack.put("g1", 2);
        indexSquareBlack.put("h1", 1);

        indexSquareWhite = new HashMap<>(64);
        indexSquareWhite.put("a8", 1);
        indexSquareWhite.put("b8", 2);
        indexSquareWhite.put("c8", 3);
        indexSquareWhite.put("d8", 4);
        indexSquareWhite.put("e8", 5);
        indexSquareWhite.put("f8", 6);
        indexSquareWhite.put("g8", 7);
        indexSquareWhite.put("h8", 8);
        indexSquareWhite.put("a7", 9);
        indexSquareWhite.put("b7", 10);
        indexSquareWhite.put("c7", 11);
        indexSquareWhite.put("d7", 12);
        indexSquareWhite.put("e7", 13);
        indexSquareWhite.put("f7", 14);
        indexSquareWhite.put("g7", 15);
        indexSquareWhite.put("h7", 16);
        indexSquareWhite.put("a6", 17);
        indexSquareWhite.put("b6", 18);
        indexSquareWhite.put("c6", 19);
        indexSquareWhite.put("d6", 20);
        indexSquareWhite.put("e6", 21);
        indexSquareWhite.put("f6", 22);
        indexSquareWhite.put("g6", 23);
        indexSquareWhite.put("h6", 24);
        indexSquareWhite.put("a5", 25);
        indexSquareWhite.put("b5", 26);
        indexSquareWhite.put("c5", 27);
        indexSquareWhite.put("d5", 28);
        indexSquareWhite.put("e5", 29);
        indexSquareWhite.put("f5", 30);
        indexSquareWhite.put("g5", 31);
        indexSquareWhite.put("h5", 32);
        indexSquareWhite.put("a4", 33);
        indexSquareWhite.put("b4", 34);
        indexSquareWhite.put("c4", 35);
        indexSquareWhite.put("d4", 36);
        indexSquareWhite.put("e4", 37);
        indexSquareWhite.put("f4", 38);
        indexSquareWhite.put("g4", 39);
        indexSquareWhite.put("h4", 40);
        indexSquareWhite.put("a3", 41);
        indexSquareWhite.put("b3", 42);
        indexSquareWhite.put("c3", 43);
        indexSquareWhite.put("d3", 44);
        indexSquareWhite.put("e3", 45);
        indexSquareWhite.put("f3", 46);
        indexSquareWhite.put("g3", 47);
        indexSquareWhite.put("h3", 48);
        indexSquareWhite.put("a2", 49);
        indexSquareWhite.put("b2", 50);
        indexSquareWhite.put("c2", 51);
        indexSquareWhite.put("d2", 52);
        indexSquareWhite.put("e2", 53);
        indexSquareWhite.put("f2", 54);
        indexSquareWhite.put("g2", 55);
        indexSquareWhite.put("h2", 56);
        indexSquareWhite.put("a1", 57);
        indexSquareWhite.put("b1", 58);
        indexSquareWhite.put("c1", 59);
        indexSquareWhite.put("d1", 60);
        indexSquareWhite.put("e1", 61);
        indexSquareWhite.put("f1", 62);
        indexSquareWhite.put("g1", 63);
        indexSquareWhite.put("h1", 64);
    }

    public static int index(String square) {
        if (!indexSquareWhite.containsKey(square)) return -1;
        return indexSquareWhite.get(square);
    }

    public static int index(String square, Board.Orientation orientation) {
        if (!indexSquareWhite.containsKey(square)) return -1;
        HashMap<String, Integer> map = orientation == Board.Orientation.White ? indexSquareWhite : indexSquareBlack;
        return map.get(square);
    }

    public static String forIndex(int index) {
        if (!indexSquareWhite.containsValue(index)) return null;
        for (Map.Entry<String, Integer> entry : indexSquareWhite.entrySet()) {
            if (entry.getValue() == index) return entry.getKey();
        }
        return null;
    }

    public static String forIndex(int index, Board.Orientation orientation) {
        HashMap<String, Integer> map = orientation == Board.Orientation.White ? indexSquareWhite : indexSquareBlack;
        if (!map.containsValue(index)) return null;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() == index) return entry.getKey();
        }
        return null;
    }

    public static String fileAsStr(String square) {
        return String.valueOf(square.charAt(0));
    }

    public static char fileAsChar(String square) {
        return square.charAt(0);
    }

    public static int rankAsInt(String square) {
        return Character.getNumericValue(square.charAt(1));
    }

    public static char rankAsChar(String square) {
        return square.charAt(1);
    }

    public static String rankAsStr(String square) {
        return String.valueOf(square.charAt(1));
    }

}
