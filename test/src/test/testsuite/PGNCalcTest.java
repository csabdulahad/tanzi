package test.testsuite;

import lib.helper.BenchMarker;
import org.junit.Assert;
import org.junit.Test;
import tanzi.algorithm.King;
import tanzi.algorithm.PieceDiscovery;
import tanzi.model.BRHistory;
import tanzi.model.MoveMeta;
import tanzi.model.Piece;
import tanzi.model.Square;
import tanzi.algorithm.MoveMaker;
import tanzi.staff.MoveRepo;
import tanzi.algorithm.PGN;
import test.Env;
import test.Puzzle;
import test.TanziTest;

import java.util.ArrayList;
import java.util.List;

import static tanzi.staff.MoveRepo.Type.REPO_GUARDED;
import static test.Env.TOTAL_SET;

public class PGNCalcTest {

    public static class T extends TanziTest {

        ArrayList<String> fAndExpected;
        ArrayList<Integer> fMoveIndex;
        ArrayList<MoveMeta> fMoveMeta;

        public T() {
            fMoveMeta = new ArrayList<>();
            fMoveIndex = new ArrayList<>();
            fAndExpected = new ArrayList<>();
        }

        @Override
        public void test() throws Exception {
            System.out.println("\ncalculating PGN...");

            for (int setI = 0; setI < TOTAL_SET; setI++) {
                long setExe = BenchMarker.now();

                System.out.print("set " + (setI + 1));

                List<String> gameList = Puzzle.set(setI + 1);

                for (int pgnI = 0; pgnI < gameList.size(); pgnI++) {

                    String gameRaw = gameList.get(pgnI);

                    br.__clearAndSetup();
                    MoveRepo repo = MoveRepo.of(REPO_GUARDED, gameRaw);
                    for (MoveMeta moveMeta : repo.metaIterable()) {

                        Piece piece = PieceDiscovery.discover(moveMeta, br);
                        Assert.assertNotNull("PGNCalTest: PieceDiscovery returned null for " + moveMeta, piece);

                        String destSquare = moveMeta.destSquare;
                        if (moveMeta.castle) destSquare = King.getCastleMeta(moveMeta)[2];

                        String res = PGN.translate(piece.brIndex(), Square.index(destSquare), moveMeta.promoteType, br);

                        if (res == null || !res.equals(moveMeta.move)) {
                            fGameId.add(Env.gameIdInDBOf(setI, pgnI));
                            fGame.add(gameRaw);
                            fMoveIndex.add(moveMeta.moveIndex);
                            fMoveMeta.add(moveMeta);
                            fAndExpected.add(String.format("Expected: %s, Calculated: %s", moveMeta.move, res));
                            break;
                        }
                        success++;

                        BRHistory history = MoveMaker.move(moveMeta, br);
                        Assert.assertNotNull(history);
                        history.redo(br);
                    }

                }

                double diff = BenchMarker.diffSec(setExe);
                totalExeTime += diff;
                BenchMarker.log(diff, " took");
            }

            System.out.printf("\nsuccess: %d, failed: %d\n", success, fGameId.size());
            double setExeSec = totalExeTime / TOTAL_SET;
            System.out.printf("all set exec: %.2f sec, set exec avg: %.2f sec, game calc avg: %.2f ms\n", totalExeTime, setExeSec, (setExeSec / 1000) * 1000);

            log();

            Assert.assertEquals(0, fGameId.size());
        }

        @Override
        public void log() {
            System.err.println("\ncalculation error log: ");
            for (int i = 0; i < fGameId.size(); i++) {
                System.err.printf("game id %d, move pair index %d\n", fGameId.get(i), (fMoveIndex.get(i) / 2) + 1);
                System.err.println(fAndExpected.get(i));
                System.err.println(fGame.get(i));
                System.err.println(fMoveMeta.get(i) + "\n");
                System.err.flush();
            }
        }

    }

    @Test
    public void test() throws Exception {
        new T().test();
    }

}
