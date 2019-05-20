package io.seqera.watchtower.service

import javax.inject.Inject

import io.seqera.mail.Mail
import io.seqera.mail.Mailer
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class MailServiceImpl implements MailService {

    @Inject
    private Mailer mailer

    @Override
    void sendMail(Mail mail) {
        assert mail, 'Mail object cannot be null'
        mailer.send(mail)
    }
}
