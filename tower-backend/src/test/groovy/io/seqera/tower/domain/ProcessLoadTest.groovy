package io.seqera.tower.domain

import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class ProcessLoadTest extends Specification {

    def 'should be equals' () {
        when:
        def load1 = new ProcessLoad(process: 'foo')
        def load2 = new ProcessLoad(process: 'foo')
        def load3 = new ProcessLoad(process: 'bar')
        then:
        load1 == load2
        load1 != load3
    }
}
