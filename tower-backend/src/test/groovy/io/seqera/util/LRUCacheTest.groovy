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

import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class LRUCacheTest  extends Specification  {

    def 'should cache key' () {

        when:
        def lru = new LRUCache(5)
        and:
        lru.put('a', 1)
        lru.put('b', 2)
        lru.put('c', 3)
        lru.put('d', 4)
        lru.put('e', 5)

        then:
        lru.get('a') == 1
        lru.get('b') == 2
        lru.get('c') == 3
        lru.get('d') == 4
        lru.get('e') == 5


        when:
        lru.put('p', 10)
        lru.put('q', 20)
        then:
        lru.get('p') == 10
        lru.get('q') == 20
        and:
        lru.get('a') == null
        lru.get('b') == null
        lru.get('c') == 3
        lru.size() == 5

    }

}
