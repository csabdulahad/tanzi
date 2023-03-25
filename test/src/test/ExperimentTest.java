package test;

import org.junit.Test;
import tanzi.app.Game;

public class ExperimentTest {

    @Test
    public void test() throws Exception {
        Game game = Game.ofMoves("e4,e5,Nf3,Nc5,Bb5").create(0);
        System.out.println(game.exportToPGN());
    }

}
