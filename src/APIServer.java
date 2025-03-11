import java.sql.SQLException;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;

public class APIServer {
    private Database db;
    private Hashtable<Integer, String> cache = new Hashtable<>();  // Cache (e.g. redis)
    private Hashtable<Integer, Semaphore> semaphoreMap = new Hashtable<Integer, Semaphore>();
    private Hashtable<Integer, String> responseMap = new Hashtable<Integer, String>();
    private int cacheHits = 0;
    private int cacheMisses = 0;

    APIServer() throws SQLException, ClassNotFoundException {
        this.db = new Database();
    }


    String getMovieName(int id) {
        // Always get from cache first
        String movie = cache.get(id);
        if (movie != null) {
            cacheHits++;
            return movie;
        }

        // Note - This is still not perfect, as rather than allowing just 1 thread to goto db, still a small number
        // would go through as many threads together could get the semaphore not present in the map. But we are not
        // solving it for the extreme optimization case.
        Semaphore sema = semaphoreMap.get(id);
        if (sema == null) {
            // No one gone to db yet for this request, do it now and block others
            Semaphore reqSemaphore = new Semaphore(1); // allow only 1 thread to go in.
            System.out.println("Semaphore created");
            semaphoreMap.putIfAbsent(id, reqSemaphore);

            try {
                reqSemaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // query db
            movie = db.getMovieNameFromID(id);
            cache.put(id, movie);
            cacheMisses++;

            reqSemaphore.release(100);
            semaphoreMap.remove(id);
            responseMap.put(id, movie);
        }
        else {
            try {
                sema.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            movie = responseMap.get(id);
            cacheHits++;
        }
        return movie;
    }

    void cacheStats() {
        System.out.println("\n\nCache hit/miss: " + cacheHits + "/" + cacheMisses);
    }
}
