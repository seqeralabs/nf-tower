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
