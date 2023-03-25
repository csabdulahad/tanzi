package database;

import lib.helper.BenchMarker;
import database.puzzle_set.*;

import java.sql.*;

public class PuzzleDB {

    private static PuzzleDB INSTANCE = null;
    private Connection connection;

    private PuzzleDB() {
        createConnection();
    }

    public static PuzzleDB getInstance() {
        if (INSTANCE == null) INSTANCE = new PuzzleDB();
        return INSTANCE;
    }

    public Connection getConnection() {
        return connection;
    }

    public void createConnection() {
        if (connection != null) return;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + Environment.DB_PATH.replace("\\", "/"));
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Failed with excep " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.err.println("database connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeSql(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
    }

    public ResultSet executeAndReturn(String sql) {
        ResultSet resultSet = null;
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            resultSet = statement.getResultSet();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return resultSet;
    }

    public void dumpPuzzleSet() {
        PuzzleSet[] puzzleSets = {
                new PuzzleSet1(), new PuzzleSet2(), new PuzzleSet3(), new PuzzleSet4(), new PuzzleSet5(),
                new PuzzleSet6(), new PuzzleSet7(), new PuzzleSet8(), new PuzzleSet9(), new PuzzleSet10(),
                new PuzzleSet11(), new PuzzleSet12(), new PuzzleSet13(), new PuzzleSet14(), new PuzzleSet15(),
                new PuzzleSet16(), new PuzzleSet17(), new PuzzleSet18(), new PuzzleSet19(), new PuzzleSet20(),
                new PuzzleSet21(), new PuzzleSet22(), new PuzzleSet23(), new PuzzleSet24(), new PuzzleSet25(),
                new PuzzleSet26(), new PuzzleSet27(), new PuzzleSet28(), new PuzzleSet29(), new PuzzleSet30(),
                new PuzzleSet31(), new PuzzleSet32(), new PuzzleSet33(), new PuzzleSet34(), new PuzzleSet35(),
                new PuzzleSet36(), new PuzzleSet37(), new PuzzleSet38(), new PuzzleSet39(), new PuzzleSet40(),
                new PuzzleSet41(), new PuzzleSet42()
        };
        BenchMarker benchmarker = new BenchMarker();
        for (PuzzleSet puzzleSet : puzzleSets) {
            try {
                puzzleSet.dumpPuzzle(this);
            } catch (SQLException e) {
                System.err.printf("Problem dumping puzzle set %d\n", puzzleSet.getPuzzleSetId());
                e.printStackTrace();
                break;
            }
        }
        benchmarker.log("Puzzle set insertion time");
    }

}
