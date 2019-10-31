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
        def list = state.getProcesses()
        then:
        list *. process == ['foo','bar']
    }
}
