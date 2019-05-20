package io.seqera.watchtower.service

import io.seqera.mail.Mail

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
interface MailService {

    void sendMail(Mail mail)

}
