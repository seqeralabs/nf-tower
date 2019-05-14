package io.seqera.watchtower.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

import java.time.Instant

/**
 * Model Workflow nextflow attribute holding Nextflow metadata
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version'])
@CompileDynamic
class NextflowMeta {
    String version
    String build
    Instant timestamp

    @JsonSetter('timestamp')
    void deserializeTimestampInstant(String timestampText) {
        timestamp = timestampText ? Instant.parse(timestampText) : null
    }

    static constraints = {
        version(nullable: true)
        build(nullable: true)
        timestamp(nullable: true)
    }

}
