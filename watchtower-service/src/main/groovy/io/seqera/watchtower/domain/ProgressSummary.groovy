package io.seqera.watchtower.domain

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@CompileDynamic
class ProgressSummary {

    static belongsTo = [workflow: Workflow]

    Integer running
    Integer submitted
    Integer failed
    Integer pending
    Integer succeeded
    Integer cached

    static mapping = {
        version false
    }

}
