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

package io.seqera.mail

import java.time.Instant

import groovy.transform.ToString

/**
 * Model the current status for mailer object
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString(includeNames = true, includePackage = false)
class MailerStatus implements Serializable, Cloneable {

    int sentCount
    long errorCount
    String errorMessage
    Instant errorTimestamp
    boolean terminated
    Properties config

}
