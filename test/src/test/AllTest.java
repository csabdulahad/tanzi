package test;

import database.Environment;
import database.PuzzleDB;
import lib.helper.BenchMarker;
import tanzi.TestTanzi;
import tanzi.algorithm.King;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tanzi.staff.Arbiter;
import tanzi.staff.BoardRegistry;
import tanzi.staff.MoveAnalyzer;
import tanzi.staff.MoveMaker;
import tanzi.staff.MoveRepo;
import tanzi.staff.PGN;
import tanzi.staff.PGNBuilder;
import tanzi.staff.PieceDiscovery;
import tanzi.model.MoveMeta;
import tanzi.model.Piece;
import tanzi.model.Square;
import tanzi.pool.meta.BRMeta;
import tanzi.pool.meta.PieceMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AllTest {

    public static int totalPuzzleSet = 42;  // total puzzle set we have 42

    Arbiter arbiter;
    BoardRegistry br;

    @Before
    public void setup() {
        Environment.check();
        br = new BoardRegistry();
        arbiter = new Arbiter(br);
    }

    public void logPool() {
        BRMeta.log();
        PieceMeta.log();
    }

    public void resetLog() {
        BRMeta.reset();
        PieceMeta.reset();
    }

    @Test
    public void copyPGNToBR() throws Exception {
        resetLog();

        System.out.print("\nPGN to BR copy\n");

        BenchMarker marker = new BenchMarker();
        BenchMarker setExecution;

        for (int puzzleSetIndex = 1; puzzleSetIndex <= totalPuzzleSet; puzzleSetIndex++) {
            System.out.printf("Set %d ", puzzleSetIndex);
            System.out.flush();

            setExecution = new BenchMarker();

            ArrayList<String> pgnList = getPuzzle(puzzleSetIndex);

            int start = 0;
            int finish = pgnList.size();
            for (int key = start; key < finish; key++) { // each PGN
                String pgnUnsplitted = pgnList.get(key);
                String[] pgn = pgnUnsplitted.split(",");

                arbiter.__clearAndSetup();
                PGN.writeTo(pgn, arbiter.getBR());
            }

            setExecution.benchmark("executed in ");
        }
        marker.benchmark("PGN copy to BR execution took");
        logPool();
    }

    @Test
    public void createPGN() throws Exception {
        resetLog();

        System.out.println("\nExecuting PGN Move Calculation");

        boolean showErrorLog = true;

        BenchMarker benchMarker = new BenchMarker();

        ArrayList<Integer> failedPuzzleSetIndex = new ArrayList<>();
        ArrayList<Integer> failedPGNIndex = new ArrayList<>();
        ArrayList<String> failedPGN = new ArrayList<>();
        ArrayList<String> failedAndExpectedPGN = new ArrayList<>();
        ArrayList<String> pgnGenerated = new ArrayList<>();
        ArrayList<Integer> failedMoveIndexInPGN = new ArrayList<>();
        ArrayList<MoveMeta> failedPGNMoveMeta = new ArrayList<>();

        int successfulCalculation = 0;
        int failedCalculation = 0;

        BenchMarker setExecution;
        for (int puzzleSetIndex = 1; puzzleSetIndex <= totalPuzzleSet; puzzleSetIndex++) {
            if (failedPGN.size() > 0) System.out.printf("so far failed: %d%n%n", failedPGN.size());
            System.out.printf("Set %d ", puzzleSetIndex);
            System.out.flush();

            setExecution = new BenchMarker();
            ArrayList<String> pgnList = getPuzzle(puzzleSetIndex);

            int pgnIndex = 0;

            int start = 0;
            int finish = pgnList.size();
            for (int key = start; key < finish; key++) { // each PGN
                pgnIndex++;
                arbiter.__clearAndSetup();

                String pgnUnsplitted = pgnList.get(key);

                String[] pgn = pgnUnsplitted.split(",");

                int moveIndexInPGN = 0;

                MoveRepo moveRepo = MoveRepo.guardedRepo(pgn);
                while (moveRepo.hasNext()) {
                    moveIndexInPGN++;

                    String move = moveRepo.nextMove();
                    MoveMeta moveMeta = MoveAnalyzer.analyze(move);
                    moveMeta.color = moveRepo.whoseTurn();

                    Piece piece = PieceDiscovery.discover(moveMeta, arbiter);
                    Assert.assertNotNull(piece);

                    String destSquare = moveMeta.destSquare;
                    if (moveMeta.castle) {
                        destSquare = King.getCastleMeta(moveMeta)[2];
                    }

                    moveMeta.pgn = PGN.generate(pgn);
                    moveMeta.puzzleSet = puzzleSetIndex;
                    moveMeta.pgnIndex = pgnIndex;
                    moveMeta.moveIndex = moveIndexInPGN;

                    String pgnValue = PGNBuilder.pgn(piece.brIndex(), Square.index(destSquare), moveMeta.promoteType, arbiter);

                    if (pgnValue == null || !pgnValue.equals(moveMeta.move)) {
                        failedPuzzleSetIndex.add(puzzleSetIndex);
                        failedPGNIndex.add(pgnIndex);
                        failedMoveIndexInPGN.add(moveIndexInPGN);
                        pgnGenerated.add(PGN.generate(pgn));
                        failedPGN.add(pgnUnsplitted);
                        failedPGNMoveMeta.add(moveMeta);
                        failedAndExpectedPGN.add(String.format("%s, %s", move, pgnValue));
                        failedCalculation++;
                        break;
                    } else {
                        successfulCalculation++;
                    }


                    boolean result = MoveMaker.move(moveMeta, arbiter, null);
                    Assert.assertTrue(result);
                }
            }
            setExecution.benchmark("executed in ");
        }

        if (showErrorLog) {
            for (int i = 0; i < failedPuzzleSetIndex.size(); i++) {
                System.err.printf("Puzzle Set %d, PGN Id %d, Move Pair Index %d%n", failedPuzzleSetIndex.get(i), failedPGNIndex.get(i), (failedMoveIndexInPGN.get(i) / 2) + 1);
                System.err.println("PGN Output: " + failedAndExpectedPGN.get(i));
                System.err.print(pgnGenerated.get(i));
                System.err.println(failedPGNMoveMeta.get(i));
                System.err.println(failedPGN.get(i) + "\n\n");
                System.err.flush();
            }
        }

        System.out.flush();

        System.out.printf("%nSuccess: %d, Failed: %d%n", successfulCalculation, failedCalculation);
        benchMarker.benchmark("PGN calculation execution time");
        logPool();
        Assert.assertEquals(failedCalculation, 0);
    }

    @Test
    public void playPGN() {
        resetLog();

        System.out.println("\nExecuting PGN Playing");

        ArrayList<String> failedPGN = new ArrayList<>();

        int totalPGNExecuted = 0;
        int totalPGNFailed = 0;

        BenchMarker benchMarker = new BenchMarker();
        BenchMarker setExecution;

        for (int puzzleSetIndex = 1; puzzleSetIndex <= totalPuzzleSet; puzzleSetIndex++) {
            System.out.printf("Set %d ", puzzleSetIndex);
            System.out.flush();

            setExecution = new BenchMarker();

            ArrayList<String> pgnList = getPuzzle(puzzleSetIndex);

            ArrayList<Integer> failedPGNIndexList = new ArrayList<>();
            ArrayList<MoveMeta> failedMoveMetaList = new ArrayList<>();
            ArrayList<Exception> exceptionList = new ArrayList<>();
            ArrayList<Integer> moveIndexInPGNList = new ArrayList<>();

            int pgnIndex = 0;
            int start = 0;
            int finish = pgnList.size();

            for (int key = start; key < finish; key++) { // each PGN
                arbiter.__clearAndSetup();

                String pgn = pgnList.get(key);
                String[] puzzle = pgn.split(",");
                MoveRepo moveRepo = MoveRepo.guardedRepo(puzzle);

                pgnIndex++;
                totalPGNExecuted++;

                int moveIndexInPGN = 0;
                while (moveRepo.hasNext()) {
                    String move = moveRepo.nextMove();

                    MoveMeta moveMeta = MoveAnalyzer.analyze(move);
                    moveMeta.color = moveRepo.whoseTurn();

                    try {
                        boolean result = MoveMaker.move(moveMeta, arbiter, null);
                        Assert.assertTrue(result);
                    } catch (Exception e) {
                        totalPGNFailed++;
                        failedPGN.add(PGN.generate(puzzle));
                        failedPGNIndexList.add(pgnIndex);
                        failedMoveMetaList.add(moveMeta);
                        exceptionList.add(e);
                        moveIndexInPGNList.add(moveIndexInPGN);
                        break;
                    }
                    moveIndexInPGN++;
                }
            }
            setExecution.benchmark("executed in ");

            for (int i = 0; i < failedPGNIndexList.size(); i++) {
                System.err.printf("Puzzle set %d ", puzzleSetIndex);
                System.err.printf("PGN index %d ", failedPGNIndexList.get(i));
                System.err.printf("Move index in PGN %d ", moveIndexInPGNList.get(i));
                System.err.printf("\nFailed PGN %s", failedPGN.get(i));
                System.err.println(failedMoveMetaList.get(i) + "\n\n\n");
                System.err.flush();
            }

            Assert.assertEquals(0, failedPGNIndexList.size());
            Assert.assertEquals(0, moveIndexInPGNList.size());
            Assert.assertEquals(0, failedMoveMetaList.size());
        }

        System.out.printf("Total PGN executed %d, Success %d, Failed %d\n", totalPGNExecuted, totalPGNExecuted - totalPGNFailed, totalPGNFailed);
        System.out.flush();
        benchMarker.benchmark("PGN Playing Execution time : ");
        logPool();

        Assert.assertEquals(totalPGNFailed, 0);
    }

    private static ArrayList<String> getPuzzle(int puzzleSet) {
        int startFrom = (puzzleSet - 1) * 1000;

        /* get all the puzzle */
        ArrayList<String> pgnList = new ArrayList<>();
        try {
            PuzzleDB puzzleDB = PuzzleDB.getInstance();
            ResultSet resultSet = puzzleDB.executeAndReturn("SELECT * FROM pgn LIMIT 1000 OFFSET " + startFrom);
            while (resultSet.next()) {
                String pgn = resultSet.getString("problem") + "," + resultSet.getString("solution");
                pgnList.add(pgn);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pgnList;
    }

    private static String getPuzzleAt(int puzzleSet, int pgnIndex) {

        int startFrom = (puzzleSet - 1) * 1000;
        int id = startFrom + pgnIndex;

        try {
            PuzzleDB puzzleDB = PuzzleDB.getInstance();
            ResultSet resultSet = puzzleDB.executeAndReturn("SELECT * FROM pgn WHERE id = " + id);
            if (!resultSet.next()) return null;

            String pgn = resultSet.getString("problem") + "," + resultSet.getString("solution");
            resultSet.close();
            return pgn;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}