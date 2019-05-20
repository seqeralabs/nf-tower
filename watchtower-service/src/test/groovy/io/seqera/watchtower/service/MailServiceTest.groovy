package io.seqera.watchtower.service

import io.seqera.mail.Mail
import io.seqera.mail.Mailer
import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class MailServiceTest extends Specification {

    def 'should send mail message' () {

        given:
        def mail = Mock(Mail)
        def mailer = Mock(Mailer)
        def service = Spy(MailServiceImpl)
        service.mailer = mailer

        when:
        service.sendMail(mail)
        then:
        1 * service.sendMail(mail) >> null

    }

}
