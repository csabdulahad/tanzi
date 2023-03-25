package test.testsuite;

import org.junit.Assert;
import org.junit.Test;
import tanzi.model.BRHistory;
import tanzi.model.MoveMeta;
import tanzi.staff.BRHistorian;
import tanzi.staff.BoardRegistry;
import tanzi.algorithm.MoveMaker;
import tanzi.staff.MoveRepo;
import test.Puzzle;

public class MoveMakerTest {

    @Test
    public void test() throws Exception {

        Puzzle puzzle = new Puzzle();
        String move;

        while ((move = puzzle.next()) != null) {
            BoardRegistry br = new BoardRegistry();
            MoveRepo repo = MoveRepo.of(MoveRepo.Type.REPO_GROWING, move);
            BRHistorian his = new BRHistorian(repo, br);

            for (MoveMeta meta : repo.metaIterable()) {
                BRHistory history = MoveMaker.move(meta, br);
                Assert.assertNotNull(history);
                history.saveAndExecute(his, br);
            }
        }

    }

}
