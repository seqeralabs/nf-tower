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

import groovy.transform.CompileStatic

@CompileStatic
interface ProgressState {

    long getPending()
    long getRunning()
    long getSubmitted()
    long getSucceeded()
    long getFailed()
    long getCached()

    long getTotalCpus()
    long getCpuTime()
    double getCpuLoad()
    long getMemoryRss()
    long getMemoryReq()
    long getReadBytes()
    long getWriteBytes()
    long getVolCtxSwitch()
    long getInvCtxSwitch()

}
