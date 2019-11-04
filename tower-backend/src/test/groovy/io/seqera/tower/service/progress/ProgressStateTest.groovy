package io.seqera.tower.service.progress


import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class ProgressStateTest extends Specification {

    def 'should return not null entries' () {
        given:
        def state = new ProgressState('xyz-123', ['foo','bar'])

        when:
        def list = state.getProcessLoads()
        then:
        list *. process == ['foo','bar']
    }

    def 'should return a named load' () {
        when:
        def state = new ProgressState('xyz-123', ['foo','bar'])
        then:
        state.getState('foo').process == 'foo'
        state.getState('bar').process == 'bar'
        state.getState('gamma').process == 'gamma'
    }

    def 'should be equals' () {
        when:
        def s1 = new ProgressState('abc', ['foo','bar'])
        def s2 = new ProgressState('abc', ['foo','bar'])
        def s3 = new ProgressState('abc', ['alpha','beta'])
        then:
        s1 == s2
        s1 != s3
    }


}
