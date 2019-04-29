package watchtower.service.domain

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import watchtower.service.pogo.enums.TaskStatus

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
    Long task_id
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


    String error_action

    Long exit
    Long duration
    Long realtime
    Long native_id

    Double cpuPercentage
    Double memPercentage
    Long rss
    Long vmem
    Long peak_rss
    Long peak_vmem
    Long rchar
    Long wchar
    Long syscr
    Long syscw
    Long read_bytes
    Long write_bytes

    Long vol_ctxt
    Long inv_ctxt


    static constraints = {
        task_id(unique: 'workflow')

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
        error_action(nullable: true)
        duration(nullable: true)
        realtime(nullable: true)
        native_id(nullable: true)
        cpuPercentage(nullable: true)
        memPercentage(nullable: true)
        rss(nullable: true)
        vmem(nullable: true)
        peak_rss(nullable: true)
        peak_vmem(nullable: true)
        rchar(nullable: true)
        wchar(nullable: true)
        syscr(nullable: true)
        syscw(nullable: true)
        read_bytes(nullable: true)
        write_bytes(nullable: true)
        vol_ctxt(nullable: true)
        inv_ctxt(nullable: true)
    }

}