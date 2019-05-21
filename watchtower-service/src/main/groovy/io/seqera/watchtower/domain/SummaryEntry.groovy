package io.seqera.watchtower.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version'])
@CompileDynamic
class SummaryEntry {

    /*
     * The process name
     */
    String process

    SummaryData cpu
    SummaryData mem
    SummaryData vmem
    SummaryData time
    SummaryData reads
    SummaryData writes
    SummaryData cpuUsage
    SummaryData memUsage
    SummaryData timeUsage

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

    static mapping = {
        version false
    }

}
