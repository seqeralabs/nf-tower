package io.seqera.watchtower.service

import javax.inject.Inject
import javax.inject.Singleton

import io.seqera.mail.Mail
import io.seqera.mail.Mailer
import io.seqera.mail.MailerConfig
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Singleton
class MailServiceImpl implements MailService {

    private Mailer mailer

    @Inject
    MailServiceImpl(MailerConfig mailerConfig) {
        mailer = new Mailer()
        mailer.setConfig(mailerConfig)
    }

    @Override
    void sendMail(Mail mail) {
        assert mail, 'Mail object cannot be null'
        if( !mail.from )
            mail.from(mailer.config.from)
        mailer.send(mail)
    }
}
