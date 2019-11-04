import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.concurrent.ConcurrentMap;

/**
 * synchronize by the value of object
 *
 * http://antkorwin.com/concurrency/synchronization_by_value.html
 */
public class StringMutex {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    private static final ConcurrentReferenceHashMap.ReferenceType DEFAULT_REFERENCE_TYPE =
            ConcurrentReferenceHashMap.ReferenceType.SOFT;

    private final ConcurrentMap<String, Mutex> map;

    public StringMutex() {
        this.map = new ConcurrentReferenceHashMap<>(DEFAULT_INITIAL_CAPACITY,
                DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, DEFAULT_REFERENCE_TYPE);
    }

    public Mutex getMutex(String key) {
        return this.map.computeIfAbsent(key, Mutex::new);

    }

    public static class Mutex {
        private final String key;

        public Mutex(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Mutex mutex = (Mutex) o;

            return key != null ? key.equals(mutex.key) : mutex.key == null;
        }

        @Override
        public int hashCode() {
            return key != null ? key.hashCode() : 0;
        }
    }
}
