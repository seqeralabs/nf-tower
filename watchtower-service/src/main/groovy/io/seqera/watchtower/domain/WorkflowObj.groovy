package io.seqera.watchtower.domain

import java.time.Instant

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

/**
 * Model Workflow execution request
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@CompileDynamic
class WorkflowObj {

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

//    List<String> configFiles
    Map params

    Manifest manifest
    NextflowMeta nextflow
    Stats stats

    static embedded = ['manifest', 'nextflow', 'stats']

    static transients = ['started', 'succeeded', 'failed', 'workflowId']

    boolean isStarted() {
        (complete == null)
    }

    boolean isSucceeded() {
        complete && success
    }

    boolean isFailed() {
        complete && (success != null) && !success
    }

    static constraints = {
        sessionId(unique: 'runName')

        resume(nullable: true)
        success(nullable: true)
        complete(nullable: true)

        completeTime(nullable: true)
        params(nullable: true)
//        configFiles(nullable: true)
        containerEngine(nullable: true)
        exitStatus(nullable: true)
        duration(nullable: true)
        errorReport(nullable: true)
        errorMessage(nullable: true)
    }

}
