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

package io.seqera.tower.service.mail

import io.seqera.mail.MailerStatus
import io.seqera.tower.domain.Mail
/**
 * Implements a Mail delivery service
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
interface MailService {

    void sendMail(Mail mail)

    void start()

    void stop()

    MailerStatus getStatus()

}
