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

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import groovy.transform.CompileStatic

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class H8WarnFilter extends Filter<ILoggingEvent> {
    @Override
    FilterReply decide(ILoggingEvent event) {
        return stripPrefix(event.message) ? FilterReply.DENY : FilterReply.NEUTRAL
    }

    static private boolean stripPrefix(String str) {
        if( str.startsWith('Cannot decrypt / verify JWT: Invalid JWT serialization: Missing dot delimiter') )
            return true
        if( str.startsWith("HHH90000022:") )
            return true
        return false
    }
}
