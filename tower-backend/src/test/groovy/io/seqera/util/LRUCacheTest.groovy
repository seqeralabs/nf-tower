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
