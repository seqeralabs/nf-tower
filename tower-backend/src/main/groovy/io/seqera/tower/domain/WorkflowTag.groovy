package io.seqera.tower.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'tasks', 'owner'])
@CompileDynamic
class WorkflowTag {

    static belongsTo = [workflow: Workflow]

    String label

    static constraints = {
        label(blank: false, maxSize: 10, unique: 'workflow')
    }

}
