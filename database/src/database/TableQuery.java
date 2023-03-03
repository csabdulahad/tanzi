package database;

public class TableQuery {

    public static final String TABLE_PUZZLE = "CREATE TABLE IF NOT EXISTS 'pgn' (" +
            "'id' INTEGER, " +
            "'game_key' TEXT UNIQUE, " +
            "'game_type' TEXT, " +
            "'white_name' TEXT, " +
            "'white_point' INTEGER, " +
            "'black_name' TEXT, " +
            "'black_point' INTEGER, " +
            "'problem' TEXT, " +
            "'solution' TEXT, " +
            "'game_link' TEXT, " +
            "PRIMARY KEY('id' AUTOINCREMENT));";
}
