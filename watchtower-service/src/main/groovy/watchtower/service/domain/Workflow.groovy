package watchtower.service.domain

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@CompileDynamic
class Workflow {

    String runId
    String runName
    String event

    Date utcTime

    static constraints = {
        event(nullable: true)
        utcTime(nullable: true)
    }

}
