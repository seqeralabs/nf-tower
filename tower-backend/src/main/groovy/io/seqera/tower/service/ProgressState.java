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

package io.seqera.tower.service;


import java.util.Map;

import io.seqera.tower.enums.TaskStatus;
import static io.seqera.tower.enums.TaskStatus.*;

public interface ProgressState {

    Map<TaskStatus,Long> getTaskCount();

    default long getPending() { return getTaskCount().getOrDefault(NEW, 0L); }
    default long getRunning()  { return getTaskCount().getOrDefault(RUNNING, 0L); }
    default long getSubmitted()  { return getTaskCount().getOrDefault(SUBMITTED, 0L); }
    default long getSucceeded()  { return getTaskCount().getOrDefault(COMPLETED, 0L); }
    default long getFailed()  { return getTaskCount().getOrDefault(FAILED, 0L); }
    default long getCached()  { return getTaskCount().getOrDefault(CACHED, 0L); }

    long getTotalCpus();
    long getCpuTime();
    float getCpuLoad();
    long getMemoryRss();
    long getMemoryReq();
    long getReadBytes();
    long getWriteBytes();
    long getVolCtxSwitch();
    long getInvCtxSwitch();

    default void sumTaskCount( TaskStatus status, long value ) {
        long x = getTaskCount().getOrDefault(status, 0L);
        getTaskCount().put(status, x + value);
    }

}


