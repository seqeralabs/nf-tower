package io.seqera.tower.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

import java.time.OffsetDateTime

@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'workflow'])
@CompileDynamic
class WorkflowTag {

    static belongsTo = [workflow: Workflow]

    String text
    OffsetDateTime dateCreated

    static constraints = {
        text(blank: false, maxSize: 10, unique: 'workflow')
    }

}
