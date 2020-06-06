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


import spock.lang.Ignore
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class CompactUuidTest extends Specification {


    def 'should encode-decode longs' () {

        expect:
        CompactUuid.bytesToLong(CompactUuid.longToBytes(NUM)) == NUM

        where: 
        NUM     || _
        0       || _
        1       || _
        -1      || _
        128     || _
        -128    || _
        Long.MAX_VALUE || _
        Long.MIN_VALUE || _

    }
    
    def 'should encode-decode uuid' () {
        given:
        def uuid = UUID.randomUUID()
        expect:
        CompactUuid.bytesToUuid(CompactUuid.uuidToBytes(uuid)) == uuid
    }

    def 'should encode-decode key' () {
        given:

        def uuid = UUID.randomUUID()
        when:
        def key = CompactUuid.encode(uuid)
        then:
        key

        when:
        def copy = CompactUuid.decodeToUuid(key)
        def bytes = CompactUuid.decodeToBytes(key)
        then:
        copy == uuid
        bytes == CompactUuid.uuidToBytes(uuid)
    }

    @Ignore
    def 'min-max len' () {

        when:
        int min = Integer.MAX_VALUE
        int max = 0
        1_000_000.times {
            def id = CompactUuid.generate()
            min = Math.min(id.length(),min)
            max = Math.max(id.length(),max)
        }

        println "min=$min; max=$max"
        then:
        true

    }
}
