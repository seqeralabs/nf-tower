package io.seqera.watchtower.domain

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@CompileDynamic
class MagnitudeSummary {

    static belongsTo = [workflow: Workflow]

    String name
    String taskLabel

    Double mean
    Double min
    Double q1
    Double q2
    Double q3
    Double max

    String minLabel
    String maxLabel
    String q1Label
    String q2Label
    String q3Label

    static mapping = {
        version false
    }
}
