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

package io.seqera.tower.service.progress

import static io.seqera.tower.enums.TaskStatus.*

import com.fasterxml.jackson.annotation.JsonGetter
import io.seqera.tower.domain.Task
import io.seqera.tower.enums.TaskStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Common logic for task and workflow progress metadata
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
trait ProgressRecord {

    private static final Logger log = LoggerFactory.getLogger(ProgressRecord)

    private Map<TaskStatus,Long> taskStatuses = new HashMap<>(6);

    abstract long getCpus();
    abstract long getCpuTime();
    abstract long getCpuLoad();
    abstract long getMemoryRss();
    abstract long getMemoryReq();
    abstract long getReadBytes();
    abstract long getWriteBytes();
    abstract long getVolCtxSwitch();
    abstract long getInvCtxSwitch();

    abstract void setCpus(long value);
    abstract void setCpuTime(long value);
    abstract void setCpuLoad(long value);
    abstract void setMemoryRss(long value);
    abstract void setMemoryReq(long value);
    abstract void setReadBytes(long value);
    abstract void setWriteBytes(long value);
    abstract void setVolCtxSwitch(long value);
    abstract void setInvCtxSwitch(long value);

    @JsonGetter('memoryEfficiency')
    float getMemoryEfficiency() {
        if( memoryReq==0 ) return 0
        return memoryRss / memoryReq * 100 as float
    }

    @JsonGetter('cpuEfficiency')
    float getCpuEfficiency() {
        if( cpuTime==0 ) return 0
        return cpuLoad / cpuTime * 100 as float
    }

    abstract long getLoadTasks()
    abstract long getLoadCpus()
    abstract long getLoadMemory()
    abstract long getPeakCpus()
    abstract long getPeakTasks()
    abstract long getPeakMemory()

    abstract void setLoadTasks(long value)
    abstract void setLoadCpus(long value)
    abstract void setLoadMemory(long value)
    abstract void setPeakCpus(long value)
    abstract void setPeakTasks(long value)
    abstract void setPeakMemory(long value)

    long getStatus( TaskStatus status ) {
        taskStatuses.getOrDefault(status, 0L)
    }

    void setStatus( TaskStatus status, long value ) {
        taskStatuses.put(status, value)
    }

    void incStatus( TaskStatus status, long value=1 ) {
        setStatus(status, getStatus(status) +value)
    }

    void decStatus( TaskStatus status, long value=1 ) {
        final newValue = getStatus(status) -value
        if( newValue >= 0 )
            setStatus(status, newValue)
        else
            log.warn "Unexpected progress status negative value: status=$status; value=$value; newValue=$newValue -- ignoring it"
    }

    long getPending() { getStatus(NEW) }
    long getSubmitted() { getStatus(SUBMITTED) }
    long getRunning() { getStatus(RUNNING) }
    long getSucceeded() { getStatus(COMPLETED) }
    long getFailed() { getStatus(FAILED) }
    long getCached() { getStatus(CACHED) }

    void setPending(long value) { setStatus(NEW, value) }
    void setSubmitted(long value) { setStatus(SUBMITTED, value) }
    void setRunning(long value) { setStatus(RUNNING, value) }
    void setSucceeded(long value) { setStatus(COMPLETED, value) }
    void setFailed(long value) { setStatus(FAILED, value) }
    void setCached(long value) { setStatus(CACHED, value) }

    void incLoad(Task task) {
        loadTasks += 1
        loadCpus += task.cpus ?: 0
        loadMemory += task.memory ?: 0
    }

    void decLoad(Task task) {
        final newTasks = loadTasks - 1
        final newCpus = loadCpus - (task.cpus ?: 0)
        final newMemory = loadMemory - (task.memory ?: 0)
        if( newTasks < 0 ) {
            log.warn "Unexpected negative load tasks value: current=$loadTasks; newTasks=$newTasks -- ignoring it"
            return
        }
        if( newCpus < 0 ) {
            log.warn "Unexpected negative load cpus value: current=$loadCpus; newCpus=$newCpus -- ignoring it"
            return
        }
        if( newCpus < 0 ) {
            log.warn "Unexpected negative load memory value: current=$loadMemory; newMemory=$newMemory -- ignoring it"
            return
        }
        loadTasks = newTasks
        loadCpus = newCpus
        loadMemory = newMemory
    }

    void updatePeaks() {
        peakTasks = Math.max(peakTasks, loadTasks)
        peakCpus = Math.max(peakCpus, loadCpus)
        peakMemory = Math.max(peakMemory, loadMemory)
    }

    void incStats(Task task) {
        cpus += _l(task.cpus)
        cpuTime += _l(task.cpus) * _l(task.realtime)
        cpuLoad += _f(task.pcpu) /100 * _l(task.realtime) as long
        memoryRss += _l(task.peakRss)
        memoryReq += _l(task.memory)
        readBytes += _l(task.rchar)
        writeBytes += _l(task.wchar)
        volCtxSwitch += _l(task.volCtxt)
        invCtxSwitch += _l(task.invCtxt)
    }

    private long _l(value) { value!=null ? value as long : 0 }
    private float _f(value) { value!=null ? value as float : 0 }

    void incStatsAndLoad(ProgressRecord that) {

        for( TaskStatus status : values() )
            incStatus(status, that.getStatus(status))

        cpus += that.cpus
        cpuTime += that.cpuTime
        cpuLoad += that.cpuLoad
        memoryRss += that.memoryRss
        memoryReq += that.memoryReq
        readBytes += that.readBytes
        writeBytes += that.writeBytes
        volCtxSwitch += that.volCtxSwitch
        invCtxSwitch += that.invCtxSwitch

        loadTasks += that.loadTasks
        loadCpus += that.loadCpus
        loadMemory += that.loadMemory
    }

}


