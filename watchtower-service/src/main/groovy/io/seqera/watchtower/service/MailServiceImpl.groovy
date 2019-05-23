package io.seqera.watchtower.service

import io.seqera.mail.MailerConfig

import javax.inject.Inject

import io.seqera.mail.Mail
import io.seqera.mail.Mailer

import javax.inject.Singleton

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
        mailer.send(mail)
    }
}
