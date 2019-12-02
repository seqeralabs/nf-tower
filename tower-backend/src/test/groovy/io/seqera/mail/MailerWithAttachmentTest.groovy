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

import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMultipart
import java.nio.file.Files
import java.nio.file.Path

import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.util.AbstractContainerBaseTest
import org.subethamail.wiser.Wiser
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(application = Application.class)
class MailerWithAttachmentTest extends AbstractContainerBaseTest {


    void "should send email with attachment"() {
        given:
        Integer PORT = 3025
        String USER = 'foo'
        String PASSWORD = 'secret'
        Wiser server = new Wiser(PORT)
        server.start()

        MailerConfig config = new MailerConfig(smtp:[host: '127.0.0.1', port: PORT, user: USER, password: PASSWORD])
        Mailer mailer = new Mailer(config: config)

        String TO = "receiver@gmail.com"
        String FROM = 'paolo@nextflow.io'
        String SUBJECT = "Sending test"
        String CONTENT = "This content should be sent by the user."
        Path ATTACH = Files.createTempFile('test', null)
        ATTACH.text = 'This is the file attachment content'

        when:
        Map mail = [
                from: FROM,
                to: TO,
                subject: SUBJECT,
                body: CONTENT,
                attach: ATTACH
        ]
        mailer.send(mail)

        then:
        server.messages.size() == 1
        Message message = server.messages.first().mimeMessage
        message.from == [new InternetAddress(FROM)]
        message.allRecipients.contains(new InternetAddress(TO))
        message.subject == SUBJECT
        (message.content as MimeMultipart).count == 2

        cleanup:
        if( ATTACH ) Files.delete(ATTACH)
        server?.stop()
    }
}
