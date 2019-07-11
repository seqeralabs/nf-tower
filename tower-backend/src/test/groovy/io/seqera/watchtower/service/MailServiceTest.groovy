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

package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.mail.Mail
import io.seqera.mail.Mailer
import io.seqera.mail.MailerConfig
import io.seqera.watchtower.Application
import io.seqera.watchtower.util.AbstractContainerBaseTest
import org.subethamail.wiser.Wiser
import spock.lang.Specification

import javax.inject.Inject
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMultipart

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
        Wiser server = new Wiser(mailerConfig.smtp.port)
        server.start()

        and: 'an email'
        Map mailProperties = [to: 'receiver@nextflow.io', from: mailerConfig.from, subject: 'This is a test', body: 'Hello there']
        Mail mail = Mail.of(mailProperties)

        when: 'send the email'
        mailService.sendMail(mail)

        then: 'the message has been received'
        server.messages.size() == 1
        Message message = server.messages.first().mimeMessage
        message.from == [new InternetAddress(mailProperties.from)]
        message.allRecipients.contains(new InternetAddress(mailProperties.to))
        message.subject == mailProperties.subject
        (message.content as MimeMultipart).getBodyPart(0).content.getBodyPart(0).content == 'Hello there'

        cleanup: 'stop the mock server'
        server.stop()
    }

}
