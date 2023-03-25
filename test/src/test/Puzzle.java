package test;

import database.PuzzleDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Puzzle {
    private int setIndex = 1;
    int index = -1;
    private ArrayList<String> buffer;

    public Puzzle() {
        buffer = new ArrayList<>();
    }

    public String next() {
        index++;
        if (index == 1000) {
            setIndex++;
            if (setIndex > Env.TOTAL_SET) return null;

            index = 0;
            buffer.clear();
        }

        if (buffer.size() == 0) {
            System.out.println("Puzzle set :\t" + setIndex);
            buffer = set(setIndex);
        }

        return buffer.get(index);
    }

    public static ArrayList<String> set(int setNumber) {
        int startFrom = (setNumber - 1) * 1000;

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

    private static String puzzle(int id) {

        try {
            PuzzleDB puzzleDB = PuzzleDB.getInstance();
            ResultSet resultSet = puzzleDB.executeAndReturn("SELECT * FROM pgn WHERE id = " + id);
            if (!resultSet.next()) return null;

            String pgn = resultSet.getString("problem") + "," + resultSet.getString("solution");
            resultSet.close();
            return pgn;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
