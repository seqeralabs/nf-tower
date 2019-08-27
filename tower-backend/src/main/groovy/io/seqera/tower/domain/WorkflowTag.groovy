package io.seqera.tower.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'workflow', 'owner'])
@CompileDynamic
class WorkflowTag {

    static belongsTo = [workflow: Workflow]

    String text

    static constraints = {
        text(blank: false, maxSize: 10, unique: 'workflow')
    }

}
