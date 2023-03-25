package lib.helper;

public class BenchMarker {

    long start, end;
    
    public BenchMarker() {
        start = System.currentTimeMillis();
    }

    public static BenchMarker start() {
        return new BenchMarker();
    }

    public void end() {
        end = System.currentTimeMillis();
    }

    public void log(String message) {
        System.out.printf("%s %.2f sec%n", message, getExecution());
    }

    public double getExecution() {
        double total = (double) end - start;
        return (total / 1000);
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long diff(long start) {
        return now() - start;
    }

    public static double diffSec(long start) {
        return (now() - start) / 1000d;
    }

    public static void log(double sec, String message) {
        System.out.printf("%s %.2f sec%n", message, sec);
    }

}
