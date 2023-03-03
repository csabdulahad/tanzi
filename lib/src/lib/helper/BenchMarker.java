package lib.helper;

public class BenchMarker {

    long start;

    public BenchMarker() {
        start = System.currentTimeMillis();
    }

    public void benchmark(String message) {
        long end = System.currentTimeMillis();
        double total = (double) end - start;
        double sec = (total / 1000);
        System.out.printf("%s %.2f sec%n", message, sec);
    }

}
