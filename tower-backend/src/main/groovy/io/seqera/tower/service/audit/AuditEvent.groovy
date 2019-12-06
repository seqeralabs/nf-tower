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

package io.seqera.tower.service.audit

import groovy.transform.CompileStatic
import groovy.transform.ToString
import io.seqera.tower.service.audit.AuditEventType
/**
 * Entity tracking user business events
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
@ToString(includeNames = true, includePackage = false)
class AuditEvent {

    String clientIp
    String target
    String status
    String principal
    AuditEventType type

}
