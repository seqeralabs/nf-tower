/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.mail

import javax.inject.Inject

import io.micronaut.management.endpoint.annotation.Endpoint
import io.micronaut.management.endpoint.annotation.Read
import io.micronaut.management.endpoint.annotation.Write
import io.seqera.tower.service.mail.MailService
/**
 * Endpoint for mailer status
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Endpoint('mailer')
class MailerEndpoint {

    @Inject
    MailService mailService

    @Read
    MailerStatus getStatus() {
        mailService.getStatus()
    }

    @Write
    MailerStatus pause(boolean status) {
        return mailService.getStatus()
    }
}
