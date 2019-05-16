package io.seqera.watchtower.domain

import com.fasterxml.jackson.annotation.JsonGetter
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
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached'])
@CompileDynamic
class NextflowMeta {

    String nextflowVersion
    String build
    Instant timestamp

    @JsonSetter('timestamp')
    void deserializeTimestampInstant(String timestampText) {
        timestamp = timestampText ? Instant.parse(timestampText) : null
    }

    @JsonSetter('version')
    void deserializeNextflowVersion(String version) {
        nextflowVersion = version
    }

    @JsonGetter('version')
    String serializeNextflowVersion() {
        nextflowVersion
    }


    static constraints = {
        nextflowVersion(nullable: true)
        build(nullable: true)
        timestamp(nullable: true)
    }

    static mapping = {
        version false
    }

}
