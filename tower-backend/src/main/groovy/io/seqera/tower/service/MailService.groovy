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

package io.seqera.tower.service

import javax.inject.Inject
import java.time.OffsetDateTime

import grails.gorm.services.Query
import grails.gorm.services.Service
import io.seqera.mail.MailSpooler
import io.seqera.mail.MailerConfig
import io.seqera.tower.domain.Mail
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Service(Mail)
abstract class MailService {

    @Inject
    MailSpooler spooler

    @Inject
    MailerConfig config

    void sendMail(Mail mail) {
        assert mail, 'Mail object cannot be null'
        if( !mail.from )
            mail.from(config.from)

        mail.save(failOnError: true)
        spooler.newMail()
    }


    @Query("from $Mail as m where m.sent != true order by lastUpdated")
    abstract List<Mail> findPendingMails()

    @Query("delete $Mail as m where m.sent = true and lastUpdated < $minDate")
    abstract Integer deleteEmailSendOlderThan(OffsetDateTime minDate)

}
