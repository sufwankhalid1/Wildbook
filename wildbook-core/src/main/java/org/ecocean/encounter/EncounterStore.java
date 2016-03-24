package org.ecocean.encounter;

import org.ecocean.util.LRUCache;

public class EncounterStore {
    public final static String CACHE_NAME = "encounter.store";
    private final LRUCache<Integer, Encounter> cache;

    public EncounterStore(final int cacheSize) {
        cache = new LRUCache<>(CACHE_NAME, cacheSize);
    }

    public Encounter get(final Integer id) {
        return cache.get(id);
    }

    public void put(final Encounter obj) {
        cache.put(obj.getId(), obj);
    }

    public void remove(final Integer id) {
        cache.remove(id);
    }
}
