package org.ecocean.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(LRUCache.class);
    private static final long serialVersionUID = 1L;
    private final String name;
    private int cacheSize;

    /**
     * @param name used solely to identify this LRU for debugging purposes. Can be null if you don't care.
     * @param cacheSize size of cache
     */
    public LRUCache(final String name, final int cacheSize) {
        super(16, 0.75f, true);
        this.cacheSize = cacheSize;
        this.name = name;
    }

    public void setCacheSize(final int cacheSize) {
        this.cacheSize = cacheSize;
    }

    private boolean isFull() {
        return size() >= cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
        boolean isfull = isFull();

        if (isfull && logger.isDebugEnabled()) {
            //
            // Printing this out will allow you to see how often this is called. If way too frequently
            // you might want to bump up the cache size. It will get called, that is the nature of an LRU,
            // but you don't want it called so frequently that we might as well not even have a cache.
            //
            logger.debug("LRUCache [" + name + "] is full, dropping [" + eldest.getValue() + "].");
        }

        return isfull;
    }
}