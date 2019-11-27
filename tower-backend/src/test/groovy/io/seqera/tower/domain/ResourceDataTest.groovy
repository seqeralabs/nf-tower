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

package io.seqera.tower.domain

import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class ResourceDataTest extends Specification {

    def 'should should sanitize text fields' () {
        given:
        def EXPECT = 'x' * 255
        def LONGER = 'x' * 256
        assert LONGER.size() == 256
        and:
        def res = new ResourceData()
        res.q1Label = LONGER
        res.q2Label = LONGER
        res.q3Label = LONGER
        res.minLabel = LONGER
        res.maxLabel = LONGER

        when:
        res.sanitize()
        then:
        res.hasWarnings()
        res.getWarnings().size() == 5
        and:
        res.q1Label == EXPECT
        res.q2Label == EXPECT
        res.q3Label == EXPECT
        res.minLabel == EXPECT
        res.maxLabel == EXPECT
    }
}
