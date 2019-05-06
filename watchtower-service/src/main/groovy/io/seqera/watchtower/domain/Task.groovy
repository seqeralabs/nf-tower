package io.seqera.watchtower.domain

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import io.seqera.watchtower.pogo.enums.TaskStatus

import java.time.Instant

@Entity
@CompileDynamic
/**
 * Workflow task info.
 * @see https://www.nextflow.io/docs/latest/tracing.html#execution-report
 */
class Task {

    static belongsTo = [workflow: Workflow]

    /**
     * The order of the task in the workflow
     */
    Long taskId
    String hash
    String name
    String process
    String tag

    TaskStatus currentStatus

    Instant submitTime
    Instant startTime
    Instant completeTime

    String module //Multi-value field (encoded as JSON)

    String container
    Integer attempt
    String script
    String scratch
    String workdir

    String queue
    Integer cpus
    Long memory
    Long disk
    String time
    String env


    String errorAction

    Long exit
    Long duration
    Long realtime
    Long nativeId

    Double cpuPercentage
    Double memPercentage
    Long rss
    Long vmem
    Long peakRss
    Long peakVmem
    Long rchar
    Long wchar
    Long syscr
    Long syscw
    Long readBytes
    Long writeBytes

    Long volCtxt
    Long invCtxt


    static mapping = {
        version false
    }

    static constraints = {
        taskId(unique: 'workflow')

        workflow(nullable: true)
        process(nullable: true)
        tag(nullable: true)
        currentStatus(nullable: true)
        exit(nullable: true)
        startTime(nullable: true)
        completeTime(nullable: true)
        module(nullable: true)
        container(nullable: true)
        attempt(nullable: true)
        script(nullable: true)
        scratch(nullable: true)
        workdir(nullable: true)
        queue(nullable: true)
        cpus(nullable: true)
        memory(nullable: true)
        disk(nullable: true)
        time(nullable: true)
        env(nullable: true)
        errorAction(nullable: true)
        duration(nullable: true)
        realtime(nullable: true)
        nativeId(nullable: true)
        cpuPercentage(nullable: true)
        memPercentage(nullable: true)
        rss(nullable: true)
        vmem(nullable: true)
        peakRss(nullable: true)
        peakVmem(nullable: true)
        rchar(nullable: true)
        wchar(nullable: true)
        syscr(nullable: true)
        syscw(nullable: true)
        readBytes(nullable: true)
        writeBytes(nullable: true)
        volCtxt(nullable: true)
        invCtxt(nullable: true)
    }

}