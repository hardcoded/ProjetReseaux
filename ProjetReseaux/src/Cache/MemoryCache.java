package Cache;
 
import java.util.ArrayList;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;
 
/**
* @author Crunchify.com
*/
 
public class MemoryCache<K, T> {
 
    private long timeToLive;
    private LRUMap crunchifyCacheMap;
 
    protected class CrunchifyCacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public T value;
 
        protected CrunchifyCacheObject(T value) {
            this.value = value;
        }
    }
 
    public MemoryCache(long TimeToLive, final long TimerInterval, int maxItems) {
        this.timeToLive = TimeToLive * 1000;
 
        CacheMap = new LRUMap(maxItems);
 
        if (timeToLive > 0 && TimerInterval > 0) {
 
            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(TimerInterval * 1000);
                        } catch (InterruptedException ex) {
                        }
                        cleanup();
                    }
                }
            });
 
            t.setDaemon(true);
            t.start();
        }
    }
 
    public void put(K key, T value) {
        synchronized (CacheMap) {
            crunchifyCacheMap.put(key, new CrunchifyCacheObject(value));
        }
    }
 
    @SuppressWarnings("unchecked")
    public T get(K key) {
        synchronized (CacheMap) {
            CrunchifyCacheObject c = (CrunchifyCacheObject) crunchifyCacheMap.get(key);
 
            if (c == null)
                return null;
            else {
                c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }
 
    public void remove(K key) {
        synchronized (CacheMap) {
            crunchifyCacheMap.remove(key);
        }
    }
 
    public int size() {
        synchronized (CacheMap) {
            return crunchifyCacheMap.size();
        }
    }
 
    @SuppressWarnings("unchecked")
    public void cleanup() {
 
        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;
 
        synchronized (CacheMap) {
            MapIterator itr = crunchifyCacheMap.mapIterator();
 
            deleteKey = new ArrayList<K>((crunchifyCacheMap.size() / 2) + 1);
            K key = null;
            CrunchifyCacheObject c = null;
 
            while (itr.hasNext()) {
                key = (K) itr.next();
                c = (CrunchifyCacheObject) itr.getValue();
 
                if (c != null && (now > (timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }
 
        for (K key : deleteKey) {
            synchronized (CacheMap) {
                crunchifyCacheMap.remove(key);
            }
 
            Thread.yield();
        }
    }
}