package test.testsuite;

import lib.helper.BenchMarker;
import org.junit.Assert;
import org.junit.Test;
import tanzi.staff.MoveRepo;
import tanzi.algorithm.PGN;
import test.Env;
import test.Puzzle;
import test.TanziTest;

import java.util.ArrayList;
import java.util.List;

import static test.Env.TOTAL_SET;

public class PgnToBRTest {

    public static class T extends TanziTest {

        private final ArrayList<String> fMsg;

        public T() {
            fMsg = new ArrayList<>();
        }

        @Override
        public void test() {
            System.out.println("\ncopying pgn game to BR...");

            for (int setI = 0; setI < TOTAL_SET; setI++) {
                long setExe = BenchMarker.now();

                System.out.print("set " + (setI + 1));

                List<String> gameList = Puzzle.set(setI + 1);

                for (int pgnI = 0; pgnI < gameList.size(); pgnI++) {

                    String gameRaw = gameList.get(pgnI);

                    br.__clearAndSetup();
                    try {
                        MoveRepo repo = PGN.writeToBR(gameRaw, br);
                        success++;
                    } catch (Exception e) {
                        fGameId.add(Env.gameIdInDBOf(setI, pgnI));
                        fGame.add(gameRaw);
                        fMsg.add(e.getMessage());
                    }

                }

                double diff = BenchMarker.diffSec(setExe);
                totalExeTime += diff;
                BenchMarker.log(diff, " took");
            }

            System.out.printf("\ncopying to BR success: %d, failed: %d\n", success, fGameId.size());
            double setExeSec = totalExeTime / TOTAL_SET;
            System.out.printf("all set exec: %.2f sec, set exec avg: %.2f sec, game calc avg: %.2f ms\n", totalExeTime, setExeSec, (setExeSec / 1000) * 1000);

            log();

            Assert.assertEquals(0, fGame.size());
        }

        @Override
        public void log() {
            System.err.println("\ncopying pgn game to BR error log: ");
            for (int i = 0; i < fGameId.size(); i++) {
                System.err.println("game id: " + fGameId.get(i));
                System.err.println(fGame.get(i) + "\n");
                System.err.println(fMsg.get(i));
                System.err.flush();
            }
        }

    }

    @Test
    public void test() {
        new T().test();
    }

}
