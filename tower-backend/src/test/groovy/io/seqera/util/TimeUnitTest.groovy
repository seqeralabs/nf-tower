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

import java.time.Instant

import spock.lang.Specification

class TimeUnitTest extends Specification {

    final long SEC = 1000
    final long MIN = 60 * SEC
    final long HOUR = 60 * MIN
    final long DAY = 24 * HOUR

    def 'test create by string'() {

        expect:
        TimeUnit.of('123 millis').toMillis() == 123
        TimeUnit.of('123 ms').toMillis() == 123
        TimeUnit.of('123ms').toMillis() == 123

        TimeUnit.of('5 seconds').toSeconds() == 5
        TimeUnit.of('5 second').toSeconds() == 5
        TimeUnit.of('5 sec').toSeconds() == 5
        TimeUnit.of('5 s').toSeconds() == 5
        TimeUnit.of('5s').toSeconds() == 5

        TimeUnit.of('5 minutes').toSeconds() == 300
        TimeUnit.of('5 minute').toSeconds() == 300
        TimeUnit.of('5 min').toSeconds() == 300
        TimeUnit.of('5min').toSeconds() == 300

        TimeUnit.of('5 hours').toMinutes() == 300
        TimeUnit.of('5 hour').toMinutes() == 300
        TimeUnit.of('5 h').toMinutes() == 300
        TimeUnit.of('5h').toMinutes() == 300

        TimeUnit.of('1 days').toHours() == 24
        TimeUnit.of('1 day').toHours() == 24
        TimeUnit.of('1 d').toHours() == 24
        TimeUnit.of('1d').toHours() == 24
        TimeUnit.of('1days').toHours() == 24
        TimeUnit.of('1day').toHours() == 24
        TimeUnit.of('1d').toHours() == 24

    }

    def 'should not be parsed as a duration'() {
        when:
        new TimeUnit('live_in_3d')
        then:
        thrown(IllegalArgumentException)

        when:
        new TimeUnit('/path/to/samples/2016-06-05_21:04:05/sample.bam')
        then:
        thrown(IllegalArgumentException)
    }

    def 'should parse multi unit time format'() {
        expect:
        TimeUnit.of('1d 2h').toMillis() == DAY + 2 * HOUR
        TimeUnit.of('1 d 2 h').toMillis() == DAY + 2 * HOUR
        TimeUnit.of('2d3h4m').toMillis() == 2 * DAY + 3 * HOUR + 4 * MIN
        TimeUnit.of('2d 3h 4m').toMillis() == 2 * DAY + 3 * HOUR + 4 * MIN
    }

    def 'should parse float time'() {
        expect:
        TimeUnit.of('10.5 s').toMillis() == 10_500
        TimeUnit.of('10.5 m').toSeconds() == 630
    }

    def 'should parse legacy time format string'() {

        expect:
        TimeUnit.of('1:0:0').toString() == '1h'
        TimeUnit.of('01:00:00').toString() == '1h'
        TimeUnit.of('10:00:00').toString() == '10h'
        TimeUnit.of('01:02:03').toString() == '1h 2m 3s'
    }


    def 'test format'() {

        when:
        def duration = new TimeUnit('5min')

        then:
        duration.duration == 5 * 60 * 1000
        duration.toMillis() == 5 * 60 * 1000
        duration.toSeconds() == 5 * 60
        duration.toMinutes() == 5

        duration.format('ss') == '300'
        duration.format('mm:ss') == '05:00'
        duration.toString() == '5m'

    }

    def 'test toString'() {

        expect:
        new TimeUnit(100).toString() == '100ms'
        new TimeUnit(1_000).toString() == '1s'
        new TimeUnit(1_100).toString() == '1.1s'
        new TimeUnit(32_300).toString() == '32.3s'
        new TimeUnit(61 * 1000).toString() == '1m 1s'
        new TimeUnit(61 * 1000 + 200).toString() == '1m 1s'
        new TimeUnit(61 * 1000 + 800).toString() == '1m 2s'
        new TimeUnit(60 * 60 * 1000 + 1000).toString() == '1h 1s'
        new TimeUnit(25 * 60 * 60 * 1000 + 1000).toString() == '1d 1h 1s'

    }

    def 'test equals and compare'() {
        expect:
        new TimeUnit('1h') == new TimeUnit('1h')
        new TimeUnit('10min') < new TimeUnit('11min')
        new TimeUnit('20min') > new TimeUnit('11min')

    }

    def 'should not convert UUID number'() {

        when:
        new TimeUnit('10833d95-1546-4BDF-AEA6-9F9676571854')
        then:
        thrown(IllegalArgumentException)

    }

    def 'should add time'() {

        expect:
        new TimeUnit('1 hour') + new TimeUnit('3 hours') == new TimeUnit('4 hours')

    }

    def 'should subtract time'() {

        expect:
        new TimeUnit('4 hour') - new TimeUnit('3 hours') == new TimeUnit('1 hour')

    }

    def 'should multiple time'() {
        expect:
        new TimeUnit('2 hour') * 3 == new TimeUnit('6 hours')
        new TimeUnit('6 hours') * 1.5 == new TimeUnit('9 hours')
        // `multiply` a number by a MemUnit is implemented by `NumberDelegatingMetaClass`
    }

    def 'should divide time'() {
        expect:
        new TimeUnit('6 hour') / 2 == new TimeUnit('3 hours')
        new TimeUnit('9 hours') / 1.5 == new TimeUnit('6 hours')
    }

    def 'should validate groovy truth'() {
        expect:
        !new TimeUnit(0)
        new TimeUnit(1)
    }


    def 'should validate duration between'() {

        given:
        def start = Instant.now()
        def end = start.plusMillis(1000)

        expect:
        TimeUnit.between(start, end) == TimeUnit.of('1sec')
    }
}
