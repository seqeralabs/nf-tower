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

package io.seqera.tower.exchange.trace

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Process progress metadata as computed by NF
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString(includePackage = false, includeNames = true)
@CompileStatic
class TraceProgressDetail {

    int index
    String name
    int pending
    int submitted
    int running
    int succeeded
    int cached
    int failed
    int aborted
    int stored
    int ignored
    int retries
    boolean terminated

    long loadCpus
    long loadMemory
    int peakRunning
    long peakCpus
    long peakMemory

}
