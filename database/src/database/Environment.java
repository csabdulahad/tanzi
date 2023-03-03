package database;

import lib.helper.Console;
import lib.helper.Pref;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class Environment {

    public static final String PATH_SEPARATOR = File.separator;
    public static final String USER_HOME = System.getProperty("user.home");

    public static final String TANZI_ENGIN = USER_HOME + PATH_SEPARATOR + "TanziEngine";
    public static final String DB_PATH = TANZI_ENGIN + PATH_SEPARATOR + "puzzle.db";

    public static final String KEY_HELLO_LOAD = "helloLoad";

    public static void check() {
        Console.log(Environment.class, "checking environment...");

        File envFolder = new File(TANZI_ENGIN);
        File db = new File(DB_PATH);

        if (envFolder.exists()) {
            System.out.println("Environment has been checked");
            return;
        }

        // create database file and table for the first hello load
        try {
            envFolder.mkdir();
            Console.log(Environment.class, "program folder created");

            db.createNewFile();
            Console.log(Environment.class, "database file created");

            PuzzleDB puzzleDB = PuzzleDB.getInstance();

            puzzleDB.executeSql(TableQuery.TABLE_PUZZLE);
            Console.log(Environment.class, "database table created");

            Console.log(Environment.class, "inserting puzzle dataset...");
            puzzleDB.dumpPuzzleSet();
            Console.log(Environment.class, "puzzle dataset inserted");

            puzzleDB.getConnection().commit();
            Pref.putBoolean(Environment.KEY_HELLO_LOAD, false);

            System.out.println("Environment has been prepared");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.err.println("Exception in preparing environment");
            System.err.flush();
        }
    }

    public static URL getResource(String path) {
        return Environment.class.getResource(path);
    }

}
