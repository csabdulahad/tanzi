package lib.helper;
/*
 * this class customizes console output so that debug messages can be found easily
 * */

public class Console {

    private static final String TAG = "ahad";

    public static void log(Class cl, String message) {
         System.out.println(cl.getSimpleName() + " > " + message);
    }

}
