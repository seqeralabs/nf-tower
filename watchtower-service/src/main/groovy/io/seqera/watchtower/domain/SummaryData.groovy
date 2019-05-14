package io.seqera.watchtower.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'tasks'])
@CompileDynamic
class SummaryData {

    Float mean
    Float min
    Float q1
    Float q2
    Float q3
    Float max
    String minLabel
    String maxLabel
    String q1Label
    String q2Label
    String q3Label



}