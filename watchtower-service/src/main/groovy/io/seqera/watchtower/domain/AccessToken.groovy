package io.seqera.watchtower.domain

import java.time.Instant

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
/**
 * Represent a user API access token
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@CompileDynamic
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'workflow'])
class AccessToken {

    String token
    String name
    Instant dateCreated
    Instant lastUsed

    static belongsTo = [user: User]

    static constraints = {
        token( unique: true )
        lastUsed( nullable: true )
    }

}
