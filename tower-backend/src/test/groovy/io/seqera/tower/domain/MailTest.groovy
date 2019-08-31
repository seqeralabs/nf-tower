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

package io.seqera.tower.domain

import java.nio.file.Paths

import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(application = Application.class)
class MailTest extends Specification {

    void 'should capture mail params' () {
        given:
        Closure closure = {
            from 'jim@dot.com'
            to 'paolo@dot.com'
            cc 'you@dot.com'
            bcc 'mrhide@dot.com'
            type 'text/html'
            subject 'Hi there'
            charset 'utf-8'
            body 'Hello world'
            attach 'foo.png'
            attach (['this.txt','that.txt'])

        }

        when:
        Mail mail = new Mail()
        mail.with(closure)
        then:
        mail.from == 'jim@dot.com'
        mail.to == 'paolo@dot.com'
        mail.cc == 'you@dot.com'
        mail.bcc == 'mrhide@dot.com'
        mail.type == 'text/html'
        mail.subject ==  'Hi there'
        mail.charset == 'utf-8'
        mail.body == 'Hello world'
        mail.attachments == [new MailAttachment('foo.png'), new MailAttachment('this.txt'), new MailAttachment('that.txt')]
    }


    void 'should add attachments' () {
        given:
        Mail mail

        when:
        mail = new Mail()
        mail.attach('/some/file.txt')
        then:
        mail.attachments == [new MailAttachment('/some/file.txt')]

        when:
        mail = new Mail()
        mail.attach(new File('x.txt'))
        then:
        mail.attachments == [new MailAttachment('x.txt')]

        when:
        mail = new Mail()
        mail.attach(Paths.get('x.txt'))
        then:
        mail.attachments == [new MailAttachment('x.txt')]

        when:
        mail = new Mail()
        mail.attach("file.${1}")
        then:
        mail.attachments == [new MailAttachment('file.1')]

        when:
        mail = new Mail()
        mail.attach(['foo.txt','bar.txt'])
        then:
        mail.attachments == [new MailAttachment('foo.txt'), new MailAttachment('bar.txt')]

        when:
        mail = new Mail()
        mail.attach 'pic.png', contentId: 'my-img'
        then:
        mail.attachments == [new MailAttachment('pic.png', contentId:'my-img')]

        when:
        mail = new Mail()
        mail.attach( new MailAttachment('/file.txt') )
        then:
        mail.attachments ==  [new MailAttachment('/file.txt')]
        when:
        mail = new Mail()
        mail.attach(new Object())
        then:
        thrown(IllegalArgumentException)
    }

    void 'should create a mail from a Map' () {
        given:
        Map map = [
                from:'me@google.com',
                to: 'you@nextflow.com',
                cc: 'hola@dot.com, hello@dot.com',
                bcc: 'foo@host.com',
                subject: 'this is a notification',
                charset: 'utf-8',
                type: 'text/html',
                body: 'Hello world',
                text: 'Pura vida',
                attach: '/some/file'
        ]

        when:
        Mail mail = Mail.of(map)

        then:
        mail.from == 'me@google.com'
        mail.to == 'you@nextflow.com'
        mail.cc == 'hola@dot.com, hello@dot.com'
        mail.bcc == 'foo@host.com'
        mail.subject == 'this is a notification'
        mail.charset == 'utf-8'
        mail.type == 'text/html'
        mail.body == 'Hello world'
        mail.text == 'Pura vida'
        mail.attachments == [new MailAttachment('/some/file')]
    }
}
