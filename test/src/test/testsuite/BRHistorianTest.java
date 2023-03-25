package test.testsuite;

import lib.helper.BenchMarker;
import org.junit.Assert;
import org.junit.Test;
import tanzi.algorithm.MoveMaker;
import tanzi.model.BRHistory;
import tanzi.staff.BoardRegistry;
import tanzi.model.MoveMeta;
import tanzi.staff.*;
import test.Env;
import test.Puzzle;
import test.TanziTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static tanzi.staff.MoveRepo.Type.REPO_GUARDED;
import static test.Env.TOTAL_SET;

public class BRHistorianTest {

    public static class T extends TanziTest {

        // we chose 40 as permutation limit to move back and forth in the BRHistory as it is
        // based on the statistics provided by the Chess games database (685,801) games, the
        // average number of moves is 40.04.
        private final int PERMUTATION_LIMIT = 40;
        MoveRepo repo;
        BRHistorian his;
        Random random;

        ArrayList<String> fromToHistory;

        public T() {
            random = new Random();
            fromToHistory = new ArrayList<>();
        }

        @Override
        public void test() throws Exception {
            System.out.println("\nexecuting history permutation...");

            for (int setI = 0; setI < TOTAL_SET; setI++) {
                long setExe = BenchMarker.now();

                System.out.print("set " + (setI + 1));

                List<String> gameList = Puzzle.set(setI + 1);

                for (int pgnI = 0; pgnI < gameList.size(); pgnI++) {

                    String gameRaw = gameList.get(pgnI);

                    // reload the repo and create historian if it has been
                    repo = repo == null ? MoveRepo.of(REPO_GUARDED, gameRaw) : repo;
                    his = his == null ? new BRHistorian(repo, br) : his;

                    br.__clearAndSetup();
                    repo.reload(gameRaw);
                    for (MoveMeta meta : repo.metaIterable()) {
                        BRHistory history = MoveMaker.move(meta, br);
                        Assert.assertNotNull(history);
                        history.saveAndExecute(his, br);
                    }

                    int moveIndex = -1, lastIndex = -1;
                    try {
                        for (int i = 0; i < PERMUTATION_LIMIT; i++) {
                            moveIndex = random.nextInt(repo.moveCount());
                            Assert.assertTrue(his.goTo(moveIndex));
                            Assert.assertEquals(moveIndex, repo.currentIndex());

                            // for every odd number, do an outbound navigation failure test
                            if (moveIndex % 2 != 0) {
                                Assert.assertFalse(his.goTo(repo.moveCount()));
                                Assert.assertEquals(moveIndex, repo.currentIndex());
                                success ++;
                            }

                            lastIndex = repo.currentIndex();
                            success++;
                        }
                    } catch (Exception e) {
                        fGameId.add(Env.gameIdInDBOf(setI, pgnI));
                        fGame.add(gameRaw);
                        fromToHistory.add("Wanted to go from " + lastIndex + " to " + moveIndex + "\nerr msg: " + e.getMessage());
                        break;
                    }

                }

                double diff = BenchMarker.diffSec(setExe);
                totalExeTime += diff;
                BenchMarker.log(diff, " took");
            }

            System.out.printf("\npermutation success: %d, failed: %d\n", success, fGameId.size());
            double setExeSec = totalExeTime / TOTAL_SET;
            System.out.printf("all set exec: %.2f sec, set exec avg: %.2f sec, game calc avg: %.2f ms\n", totalExeTime, setExeSec, (setExeSec / 1000) * 1000);

            log();

            Assert.assertEquals(0, fGame.size());
        }

        @Override
        public void log() {
            System.err.println("\nhistory permutation error log: ");
            for (int i = 0; i < fGameId.size(); i++) {
                System.err.println("game id: " + fGameId.get(i));
                System.err.println(fromToHistory.get(i));
                System.err.println(fGame.get(i) + "\n");
                System.err.flush();
            }
        }
    }

    @Test
    public void permutationTest() throws Exception {
        new T().test();
    }

    @Test
    public void singlePermutationTest() throws Exception {
        String moves = "e4, e5, Nf3, Nc6, Bb5";
        BoardRegistry br = new BoardRegistry();
        MoveRepo repo = MoveRepo.growingRepo(moves);
        BRHistorian his = new BRHistorian(repo, br);

        Assert.assertTrue(his.goTo(1));
        Assert.assertEquals(1, repo.currentIndex());

        Assert.assertTrue(his.goTo(-1));
        Assert.assertEquals(-1, repo.currentIndex());

        Assert.assertFalse(his.goTo(5));
        Assert.assertEquals(-1, repo.currentIndex());

        Assert.assertTrue(his.goTo(0));
        Assert.assertEquals(0, repo.currentIndex());

        Assert.assertTrue(his.goTo(4));
        Assert.assertEquals(4, repo.currentIndex());

        Assert.assertTrue(his.goTo(2));
        Assert.assertEquals(2, repo.currentIndex());

        Assert.assertTrue(his.goTo(4));
        Assert.assertEquals(4, repo.currentIndex());

        Assert.assertTrue(his.goTo(0));
        Assert.assertEquals(0, repo.currentIndex());

    }

    @Test
    public void brHistorianSize() throws Exception {
        String moves = "e4, e5";

        BoardRegistry br = new BoardRegistry();
        MoveRepo repo = MoveRepo.of(MoveRepo.Type.REPO_GROWING, moves);
        BRHistorian his = new BRHistorian(repo, br);

        for (MoveMeta meta : repo.metaIterable()) {
            BRHistory history = MoveMaker.move(meta, br);
            Assert.assertNotNull(history);
            history.saveAndExecute(his, br);
        }
        Assert.assertEquals(2, his.numOfChanges());

        moves = "d4, d5, Nf3, Nc6";
        repo.reload(moves);
        Assert.assertEquals(0, his.numOfChanges());
    }

    @Test
    public void brhOnRepoOverridden() throws Exception {
        String moves = "e4, e5, Nf3, Nc6, Bb5"; // Ruy López Opening

        BoardRegistry br = new BoardRegistry();
        MoveRepo repo = MoveRepo.of(MoveRepo.Type.REPO_GROWING, moves);
        BRHistorian his = new BRHistorian(repo, br);

        // play the game
        for (MoveMeta meta : repo.metaIterable()) {
            BRHistory history = MoveMaker.move(meta, br);
            Assert.assertNotNull(history);
            history.saveAndExecute(his, br);
        }

        Assert.assertFalse(repo.override("d4"));

        // now change this Ruy López Opening game to a Scotch Game
        Assert.assertTrue(repo.override("d4", 4));
        Assert.assertEquals(4, his.numOfChanges());

        Assert.assertTrue(repo.override("d4", 2));
        Assert.assertEquals(2, his.numOfChanges());

        BRHistory history = MoveMaker.move(repo.metaAt(2), br);
        Assert.assertNotNull(history);
        history.saveAndExecute(his, br);

        Assert.assertEquals(3, his.numOfChanges());
    }

}
