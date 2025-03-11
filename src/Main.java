import java.sql.*;
import java.util.Hashtable;
import java.util.Random;

// Prototype - Request Hedging, Debouncing using Semaphore Map,
// If a large number of requests arrive at the same time for a missing key in cache, they will all fail from cache,
// only one should hit the DB (not to overload the db) and update the cache, rest all should get it from the cache.

public class Main {
    public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException {
        APIServer server = new APIServer();
        int threadCount = 100;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; ++i) {
            threads[i] = new Thread(new Executor(i, server));
            threads[i].start();
        }

        for (int i = 0; i < threadCount; ++i)
            threads[i].join();

        server.cacheStats();
    }

    static class Executor extends Thread {
        private int id;
        private APIServer server;

        Executor(int id, APIServer server) {
            this.id = id;
            this.server = server;
        }

        @Override
        public void run() {
            Random random = new Random();
            int min = 0;
            int max = 1099; // max movie id in the table

            for (int i = 0; i < 1; ++i) {
                int movieId = random.nextInt(max - min) + min;
                String movieName = server.getMovieName(120);
                System.out.println(movieId + " - " +movieName );
            }
        }
    }
}