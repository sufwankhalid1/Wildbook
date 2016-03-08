package org.ecocean.encounter;

import org.ecocean.util.LRUCache;

public class EncounterStore {
    private final LRUCache<Integer, Encounter> cache;

    public EncounterStore(final int cacheSize) {
        cache = new LRUCache<>(cacheSize);
    }

    public Encounter get(final Integer id) {
        return cache.get(id);
    }

    public void put(final Encounter obj) {
        cache.put(obj.getId(), obj);
    }

    public void remove(final Encounter obj) {
        cache.remove(obj.getId());
    }

    public void remove(final Integer id) {
        cache.remove(id);
    }
}
