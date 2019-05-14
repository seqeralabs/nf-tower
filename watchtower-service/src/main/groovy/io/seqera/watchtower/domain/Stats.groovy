package io.seqera.watchtower.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic


/**
 * Model workflow stats
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version'])
@CompileDynamic
class Stats {

    String computeTimeFmt

    Integer cachedCount
    Integer failedCount
    Integer ignoredCount
    Integer succeedCount

    String cachedCountFmt
    String succeedCountFmt
    String failedCountFmt
    String ignoredCountFmt

    Float cachedPct
    Float failedPct
    Float succeedPct
    Float ignoredPct

    Long cachedDuration
    Long failedDuration
    Long succeedDuration

    static constraints = {
        cachedCount(nullable: true)
        failedCount(nullable: true)
        ignoredCount(nullable: true)
        succeedCount(nullable: true)
        cachedCountFmt(nullable: true)
        succeedCountFmt(nullable: true)
        failedCountFmt(nullable: true)
        ignoredCountFmt(nullable: true)
        cachedPct(nullable: true)
        failedPct(nullable: true)
        succeedPct(nullable: true)
        ignoredPct(nullable: true)
        cachedDuration(nullable: true)
        failedDuration(nullable: true)
        succeedDuration(nullable: true)
    }

}
