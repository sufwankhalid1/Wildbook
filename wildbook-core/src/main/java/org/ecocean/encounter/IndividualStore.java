package org.ecocean.encounter;

import org.ecocean.Individual;
import org.ecocean.util.LRUCache;

public class IndividualStore {
    private final LRUCache<Integer, Individual> cache;

    public IndividualStore(final int cacheSize) {
        cache = new LRUCache<>(cacheSize);
    }

    public Individual get(final Integer id) {
        return cache.get(id);
    }

    public void put(final Individual obj) {
        cache.put(obj.getId(), obj);
    }

    public void remove(final Individual obj) {
        cache.remove(obj.getId());
    }

    public void remove(final Integer id) {
        cache.remove(id);
    }
}
