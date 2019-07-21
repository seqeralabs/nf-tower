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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
/**
 * Models execution metrics information collected by nextflow.
 * The {@link Workflow} has a collection of {@link WorkflowMetrics} objects.
 *
 * Each {@link WorkflowMetrics} holds the usage metrics (cpus, mem, vmem, etc) for a
 * specific process
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version'])
@CompileDynamic
class WorkflowMetrics {

    /*
     * The process name
     */
    String process

    ResourceData cpu
    ResourceData mem
    ResourceData vmem
    ResourceData time
    ResourceData reads
    ResourceData writes
    ResourceData cpuUsage
    ResourceData memUsage
    ResourceData timeUsage

    static belongsTo = [workflow: Workflow]

    static embedded = [
            'cpu',
            'mem',
            'vmem',
            'time',
            'reads',
            'writes',
            'cpuUsage',
            'memUsage',
            'timeUsage'
    ]

    static constraints = {
        cpu(nullable: true)
        mem(nullable: true)
        vmem(nullable: true)
        time(nullable: true)
        reads(nullable: true)
        writes(nullable: true)
        cpuUsage(nullable: true)
        memUsage(nullable: true)
        timeUsage(nullable: true)
    }

}

@CompileDynamic
class ResourceData {

    Float mean
    Float min
    Float q1
    Float q2
    Float q3
    Float max
    String minLabel
    String maxLabel
    String q1Label
    String q2Label
    String q3Label

}
