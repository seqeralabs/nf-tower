package io.seqera.watchtower.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.databind.ObjectMapper
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

import java.time.Instant

@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'configFiles'])
@CompileDynamic
/**
 * Workflow info.
 * @see https://www.nextflow.io/docs/latest/tracing.html#execution-report
 */
class Workflow {

    static hasMany = [tasks: Task, magnitudeSummaries: MagnitudeSummary]

    Instant submit
    Instant start //TODO For now, submitTime and startTime are the same, when using Launchpad they would differ.
    Instant complete

    Boolean resume
    Boolean success

    String workflowId
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

    //Multivalue properties
    String configFiles
    Map params

    Manifest manifest
    NextflowMeta nextflow
    Stats stats

    static embedded = ['manifest', 'nextflow', 'stats']

    static transients = ['workflowId']

    boolean checkIsStarted() {
        (complete == null)
    }

    boolean checkIsSucceeded() {
        complete && success
    }

    boolean checkIsFailed() {
        complete && (success != null) && !success
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
    }


    static mapping = {
        version false
    }

}
