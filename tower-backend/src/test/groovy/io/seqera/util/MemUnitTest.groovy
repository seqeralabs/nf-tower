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

package io.seqera.utilclass


import io.seqera.util.MemUnit
import spock.lang.Specification

class MemUnitTest extends Specification {

    def 'should convert mem unit to string'() {

        expect:
        new MemUnit(1000).toString() == '1000 B'
        new MemUnit(1024).toString() == '1 KB'
        new MemUnit(1500).toString() == '1.5 KB'

        new MemUnit(2 * 1024 * 1024).toString() == '2 MB'
        new MemUnit(3 * 1024L * 1024L * 1024L).toString() == '3 GB'
        new MemUnit(4 * 1024L * 1024L * 1024L * 1024L).toString() == '4 TB'
        new MemUnit(5 * 1024L * 1024L * 1024L * 1024L * 1024L).toString() == '5 PB'
        new MemUnit(6 * 1024L * 1024L * 1024L * 1024L * 1024L * 1024L).toString() == '6 EB'

    }


    def 'should create mem unit from string'() {

        expect:
        new MemUnit('1').getBytes() == 1
        new MemUnit('2B').getBytes() == 2
        new MemUnit('3 B').getBytes() == 3

        new MemUnit('1KB').getBytes() == 1024
        new MemUnit('2 KB').getBytes() == 2 * 1024
        new MemUnit('3.5 KB').getBytes() == 3.5 * 1024
        new MemUnit('5K').getBytes() == 5 * 1024

        new MemUnit('1MB').getBytes() == 1024 * 1024
        new MemUnit('2 MB').getBytes() == 2 * 1024 * 1024
        new MemUnit('3.5 MB').getBytes() == 3.5 * 1024 * 1024
        new MemUnit('3.5 M').getBytes() == 3.5 * 1024 * 1024

        new MemUnit('1GB').getBytes() == 1024 * 1024 * 1024L
        new MemUnit('2 GB').getBytes() == 2 * 1024 * 1024 * 1024L
        new MemUnit('3.5 GB').getBytes() == 3.5 * 1024 * 1024 * 1024L
        new MemUnit('4G').getBytes() == 4 * 1024 * 1024 * 1024L

        new MemUnit('1TB').getBytes() == 1024 * 1024 * 1024L * 1024L
        new MemUnit('2 TB').getBytes() == 2 * 1024 * 1024 * 1024L * 1024L
        new MemUnit('3.5 TB').getBytes() == 3.5 * 1024 * 1024 * 1024L * 1024L
        new MemUnit('25 TB').getBytes() == 25 * 1024 * 1024 * 1024L * 1024L

        new MemUnit('1PB').getBytes() == 1024 * 1024 * 1024L * 1024L * 1024L
        new MemUnit('2 PB').getBytes() == 2 * 1024 * 1024 * 1024L * 1024L * 1024L
        new MemUnit('3.5 PB').getBytes() == 3.5 * 1024 * 1024 * 1024L * 1024L * 1024L
        new MemUnit('35 P').getBytes() == 35 * 1024 * 1024 * 1024L * 1024L * 1024L

        new MemUnit('1000 KB').getBytes() == 1000 * 1024

        when:
        new MemUnit('1,000 KB')
        then:
        thrown(IllegalArgumentException)
    }

    def 'test getters'() {

        expect:
        new MemUnit('3.5 PB').bytes == 3.5 * 1024 * 1024 * 1024L * 1024L * 1024L
        new MemUnit('3.5 PB').kilo == 3.5 * 1024 * 1024 * 1024L * 1024L
        new MemUnit('3.5 PB').mega == 3.5 * 1024 * 1024 * 1024L
        new MemUnit('3.5 PB').giga == 3.5 * 1024 * 1024

    }

    def 'test equals and compare'() {

        expect:
        new MemUnit('1GB') == new MemUnit('1GB')
        new MemUnit('1M') < new MemUnit('1GB')
        new MemUnit('1G') > new MemUnit('1M')

    }

    def 'test conversion'() {

        def mem

        when:
        mem = new MemUnit('100 M')
        then:
        mem.getGiga() == 0
        mem.getMega() == 100
        mem.getKilo() == 100 * 1024L
        mem.getBytes() == 100 * 1024L * 1024L

        when:
        mem = new MemUnit('5G')
        then:
        mem.getGiga() == 5
        mem.getMega() == 5 * 1024L
        mem.getKilo() == 5 * 1024L * 1024L
        mem.getBytes() == 5 * 1024L * 1024L * 1024L


        when:
        mem = new MemUnit(100_000)
        then:
        mem.getBytes() == 100_000
        mem.getKilo() == 97  // note: this is floor rounded  (97,65625)
        mem.getMega() == 0
        mem.getGiga() == 0
    }

    def 'should multiply memory'() {

        expect:
        new MemUnit('2GB') * 3 == new MemUnit('6GB')
        new MemUnit('2GB') * 1.5 == new MemUnit('3GB')
        // `multiply` a number by a MemUnit is implemented by `NumberDelegatingMetaClass`

    }

    def 'should divide memory'() {

        expect:
        new MemUnit('4GB') / 2 == new MemUnit('2GB')
        new MemUnit('3GB') / 1.5 == new MemUnit('2GB')

    }

    def 'should add memory'() {
        expect:
        new MemUnit('1GB') + new MemUnit('2GB') == new MemUnit('3GB')
    }

    def 'should subtract memory'() {
        expect:
        new MemUnit('5GB') - new MemUnit('2GB') == new MemUnit('3GB')
    }

    def 'should validate groovy truth'() {
        expect:
        !new MemUnit(0)
        new MemUnit(1)
    }

    def 'should validate to unit method'() {
        expect:
        MemUnit.of(STR).toUnit(UNIT) == EXPECT

        where:
        STR      | UNIT | EXPECT
        '2 MB'   | 'B'  | 2 * 1024 * 1024
        '2 MB'   | 'KB' | 2 * 1024
        '2 MB'   | 'MB' | 2
        '2 MB'   | 'GB' | 0
        '3.5 GB' | 'KB' | 3.5 * 1024 * 1024
    }
}
