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

import java.time.OffsetDateTime

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
interface TaskDef {

    // -- getters

    String getHash()
    String getName()
    String getProcess()
    String getTag()

    OffsetDateTime getSubmit()
    OffsetDateTime getStart()
    OffsetDateTime getComplete()

    String getModule()
    String getContainer()
    Integer getAttempt()
    String getScript()
    String getScratch()
    String getWorkdir()

    String getQueue()
    Integer getCpus()
    Long getMemory()
    Long getDisk()
    Long getTime()
    String getEnv()
    String getExecutor()
    String getMachineType()

    String getErrorAction()

    Integer getExitStatus()
    Long getDuration()
    Long getRealtime()
    String getNativeId()

    Double getPcpu()
    Double getPmem()
    Long getRss()
    Long getVmem()
    Long getPeakRss()
    Long getPeakVmem()
    Long getRchar()
    Long getWchar()
    Long getSyscr()
    Long getSyscw()
    Long getReadBytes()
    Long getWriteBytes()

    Long getVolCtxt()
    Long getInvCtxt()


    // -- setters

    void setHash(String x)
    void setName(String x)
    void setProcess(String x)
    void setTag(String x)

    void setSubmit(OffsetDateTime x)
    void setStart(OffsetDateTime x)
    void setComplete(OffsetDateTime x)

    void setModule(String x)
    void setContainer(String x)
    void setAttempt(Integer x)
    void setScript(String x)
    void setScratch(String x)
    void setWorkdir(String x)

    void setQueue(String x)
    void setCpus(Integer x)
    void setMemory(Long x)
    void setDisk(Long x)
    void setTime(Long x)
    void setEnv(String x)
    void setExecutor(String x)
    void setMachineType(String x)
    void setErrorAction(String x)

    void setExitStatus(Integer x)
    void setDuration(Long x)
    void setRealtime(Long x)
    void setNativeId(String x)

    void setPcpu(Double x)
    void setPmem(Double x)
    void setRss(Long x)
    void setVmem(Long x)
    void setPeakRss(Long x)
    void setPeakVmem(Long x)
    void setRchar(Long x)
    void setWchar(Long x)
    void setSyscr(Long x)
    void setSyscw(Long x)
    void setReadBytes(Long x)
    void setWriteBytes(Long x)

    void setVolCtxt(Long x)
    void setInvCtxt(Long x)

}
