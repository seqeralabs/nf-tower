/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

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
