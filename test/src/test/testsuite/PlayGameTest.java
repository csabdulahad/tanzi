package test.testsuite;

import lib.helper.BenchMarker;
import org.junit.Assert;
import org.junit.Test;
import tanzi.model.BRHistory;
import tanzi.model.MoveMeta;
import tanzi.algorithm.MoveMaker;
import tanzi.staff.MoveRepo;
import test.Env;
import test.Puzzle;
import test.TanziTest;

import java.util.ArrayList;
import java.util.List;

import static tanzi.staff.MoveRepo.Type.REPO_GUARDED;
import static test.Env.TOTAL_SET;

public class PlayGameTest {

    public static class T extends TanziTest {

        private final ArrayList<MoveMeta> fMeta;
        private final ArrayList<String> fMsg;

        public T() {
            fMeta = new ArrayList<>();
            fMsg = new ArrayList<>();
        }

        @Override
        public void test() throws Exception {
            System.out.println("\npalying pgn games...");

            for (int setI = 0; setI < TOTAL_SET; setI++) {
                long setExe = BenchMarker.now();

                System.out.print("set " + (setI + 1));

                List<String> gameList = Puzzle.set(setI + 1);

                for (int pgnI = 0; pgnI < gameList.size(); pgnI++) {

                    String gameRaw = gameList.get(pgnI);


                    br.__clearAndSetup();
                    MoveRepo moveRepo = MoveRepo.of(REPO_GUARDED, gameRaw);
                    for (MoveMeta moveMeta : moveRepo.metaIterable()) {

                        try {
                            BRHistory history = MoveMaker.move(moveMeta, br);
                            Assert.assertNotNull(history);
                            history.redo(br);
                            success++;
                        } catch (Exception e) {
                            fGameId.add(Env.gameIdInDBOf(setI, pgnI));
                            fGame.add(gameRaw);
                            fMeta.add(moveMeta);
                            fMsg.add(e.getMessage());
                            break;
                        }
                    }

                }

                double diff = BenchMarker.diffSec(setExe);
                totalExeTime += diff;
                BenchMarker.log(diff, " took");
            }

            System.out.printf("\nplaying pgn moves success: %d, failed: %d\n", success, fGameId.size());
            double setExeSec = totalExeTime / TOTAL_SET;
            System.out.printf("all set exec: %.2f sec, set exec avg: %.2f sec, game calc avg: %.2f ms\n", totalExeTime, setExeSec, (setExeSec / 1000) * 1000);

            log();

            Assert.assertEquals(0, fGame.size());
        }

        @Override
        public void log() {
            System.err.println("\npalying pgn games error log: ");
            for (int i = 0; i < fGameId.size(); i++) {
                System.err.printf("game id %d\n", fGameId.get(i));
                System.err.println(fMsg.get(i));
                System.err.println(fGame.get(i));
                System.err.println(fMeta.get(i) + "\n");
                System.err.flush();
            }
        }

    }

    @Test
    public void test() throws Exception {
        new T().test();
    }

}
