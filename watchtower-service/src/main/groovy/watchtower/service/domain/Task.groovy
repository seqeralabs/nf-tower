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

    Long exit

    Instant submitTime
    Instant startTime
    Instant completeTime

    String module
    String container
    Integer attempt
    String script
    String scratch
    String workdir

    String queue
    Integer cpus
    String memory
    String disk
    String time
    String env

    String errorAction

    Long duration
    Long realtime
    Long native_id

    static constraints = {
        task_id(unique: 'id')

        process(nullable: true)
        tag(nullable: true)
        exit(nullable: true)
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
        native_id(nullable: true)
    }

}