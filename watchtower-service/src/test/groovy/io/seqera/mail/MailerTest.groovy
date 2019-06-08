/*
 * Copyright 2013-2019, Centre for Genomic Regulation (CRG)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.seqera.mail

import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import java.nio.file.Files
import java.nio.file.Path

import groovy.util.logging.Slf4j
import org.subethamail.wiser.Wiser
import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties

@Slf4j
class MailerTest extends Specification {

    @Timeout(1)
    def 'should resolve name quickly' () {
        // if this test fails make sure the file `/etc/hosts`
        // contains something like the following
        // 127.0.0.1  localhost	 <computer name>.local
        // ::1		  localhost  <computer name>.local
        //
        //
        //  see more at this link https://thoeni.io/post/macos-sierra-java/
        expect:
        InetAddress.getLocalHost().getCanonicalHostName() != null
    }

    void 'should return config properties'() {
        when:
        MailerConfig config = new MailerConfig(smtp: [host: 'google.com', port: '808', user: 'foo', password: 'bar'])
        Mailer mailer = new Mailer( config: config  )
        Properties props = mailer.createProps()

        then:
        props.get('mail.smtp.user') == 'foo'
        props.get('mail.smtp.password') == 'bar'
        props.get('mail.smtp.host') == 'google.com'
        props.get('mail.smtp.port') == '808'
        !props.containsKey('mail.other')

    }

    @RestoreSystemProperties
    void 'should configure proxy setting' () {
        given:
        System.setProperty('http.proxyHost', 'foo.com')
        System.setProperty('http.proxyPort', '8000')

        MailerConfig config = new MailerConfig(smtp:[host: 'gmail.com', port: 25, user:'yo'])
        Mailer mailer = new Mailer(config: config)

        when:
        Properties props = mailer.createProps()

        then:
        props.'mail.smtp.host' == 'gmail.com'
        props.'mail.smtp.port' == '25'
        props.'mail.smtp.user' == 'yo'
        props.'mail.smtp.proxy.host' == 'foo.com'
        props.'mail.smtp.proxy.port' == '8000'
        props.'mail.transport.protocol' == 'smtp'
    }


    def "sending mails using javamail"() {
        given:
        Integer PORT = 3025
        String USER = 'foo'
        String PASSWORD = 'secret'
        Wiser server = new Wiser(PORT)
        server.start()

        MailerConfig config = new MailerConfig(smtp: [host: 'localhost', port: PORT, user: USER, password: PASSWORD])
        Mailer mailer = new Mailer( config: config)

        String TO = "receiver@nextflow.io"
        String FROM = 'paolo@gmail.com'
        String SUBJECT = "Sending test"
        String CONTENT = "This content should be sent by the user."

        when:
        Map mail = [
                to: TO,
                from: FROM,
                subject: SUBJECT,
                body: CONTENT
        ]
        mailer.send(mail)

        then:
        server.messages.size() == 1
        Message message = server.messages.first().mimeMessage
        message.from == [new InternetAddress(FROM)]
        message.allRecipients.contains(new InternetAddress(TO))
        message.subject == SUBJECT
        message.content instanceof MimeMultipart
        (message.content as MimeMultipart).contentType.startsWith('multipart/related')

        cleanup:
        server?.stop()
    }

    def "sending mails using javamail (overrides 'to' address by config)"() {
        given:
        Integer PORT = 3025
        String USER = 'foo'
        String PASSWORD = 'secret'
        Wiser server = new Wiser(PORT)
        server.start()

        MailerConfig config = new MailerConfig(to: 'override@to.com', smtp: [host: 'localhost', port: PORT, user: USER, password: PASSWORD])
        Mailer mailer = new Mailer(config: config)

        String TO = "receiver@nextflow.io"
        String FROM = 'paolo@gmail.com'
        String SUBJECT = "Sending test"
        String CONTENT = "This content should be sent by the user."

        when:
        Map mail = [
                to: TO,
                from: FROM,
                subject: SUBJECT,
                body: CONTENT
        ]
        mailer.send(mail)

        then:
        server.messages.size() == 1
        Message message = server.messages.first().mimeMessage
        message.from == [new InternetAddress(FROM)]
        message.allRecipients.contains(new InternetAddress(config.to))
        message.subject == SUBJECT
        message.content instanceof MimeMultipart
        (message.content as MimeMultipart).contentType.startsWith('multipart/related')

        cleanup:
        server?.stop()
    }

    void "sending mails using java with attachment"() {
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


    void 'should send with java' () {
        given:
        Mailer mailer = Spy(Mailer)
        MimeMessage MSG = Mock(MimeMessage)
        Mail mail = new Mail()

        when:
        mailer.config = new MailerConfig(smtp: [host:'foo.com'])
        mailer.send(mail)

        then:
        1 * mailer.createMimeMessage(mail) >> MSG
        1 * mailer.sendViaJavaMail(MSG) >> null
    }



    void 'should create mime message' () {
        given:
        MimeMessage msg
        Mail mail

        when:
        mail = new Mail(from:'foo@gmail.com')
        msg = new Mailer(config: new MailerConfig(from:'fallback@hotmail.com')).createMimeMessage(mail)
        then:
        msg.from.size() == 1
        msg.from[0].toString() == 'foo@gmail.com'

        when:
        mail = new Mail()
        msg = new Mailer(config: new MailerConfig(from:'fallback@hotmail.com')).createMimeMessage(mail)
        then:
        msg.from.size() == 1
        msg.from[0].toString() == 'fallback@hotmail.com'

        when:
        mail = new Mail(from:'one@gmail.com, two@google.com')
        msg = new Mailer().createMimeMessage(mail)
        then:
        msg.from.size() == 2
        msg.from[0].toString() == 'one@gmail.com'
        msg.from[1].toString() == 'two@google.com'

        when:
        mail = new Mail(to:'foo@gmail.com, bar@google.com')
        msg = new Mailer().createMimeMessage(mail)
        then:
        msg.getRecipients(Message.RecipientType.TO).size()==2
        msg.getRecipients(Message.RecipientType.TO)[0].toString() == 'foo@gmail.com'
        msg.getRecipients(Message.RecipientType.TO)[1].toString() == 'bar@google.com'

        when:
        mail = new Mail(cc:'foo@gmail.com, bar@google.com')
        msg = new Mailer().createMimeMessage(mail)
        then:
        msg.getRecipients(Message.RecipientType.CC).size()==2
        msg.getRecipients(Message.RecipientType.CC)[0].toString() == 'foo@gmail.com'
        msg.getRecipients(Message.RecipientType.CC)[1].toString() == 'bar@google.com'

        when:
        mail = new Mail(bcc:'one@gmail.com, two@google.com')
        msg = new Mailer().createMimeMessage(mail)
        then:
        msg.getRecipients(Message.RecipientType.BCC).size()==2
        msg.getRecipients(Message.RecipientType.BCC)[0].toString() == 'one@gmail.com'
        msg.getRecipients(Message.RecipientType.BCC)[1].toString() == 'two@google.com'

        when:
        mail = new Mail(subject: 'this is a test', body: 'Ciao mondo')
        msg = new Mailer().createMimeMessage(mail)
        then:
        msg.subject == 'this is a test'
        msg.content instanceof MimeMultipart
        msg.content.count == 1
        msg.contentType.startsWith('text/plain')
        msg.content.getBodyPart(0).content.count == 1
        msg.content.getBodyPart(0).content.getBodyPart(0).content == 'Ciao mondo'
    }


    void 'should fetch config properties' () {
        given:
        Map ENV = [TOWER_SMTP_USER: 'jim', TOWER_SMTP_PASSWORD: 'secret', TOWER_SMTP_HOST: 'g.com', TOWER_SMTP_PORT: '864']
        Map SMTP = [host:'hola.com', user:'foo', password: 'bar', port: 234]
        Mailer mail

        when:
        mail = new Mailer(config: new MailerConfig(smtp: SMTP))
        then:
        mail.host == 'hola.com'
        mail.user == 'foo'
        mail.password == 'bar'
        mail.port == 234

        when:
        mail = new Mailer(config: [smtp: [host: 'local', port: '999']], env: ENV)
        then:
        mail.host == 'local'
        mail.port == 999
        mail.user == 'jim'
        mail.password == 'secret'

        when:
        mail = new Mailer(env: ENV)
        then:
        mail.host == 'g.com'
        mail.port == 864
        mail.user == 'jim'
        mail.password == 'secret'
    }


    void 'should capture send params' () {
        given:
        Mailer mailer = Spy(Mailer)

        when:
        mailer.send {
            to 'paolo@dot.com'
            from 'yo@dot.com'
            subject 'This is a test'
            body 'Hello there'
        }

        then:
        1 * mailer.send(Mail.of([to: 'paolo@dot.com', from:'yo@dot.com', subject: 'This is a test', body: 'Hello there'])) >> null
    }


    void 'should strip html tags'  () {
        given:
        Mailer mailer = new Mailer()

        expect:
        mailer.stripHtml('Hello') == 'Hello'
        mailer.stripHtml('1 < 10 > 5') == '1 < 10 > 5'
        mailer.stripHtml('<h1>1 < 5</h1>') == '1 < 5'
        mailer.stripHtml('<h1>Big title</h1><p>Hello <b>world</b></p>') == 'Big title\nHello world'
    }


    void 'should capture multiline body' () {
        given:
        Mailer mailer = Spy(Mailer)
        String BODY = '''
            multiline
            mail
            content
            '''

        when:
        mailer.send {
            to 'you@dot.com'
            subject 'foo'
            BODY
        }

        then:
        1 * mailer.send(Mail.of([to: 'you@dot.com', subject: 'foo', body: BODY])) >> null

    }

    void 'should guess html content' () {
        given:
        Mailer mailer = new Mailer()

        expect:
        !mailer.guessHtml('Hello')
        !mailer.guessHtml('1 < 10 > 5')
        mailer.guessHtml('<h1>1 < 5</h1>')
        mailer.guessHtml('1<br/>2')
        mailer.guessHtml('<h1>Big title</h1><p>Hello<br>world</p>')
    }

    @Unroll
    void 'should guess mime type' () {
        given:
        Mailer mailer = new Mailer()

        expect:
        mailer.guessMimeType(str) == type

        where:
        type            | str
        'text/plain'    | 'Hello'
        'text/plain'    | '1 < 10 > 5'
        'text/html'     | '<h1>1 < 5</h1>'
        'text/html'     | '1<br/>2'
        'text/html'     | '<h1>Big title</h1><p>Hello <b>world</b></p>'

    }

}
