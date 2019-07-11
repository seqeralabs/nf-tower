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

package io.seqera.tower.service

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
