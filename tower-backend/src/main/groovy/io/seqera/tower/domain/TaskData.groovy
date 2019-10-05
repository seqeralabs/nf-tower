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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'workflow', 'sessionId'])
@CompileDynamic
class TaskData implements TaskDef {

    /*
     * Task entity primary
     */
    Long id

    /**
     * Workflow session ID as provided by NF
     */
    String sessionId

    /**
     * Task unique hash ID
     */
    String hash

    /**
     * Task name
     */
    String name

    /**
     * Task process name
     */
    String process

    /**
     * Task tag string
     */
    String tag

    OffsetDateTime submit
    OffsetDateTime start
    OffsetDateTime complete

    String module
    String container
    Integer attempt
    String script
    String scratch
    String workdir

    String queue
    Integer cpus
    Long memory
    Long disk
    Long time
    String env

    String errorAction

    Integer exitStatus
    Long duration
    Long realtime
    String nativeId

    Double pcpu
    Double pmem
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

    static constraints = {
        sessionId(maxSize: 36)

        hash(maxSize: 34)
        process(nullable: true, maxSize: 255)
        tag(nullable: true, maxSize: 255)
        name(maxSize: 255)
        exitStatus(nullable: true)
        submit(nullable: true)
        start(nullable: true)
        complete(nullable: true)
        module(nullable: true, maxSize: 100)
        container(nullable: true, maxSize: 255)
        attempt(nullable: true)
        script(nullable: true)
        scratch(nullable: true)
        workdir(nullable: true, maxSize: 512)
        queue(nullable: true, maxSize: 100)
        cpus(nullable: true)
        memory(nullable: true)
        disk(nullable: true)
        time(nullable: true)
        env(nullable: true, maxSize: 2048)
        errorAction(nullable: true, maxSize: 10)
        duration(nullable: true)
        realtime(nullable: true)
        nativeId(nullable: true, maxSize: 100)
        pcpu(nullable: true)
        pmem(nullable: true)
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

    static mapping = {
        script(type: 'text')
    }

    String toString() {
        "TaskData[id=$id]"
    }
}
