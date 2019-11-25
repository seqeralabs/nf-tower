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
