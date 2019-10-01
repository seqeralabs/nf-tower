package io.seqera.util

import groovy.transform.CompileStatic

/**
 * Simple Least recently use (LRU) cache
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class LRUCache<K,V> extends LinkedHashMap<K,V> {
    private int maxSize;

    LRUCache(int maxSize) {
        this(maxSize, 16, 0.75f)
    }

    LRUCache(int maxSize, int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
        this.maxSize = maxSize
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > maxSize;
    }

    @Override
    V put(K key, V value) {
        super.put(key, value)
    }
}
