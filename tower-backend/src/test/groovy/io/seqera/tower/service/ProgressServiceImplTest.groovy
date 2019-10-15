package io.seqera.tower.service

import java.time.OffsetDateTime
import java.time.ZoneOffset

import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class ProgressServiceImplTest extends Specification {

    def 'should compute duration' () {

        given:
        def svc = new ProgressServiceImpl()

        when:
        def ret0 = svc.computeDuration(null)
        then:
        ret0 == null

        when:
        def ts1 = OffsetDateTime.now()
        sleep 100
        and:
        def ret1 = svc.computeDuration(ts1)
        and:
        then:
        ret1 >= 100
        ret1 < 200

        when:
        ZoneOffset zoneOffSet= ZoneOffset.of("-08:00");
        def ts2 = OffsetDateTime.now(zoneOffSet)
        sleep 100
        and:
        def ret2 = svc.computeDuration(ts2)
        then:
        ret2 >= 100
        ret2 < 200

    }
}
