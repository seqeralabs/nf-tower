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

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.databind.ObjectMapper
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import io.seqera.tower.enums.TaskStatus
/**
 * Workflow task info
 * see https://www.nextflow.io/docs/latest/tracing.html#execution-report
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'workflow', 'data'])
@CompileDynamic
class Task implements TaskDef {

    static final private ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()

    static belongsTo = [workflow: Workflow]

    /*
     * Task entity primary
     */
    Long id

    /**
     * Task index as provided by the NF execution
     */
    Long taskId

    /**
     * Current task status
     */
    TaskStatus status

    /**
     * Task metadata and metrics info
     */
    TaskData data

    OffsetDateTime dateCreated
    
    OffsetDateTime lastUpdated

    private TaskData _data() {
        if( data==null )
            data = new TaskData()
        return data
    }

    boolean checkIsSubmitted() {
        status == TaskStatus.SUBMITTED
    }

    boolean checkIsRunning() {
        status == TaskStatus.RUNNING
    }

    boolean checkIsSucceeded() {
        (status == TaskStatus.COMPLETED) && !errorAction
    }

    boolean checkIsFailed() {
        status == TaskStatus.FAILED
    }

    boolean checkIsCached() {
        status == TaskStatus.CACHED
    }

    @JsonSetter('module')
    void deserializeModuleJson(List<String> moduleList) {
        module = moduleList ? mapper.writeValueAsString(moduleList) : null
    }

    @JsonSetter('exit')
    void deserializeExistStatus(Integer exit) {
        exitStatus = exit
    }

    @JsonGetter('exit')
    String serializeExitStatus() {
        exitStatus
    }

    static mapping = {
        data lazy: false
        workflow lazy: true
        status(length: 10)
    }

    static constraints = {
        taskId(unique: 'workflow')
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
    }

    static transients = [
            'hash',
            'name',
            'process',
            'tag',
            'submit',
            'start',
            'complete',
            'module',
            'container',
            'attempt',
            'script',
            'scratch',
            'workdir',
            'queue',
            'cpus' ,
            'memory',
            'disk',
            'time',
            'env',
            'executor',
            'machineType',
            'cloudZone',
            'priceModel',
            'errorAction',
            'exitStatus',
            'duration',
            'realtime',
            'nativeId',
            'pcpu',
            'pmem',
            'rss',
            'vmem',
            'peakRss',
            'peakVmem',
            'rchar',
            'wchar',
            'syscr',
            'syscw',
            'readBytes',
            'writeBytes',
            'volCtxt',
            'invCtxt'
    ]

    // -- getters

    String getHash() { _data().hash }
    String getName() { _data().name }
    String getProcess() { _data().process }
    String getTag() { _data().tag }

    OffsetDateTime getSubmit() { _data().submit }
    OffsetDateTime getStart() { _data().start }
    OffsetDateTime getComplete() { _data().complete }

    String getModule() { _data().module }
    String getContainer() { _data().container }
    Integer getAttempt() { _data().attempt }
    String getScript() { _data().script }
    String getScratch() { _data().scratch }
    String getWorkdir() { _data().workdir }

    String getQueue() { _data().queue }
    Integer getCpus() { _data().cpus }
    Long getMemory() { _data().memory }
    Long getDisk() { _data().disk }
    Long getTime() { _data().time }
    String getEnv() { _data().env }
    String getExecutor() { _data().executor }
    String getMachineType() { _data().machineType }
    String getCloudZone() { _data().cloudZone }
    String getPriceModel() { _data().priceModel }
    String getErrorAction() { _data().errorAction }

    Integer getExitStatus() { _data().exitStatus }
    Long getDuration() { _data().duration }
    Long getRealtime() { _data().realtime }
    String getNativeId() { _data().nativeId }

    Double getPcpu() { _data().pcpu }
    Double getPmem() { _data().pmem }
    Long getRss() { _data().rss }
    Long getVmem() { _data().vmem }
    Long getPeakRss() { _data().peakRss }
    Long getPeakVmem() { _data().peakVmem }
    Long getRchar() { _data().rchar }
    Long getWchar() { _data().wchar }
    Long getSyscr() { _data().syscr }
    Long getSyscw() { _data().syscw }
    Long getReadBytes() { _data().readBytes }
    Long getWriteBytes() { _data().writeBytes }
    Long getVolCtxt() { _data().volCtxt }
    Long getInvCtxt() { _data().invCtxt }

    // -- setters

    void setHash(String x) { _data().hash = x }
    void setName(String x) { _data().name = x }
    void setProcess(String x) { _data().process = x }
    void setTag(String x) { _data().tag = x }

    void setSubmit(OffsetDateTime x) { _data().submit = x }
    void setStart(OffsetDateTime x) { _data().start = x }
    void setComplete(OffsetDateTime x) { _data().complete = x }

    void setModule(String x) { _data().module = x }
    void setContainer(String x ) { _data().container = x }
    void setAttempt(Integer x) { _data().attempt = x }
    void setScript(String x) { _data().script = x }
    void setScratch(String x) { _data().scratch = x }
    void setWorkdir(String x) { _data().workdir = x }

    void setQueue(String x) { _data().queue = x }
    void setCpus(Integer x) { _data().cpus = x }
    void setMemory(Long x) { _data().memory = x }
    void setDisk(Long x) { _data().disk= x }
    void setTime(Long x) { _data().time = x }
    void setEnv(String x) { _data().env = x }
    void setExecutor(String x) { _data().executor = x }
    void setMachineType(String x) { _data().machineType = x }
    void setCloudZone(String x) { _data().cloudZone = x }
    void setPriceModel(String x) { _data().priceModel = x }
    void setErrorAction(String x) { _data().errorAction = x }

    void setExitStatus(Integer x) { _data().exitStatus = x }
    void setDuration(Long x) { _data().duration = x }
    void setRealtime(Long x) { _data().realtime = x }
    void setNativeId(String x) { _data().nativeId = x }

    void setPcpu(Double x) { _data().pcpu = x }
    void setPmem(Double x) { _data().pmem = x }
    void setRss(Long x) { _data().rss = x }
    void setVmem(Long x) { _data().vmem = x }
    void setPeakRss(Long x) { _data().peakRss = x }
    void setPeakVmem(Long x) { _data().peakVmem = x }
    void setRchar(Long x) { _data().rchar = x }
    void setWchar(Long x) { _data().wchar = x }
    void setSyscr(Long x) { _data().syscr = x }
    void setSyscw(Long x) { _data().syscw = x }
    void setReadBytes(Long x) { _data().readBytes = x }
    void setWriteBytes(Long x) { _data().writeBytes = x }
    void setVolCtxt(Long x) { _data().volCtxt = x }
    void setInvCtxt(Long x) { _data().invCtxt = x }

    String toString() {
        "Task[id=$id; taskId=$taskId, status=$status; dataId=${data.id}]"
    }
}
