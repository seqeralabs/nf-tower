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

package io.seqera.tower.service.mail

import javax.inject.Inject
import java.util.concurrent.BlockingQueue

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.mail.Mailer
import io.seqera.mail.MailerConfig
import io.seqera.tower.Application
import io.seqera.tower.domain.Mail
import io.seqera.tower.service.mail.MailServiceImpl
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
    MailServiceImpl mailService

    def setup() {
        mailService.getPendingMails().clear()
    }

    void 'should send mail message' () {
        given: 'start a mock mail server'

        and: 'an email'
        Map mailProperties = [to: 'receiver@nextflow.io', subject: 'This is a test', body: 'Hello there']
        Mail mail = Mail.of(mailProperties)

        when: 'send the email'
        mailService.sendMail(mail)

        then: 'the message has been received'
        def m = mailService.getPendingMails()[0]
        m.from == mailerConfig.from
        m.to == 'receiver@nextflow.io'
        m.subject == 'This is a test'
        m.body == 'Hello there'
        !m.sent

    }


    void 'should find pending email' () {

        when:
        mailService.sendMail(Mail.of(to:'paolo1@gmail.com', subject: 'Hi 1', body: 'Welcome 1'))
        mailService.sendMail(Mail.of(to:'paolo2@gmail.com', subject: 'Hi 2', body: 'Welcome 2'))
        mailService.sendMail(Mail.of(to:'paolo3@gmail.com', subject: 'Hi 3', body: 'Welcome 3'))

        then:
        mailService.getPendingMails().size() == 3
    }

    void 'send send and increment counter' () {
        given:
        def MAIL = new Mail(to:'foo@host.com', subject:'bar')
        def mailer = Mock(Mailer)
        def svc = Spy(MailServiceImpl)
        svc.pendingMails = Mock(BlockingQueue)
        svc.mailer = mailer

        when:
        svc.takeAndSendMail0()
        then:
        1 * svc.pendingMails.take() >> MAIL
        and:
        1 * mailer.send(MAIL) >> null
        and:
        svc.sentCount == 1

    }

}
