package io.seqera.watchtower.domain

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.databind.ObjectMapper
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import io.seqera.watchtower.pogo.enums.WorkflowStatus

import java.time.Instant


/**
 * Workflow info.
 * @see https://www.nextflow.io/docs/latest/tracing.html#execution-report
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'tasks', 'summaryEntries', 'progress'])
@CompileDynamic
class Workflow {

    static hasMany = [tasks: Task, summaryEntries: SummaryEntry]

    Progress progress


    Instant submit
    Instant start //TODO For now, submitTime and startTime are the same, when using Launchpad they would differ.
    Instant complete

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

    //Multivalue properties encoded as JSON
    String configFiles
    String params

    Manifest manifest
    NextflowMeta nextflow
    Stats stats

    String workflowId


    static embedded = ['manifest', 'nextflow', 'stats', 'progress']

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

    @JsonSetter('start')
    void deserializeStartInstant(String startTimestamp) {
        start = startTimestamp ? Instant.parse(startTimestamp) : null
    }

    @JsonSetter('complete')
    void deserializeCompleteInstant(String completeTimestamp) {
        complete = completeTimestamp ? Instant.parse(completeTimestamp) : null
    }

    @JsonSetter('configFiles')
    void deserializeConfigFilesJson(List<String> configFilesList) {
        configFiles = configFilesList ? new ObjectMapper().writeValueAsString(configFilesList) : null
    }

    @JsonSetter('params')
    void deserializeParamsJson(Map paramsMap) {
        params = paramsMap ? new ObjectMapper().writeValueAsString(paramsMap) : null
    }


    @JsonGetter('workflowId')
    String serializeWorkflowId() {
        id?.toString() ?: workflowId
    }

    @JsonGetter('configFiles')
    def serializeConfigFiles() {
        configFiles ? new ObjectMapper().readValue(configFiles, Object.class) : null
    }

    @JsonGetter('params')
    def serializeParams() {
        params ? new ObjectMapper().readValue(params, Object.class) : null
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
        progress(nullable: true)
    }


    static mapping = {
        version false
        errorReport type: 'text'
    }

}
