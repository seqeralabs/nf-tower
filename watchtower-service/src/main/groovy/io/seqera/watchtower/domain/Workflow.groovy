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

package io.seqera.watchtower.domain

import java.time.OffsetDateTime

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.databind.ObjectMapper
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import io.seqera.watchtower.pogo.enums.WorkflowStatus
/**
 * Workflow info.
 *  see https://www.nextflow.io/docs/latest/tracing.html#execution-report
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'tasks', 'summaryEntries', 'tasksProgress', 'processesProgress', 'owner'])
@CompileDynamic
class Workflow {

    static final private ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()

    static hasMany = [tasks: Task, summaryEntries: SummaryEntry, processesProgress: ProcessProgress]
    static belongsTo = [owner: User]

    TasksProgress tasksProgress


    OffsetDateTime submit
    OffsetDateTime start //TODO For now, submitTime and startTime are the same, when using Launchpad they would differ.
    OffsetDateTime complete

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
    String exitStatus
    String commandLine
    String projectName
    String scriptName

    Long duration

    //Multi-value properties encoded as JSON
    String configFiles
    String params

    Manifest manifest
    NextflowMeta nextflow
    Stats stats

    String workflowId


    static embedded = ['manifest', 'nextflow', 'stats']

    static transients = ['workflowId']

    boolean checkIsStarted() {
        computeStatus() == WorkflowStatus.STARTED
    }

    boolean checkIsSucceeded() {
        computeStatus() == WorkflowStatus.SUCCEEDED
    }

    boolean checkIsFailed() {
        computeStatus() == WorkflowStatus.FAILED
    }

    private computeStatus() {
        (!complete) ? WorkflowStatus.STARTED   :
        (success)   ? WorkflowStatus.SUCCEEDED :
                      WorkflowStatus.FAILED

    }

    @JsonSetter('configFiles')
    void deserializeConfigFilesJson(List<String> configFilesList) {
        configFiles = configFilesList ? mapper.writeValueAsString(configFilesList) : null
    }

    @JsonSetter('params')
    void deserializeParamsJson(Map paramsMap) {
        params = paramsMap ? mapper.writeValueAsString(paramsMap) : null
    }


    @JsonGetter('workflowId')
    String serializeWorkflowId() {
        id?.toString() ?: workflowId
    }

    @JsonGetter('configFiles')
    def serializeConfigFiles() {
        configFiles ? mapper.readValue(configFiles, Object.class) : null
    }

    @JsonGetter('params')
    def serializeParams() {
        params ? mapper.readValue(params, Object.class) : null
    }


    static constraints = {
        sessionId(unique: 'runName')

        resume(nullable: true)
        success(nullable: true)
        complete(nullable: true)

        params(nullable: true)
        configFiles(nullable: true)
        containerEngine(nullable: true)
        exitStatus(nullable: true)
        duration(nullable: true)
        errorReport(nullable: true)
        errorMessage(nullable: true)

        manifest(nullable: true)
        nextflow(nullable: true)
        stats(nullable: true)

        tasksProgress(nullable: true)
    }


    static mapping = {
        version false
        errorReport(type: 'text')
        params(type: 'text')
        configFiles(type: 'text')
    }

}
