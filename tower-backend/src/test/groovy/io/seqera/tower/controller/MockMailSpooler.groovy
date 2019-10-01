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

package io.seqera.tower.controller

import javax.inject.Singleton

import io.micronaut.context.annotation.Replaces
import io.seqera.mail.MailSpooler
import io.seqera.mail.MailSpoolerImpl
import io.seqera.mail.MailerStatus

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Singleton
@Replaces(MailSpoolerImpl)
class MockMailSpooler implements MailSpooler {
    @Override
    void start() {

    }

    @Override
    void stop() {

    }

    @Override
    void newMail() {

    }

    @Override
    void pause(boolean value) {

    }

    @Override
    MailerStatus getStatus() {
        return null
    }
}
