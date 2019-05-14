package io.seqera.watchtower.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

/**
 * Model workflow manifest attribute
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version'])
@CompileDynamic
class Manifest {

    String nextflowVersion
    String defaultBranch
    String version
    String homePage
    String gitmodules
    String description
    String name
    String mainScript
    String author

    static constraints = {
        nextflowVersion(nullable: true)
        defaultBranch(nullable: true)
        version(nullable: true)
        homePage(nullable: true)
        gitmodules(nullable: true)
        description(nullable: true)
        name(nullable: true)
        mainScript(nullable: true)
        author(nullable: true)
    }

}
