package database;

import java.sql.SQLException;

public interface PuzzleSet {
    void dumpPuzzle(PuzzleDB puzzleDB) throws SQLException;
    int getPuzzleSetId();
}
