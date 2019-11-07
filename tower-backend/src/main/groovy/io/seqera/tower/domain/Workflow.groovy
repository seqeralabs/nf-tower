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
import io.seqera.tower.enums.WorkflowStatus
/**
 * Workflow info.
 *  see https://www.nextflow.io/docs/latest/tracing.html#execution-report
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'tasks', 'owner', 'project'])
@CompileDynamic
class Workflow {

    static final private ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()

    String id
    static hasMany = [tasks: Task]
    static belongsTo = [owner: User, project: Project]

    OffsetDateTime submit
    OffsetDateTime start //TODO For now, submitTime and startTime are the same, when using Launchpad they would differ.
    OffsetDateTime complete
    OffsetDateTime dateCreated
    OffsetDateTime lastUpdated
    
    Boolean resume
    Boolean success

    String sessionId

    String projectDir
    String profile
    String homeDir
    String workDir
    String container
    String commitId
    String errorMessage
    String repository
    String containerEngine
    String scriptFile
    String userName
    String launchDir
    String runName
    String errorReport
    String scriptId
    String revision
    Integer exitStatus
    String commandLine
    String projectName
    String scriptName

    Long duration
    WorkflowStatus status

    //Multi-value properties encoded as JSON
    String configFiles
    String configText
    String params

    WfManifest manifest
    WfNextflow nextflow
    WfStats stats
    Boolean deleted

    @Deprecated Long peakLoadCpus
    @Deprecated Long peakLoadTasks
    @Deprecated Long peakLoadMemory

    static embedded = ['manifest', 'nextflow', 'stats']

    boolean checkIsRunning() {
        getStatus() == WorkflowStatus.RUNNING
    }

    boolean checkIsComplete() {
        return complete!=null || status==WorkflowStatus.SUCCEEDED || status==WorkflowStatus.FAILED
    }

    boolean checkIsSucceeded() {
        getStatus() == WorkflowStatus.SUCCEEDED
    }

    boolean checkIsFailed() {
        getStatus() == WorkflowStatus.FAILED
    }

    WorkflowStatus getStatus() {
        status ?: computeStatus()
    }

    WorkflowStatus computeStatus() {
        if( complete==null )
            return WorkflowStatus.RUNNING
        ( success
            ? WorkflowStatus.SUCCEEDED
            : WorkflowStatus.FAILED )
    }

    @JsonSetter('configFiles')
    void deserializeConfigFilesJson(List<String> configFilesList) {
        configFiles = configFilesList ? mapper.writeValueAsString(configFilesList) : null
    }

    @JsonSetter('params')
    void deserializeParamsJson(Map paramsMap) {
        params = paramsMap ? mapper.writeValueAsString(paramsMap) : null
    }

    @JsonGetter('configFiles')
    def serializeConfigFiles() {
        configFiles ? mapper.readValue(configFiles, Object.class) : null
    }

    @JsonGetter('params')
    def serializeParams() {
        params ? mapper.readValue(params, Object.class) : null
    }

    @Override
    String toString() {
        "Workflow[id=$id]"
    }

    static constraints = {
        id(maxSize: 16)
        runName(unique: 'sessionId', maxSize: 80) // <-- the runName has to be unique for the same sessionId
        sessionId(maxSize: 36)

        resume(nullable: true)
        success(nullable: true)
        complete(nullable: true)
        status(nullable: true)

        commandLine(maxSize: 8096)
        params(nullable: true)
        commitId(nullable: true, maxSize: 40)
        configFiles(nullable: true)
        configText(nullable: true)
        repository(nullable: true)
        scriptId(nullable: true, maxSize: 40)
        revision(nullable: true, maxSize: 40)
        container(nullable: true)
        containerEngine(nullable: true)
        exitStatus(nullable: true)
        duration(nullable: true)
        errorReport(nullable: true)
        errorMessage(nullable: true)
        userName(maxSize: 40)
        profile(maxSize: 100)
        projectName(maxSize: 100)
        scriptName(maxSize: 100)
        project(nullable: true)
        manifest(nullable: true)
        nextflow(nullable: true)
        stats(nullable: true)

        peakLoadCpus(nullable: true)
        peakLoadTasks(nullable: true)
        peakLoadMemory(nullable: true)

        deleted(nullable: true)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
    }

    static mapping = {
        id(generator: 'assigned')
        errorReport(type: 'text')
        errorMessage(type: 'text')
        params(type: 'text')
        configFiles(type: 'text')
        configText(type: 'text')
        tasks( cascade: 'save-update')
        status(length: 10)
    }

    def beforeValidate() {
        if( deleted == null ) deleted = false
    }

}




