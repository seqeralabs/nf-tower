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

    /**
     * Sanitize object cutting fields too long
     */
    List<String> sanitize() {
        cpu?.sanitize()
        mem?.sanitize()
        vmem?.sanitize()
        time?.sanitize()
        reads?.sanitize()
        writes?.sanitize()
        cpuUsage?.sanitize()
        memUsage?.sanitize()
        timeUsage?.sanitize()

        return getWarnings() ?: Collections.<String> emptyList()
    }

    private List<String> getWarnings() {
        List<String> result=null
        result = addWarn(result, cpu)
        result = addWarn(result, mem)
        result = addWarn(result, vmem)
        result = addWarn(result, time)
        result = addWarn(result, reads)
        result = addWarn(result, writes)
        result = addWarn(result, cpuUsage)
        result = addWarn(result, memUsage)
        result = addWarn(result, timeUsage)
        return result
    }

    private List<String> addWarn(List<String> result, ResourceData res) {
        if( res?.hasWarnings() ) {
            if( result==null ) result=new ArrayList<String>(10)
            result.addAll(res.getWarnings())
        }
        return result
    }

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
        process(maxSize: 255)
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

    transient List<String> warnings

    List<String> getWarnings() { warnings ?: Collections.<String>emptyList() }

    boolean hasWarnings() { warnings }

    private void addWarn(String str) {
        if( warnings==null )
            warnings = new ArrayList<>(10)
        warnings.add(str)
    }

    private String checkLen0(String value, String name) {
        if( value?.size() > 255 ) {
            addWarn("Value for $name longer expected (255) -- offending value: $value")
            return value.substring(0,255)
        }
        return value
    }

    void sanitize() {
        q1Label = checkLen0(q1Label, 'q1Label')
        q2Label = checkLen0(q2Label, 'q2Label')
        q3Label = checkLen0(q3Label, 'q3Label')
        minLabel = checkLen0(minLabel, 'minLabel')
        maxLabel = checkLen0(maxLabel, 'maxLabel')
    }
}
