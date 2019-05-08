package io.seqera.watchtower.domain

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import io.seqera.watchtower.pogo.enums.WorkflowStatus

import java.time.Instant

@Entity
@CompileDynamic
/**
 * Workflow info.
 * @see https://www.nextflow.io/docs/latest/tracing.html#execution-report
 */
class Workflow {

    static hasMany = [tasks: Task, magnitudeSummaries: MagnitudeSummary]

    WorkflowStatus currentStatus

    //Tasks progress data
    Integer running
    Integer submitted
    Integer failed
    Integer pending
    Integer succeeded
    Integer cached

    //Timestamps
    Instant submitTime
    Instant startTime //TODO For now, submitTime and startTime are the same, when using Launchpad they would differ.
    Instant completeTime

    //Multi-value fields (JSON encoded)
    String params
    String configFiles
    //Manifest fields
    String manifestNextflowVersion
    String manifestDefaultBranch
    String manifestVersion
    String manifestHomePage
    String manifestGitmodules
    String manifestDescription
    String manifestName
    String manifestMainScript
    String manifestAuthor
    //Nextflow info fields
    String nextflowVersion
    Integer nextflowBuild
    String nextflowTimestamp

    //Identification fields
    String sessionId
    String runName
    //Miscellaneous
    String complete
    String profile
    String homeDir
    String workDir
    String container
    String commitId
    String repository
    String containerEngine
    String scriptFile
    String userName
    String launchDir
    String projectDir
    String scriptId
    String revision
    String commandLine
    String scriptName
    Integer exitStatus
    Long duration
    //Fail-related fields
    String errorReport
    String errorMessage
    //Stats fields
    String computeTimeFmt
    Integer cachedCount
    Long cachedDuration
    Long failedDuration
    Long succeedDuration
    Integer failedCount
    Double cachedPct
    String cachedCountFmt
    String succeedCountFmt
    Double failedPct
    String failedCountFmt
    String ignoredCountFmt
    Integer ignoredCount
    Double succeedPct
    Integer succeedCount
    Double ignoredPct


    static mapping = {
        version false
    }

    static constraints = {
        sessionId(unique: 'runName')

        completeTime(nullable: true)
        params(nullable: true)
        configFiles(nullable: true)
        manifestNextflowVersion(nullable: true)
        manifestDefaultBranch(nullable: true)
        manifestVersion(nullable: true)
        manifestHomePage(nullable: true)
        manifestGitmodules(nullable: true)
        manifestDescription(nullable: true)
        manifestName(nullable: true)
        manifestMainScript(nullable: true)
        manifestAuthor(nullable: true)
        nextflowVersion(nullable: true)
        nextflowBuild(nullable: true)
        nextflowTimestamp(nullable: true)
        complete(nullable: true)
        profile(nullable: true)
        homeDir(nullable: true)
        workDir(nullable: true)
        container(nullable: true)
        commitId(nullable: true)
        repository(nullable: true)
        containerEngine(nullable: true)
        scriptFile(nullable: true)
        userName(nullable: true)
        launchDir(nullable: true)
        projectDir(nullable: true)
        scriptId(nullable: true)
        revision(nullable: true)
        commandLine(nullable: true)
        scriptName(nullable: true)
        exitStatus(nullable: true)
        duration(nullable: true)
        errorReport(nullable: true)
        errorMessage(nullable: true)
        computeTimeFmt(nullable: true)
        cachedCount(nullable: true)
        cachedDuration(nullable: true)
        failedDuration(nullable: true)
        succeedDuration(nullable: true)
        failedCount(nullable: true)
        cachedPct(nullable: true)
        cachedCountFmt(nullable: true)
        succeedCountFmt(nullable: true)
        failedPct(nullable: true)
        failedCountFmt(nullable: true)
        ignoredCountFmt(nullable: true)
        ignoredCount(nullable: true)
        succeedPct(nullable: true)
        succeedCount(nullable: true)
        ignoredPct(nullable: true)
    }

}
