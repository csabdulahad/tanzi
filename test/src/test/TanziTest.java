package test;

import tanzi.staff.BoardRegistry;

import java.util.ArrayList;

public abstract class TanziTest {
    protected BoardRegistry br;

    protected double totalExeTime;

    protected int success;

    protected ArrayList<Integer> fGameId;
    protected ArrayList<String> fGame;

    public TanziTest() {
        br = new BoardRegistry();
        fGameId = new ArrayList<>();
        fGame = new ArrayList<>();
    }

    public abstract void test() throws Exception;

    public abstract void log();

}
