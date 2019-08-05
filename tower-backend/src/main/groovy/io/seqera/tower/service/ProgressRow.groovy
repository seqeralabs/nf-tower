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

package io.seqera.tower.service

import groovy.transform.CompileStatic
import io.seqera.tower.enums.TaskStatus

@CompileStatic
class ProgressRow {

    private List cols

    ProgressRow(List cols) { this.cols = cols }

    String getProcess() { cols[0] as String }

    TaskStatus getStatus() { cols[1] as TaskStatus }
    long getCount() { ll(cols[2]) }
    long getTotalCpus() { ll(cols[3]) }
    long getCpuTime() { ll(cols[4]) }
    float getCpuLoad() { dd(cols[5]) }
    long getMemoryRss() { ll(cols[6]) }
    long getMemoryReq() { ll(cols[7]) }
    long getReadBytes() { ll(cols[8]) }
    long getWriteBytes() { ll(cols[9]) }
    long getVolCtxSwitch() { ll(cols[10]) }
    long getInvCtxSwitch() { ll(cols[11]) }


    @CompileStatic
    private long ll(x) {
        x == null ? 0 : x as long
    }

    @CompileStatic
    private float dd(x) {
        x == null ? 0 : x as double
    }
}
