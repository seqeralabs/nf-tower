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

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.mail.MailerConfig
import io.seqera.tower.Application
import io.seqera.tower.domain.Mail
import io.seqera.tower.util.AbstractContainerBaseTest
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(application = Application.class)
@Transactional
class MailServiceTest extends AbstractContainerBaseTest {

    @Inject
    MailerConfig mailerConfig

    @Inject
    MailService mailService

    void 'should send mail message' () {
        given: 'start a mock mail server'

        and: 'an email'
        Map mailProperties = [to: 'receiver@nextflow.io', subject: 'This is a test', body: 'Hello there']
        Mail mail = Mail.of(mailProperties)

        when: 'send the email'
        mailService.sendMail(mail)

        then: 'the message has been received'
        def m = Mail.list()[0]
        m.from == mailerConfig.from
        m.to == 'receiver@nextflow.io'
        m.subject == 'This is a test'
        m.body == 'Hello there'
        !m.sent

    }


    void 'should find pending email' () {

        when:
        Mail.withNewTransaction {
            mailService.sendMail(Mail.of(to:'paolo1@gmail.com', subject: 'Hi 1', body: 'Welcome 1'))
            mailService.sendMail(Mail.of(to:'paolo2@gmail.com', subject: 'Hi 2', body: 'Welcome 2'))
            mailService.sendMail(Mail.of(to:'paolo3@gmail.com', subject: 'Hi 3', body: 'Welcome 3'))
        }

        then:
        def list = Mail.withNewTransaction { mailService.findPendingMails() }
        list.size() == 3

        when:
        Mail.withNewTransaction {
            list[0].sent = true
            list[0].save()
        }

        then:
        Mail.withNewTransaction { mailService.findPendingMails() } .size() == 2
    }

    def 'should delete old email' () {
        when:
        Mail.withNewTransaction {
            mailService.sendMail(Mail.of(to:'paolo1@gmail.com', subject: 'Hi 1', body: 'Welcome 1'))
            mailService.sendMail(Mail.of(to:'paolo2@gmail.com', subject: 'Hi 2', body: 'Welcome 2'))
            mailService.sendMail(Mail.of(to:'paolo3@gmail.com', subject: 'Hi 3', body: 'Welcome 3'))
        }

        then:
        def list = Mail.withNewTransaction { Mail.list() }
        list.size() == 3

        when:
        // mark as sent emails
        Mail.withNewTransaction {
            list[0].sent = true;  list[0].save()
            list[1].sent = true;  list[1].save()
        }
        and: 
        def now = OffsetDateTime.now()
        then:
        Mail.withNewTransaction { mailService.deleteEmailSendOlderThan(now.minusMinutes(1)) } == 0
        Mail.withNewTransaction { Mail.list().size() } == 3

        expect:
        Mail.withNewTransaction { mailService.deleteEmailSendOlderThan(now.plusMinutes(1)) }
        Mail.withNewTransaction { Mail.list().size() } == 1
    }

}
