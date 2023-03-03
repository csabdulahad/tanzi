package tanzi.model;

import java.util.Arrays;

/*
* POJO for capturing the meta info of an en-passer so that on next move
* enemy can be able to capture it.
* */

public class EnPasser {

    // the opposite army who should be able to take down this enPassant
    public int takerColor = -1;

    // squares of the enPassant(the passer)
    public String beforeSquare;
    public String intermediateSquare;
    public String nowSquare;

    // the opposite army squares of pawn who should be able to take down this enPassant
    public String[] taker;

    public EnPasser copy() {
        EnPasser enPasser = new EnPasser();
        enPasser.takerColor = takerColor;
        enPasser.beforeSquare = beforeSquare;
        enPasser.intermediateSquare = intermediateSquare;
        enPasser.nowSquare = nowSquare;
        enPasser.taker = Arrays.copyOf(taker, taker.length);
        return enPasser;
    }

}
