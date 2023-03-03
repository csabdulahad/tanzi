package tanzi.algorithm;

import tanzi.staff.Arbiter;
import tanzi.staff.BoardRegistry;
import tanzi.staff.GeometryEngineer;

import java.util.ArrayList;

public abstract class Check {

    /*
     * haveIGivenCheck method assumes that the move was valid to play by all chess rules and the BR is
     * reflecting the move. so with this new algorithm, we don't need to make any changes to BR
     * within algorithm thus fewer possibilities to have bugs and less code.
     *
     * here the color is of the army who played the move.
     * */

    public static boolean didIGiveCheck(int color, Arbiter arbiter, BoardRegistry boardRegistry) {
        String oppositeKingSquare = boardRegistry.getOppositeKingSquare(color);

        ArrayList<String> attacker = arbiter.whoCanGo(oppositeKingSquare, color, false, boardRegistry);
        if (attacker.size() > 0) return true;

        ArrayList<String> knightOnOppositeKing = GeometryEngineer.possibleKnightTo(oppositeKingSquare, color, boardRegistry);
        return knightOnOppositeKing.size() > 0;
    }

}
