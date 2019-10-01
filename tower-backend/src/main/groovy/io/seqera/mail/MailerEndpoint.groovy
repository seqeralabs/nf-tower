package io.seqera.mail

import javax.inject.Inject

import io.micronaut.management.endpoint.annotation.Endpoint
import io.micronaut.management.endpoint.annotation.Read
import io.micronaut.management.endpoint.annotation.Write

/**
 * Endpoint for mailer status
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Endpoint('mailer')
class MailerEndpoint {

    @Inject
    MailSpooler spooler

    @Read
    MailerStatus getStatus() {
        spooler.getStatus()
    }

    @Write
    MailerStatus pause(boolean status) {
        spooler.pause(status)
        return spooler.getStatus()
    }
}
