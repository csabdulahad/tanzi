package test;

public class Env {

    private static final int AVAILABLE_MAX_SET = 40;
    public static final int TOTAL_SET = AVAILABLE_MAX_SET;

    // as puzzles are executed a set of 1000 games, so the test suites can
    // use this method to get the actual in-db-id for the game when tests run
    // through all the games.
    public static int gameIdInDBOf(int set, int gameId) {
        return (set * 1000) + (gameId + 1);
    }

}
