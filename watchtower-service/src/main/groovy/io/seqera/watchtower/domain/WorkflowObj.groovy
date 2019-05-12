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

    String workflowId
    String sessionId
    Instant start
    boolean resume
    boolean success
    boolean complete
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
    String  exitStatus
    String commandLine
    String projectName
    String scriptName
    long duration
    List<String> configFiles
    Manifest manifest
    NextflowMeta nextflow
    Stats stats
    Map params

    static embedded = ['manifest', 'nextflow', 'stats']

}
