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

package io.seqera.tower.exchange.serviceinfo

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.seqera.tower.exchange.BaseResponse
import io.seqera.tower.service.ServiceInfo

/**
 * Implements Service info controller response object
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
@CompileStatic
class ServiceInfoResponse implements BaseResponse {
    String message
    ServiceInfo serviceInfo

    ServiceInfoResponse() {}

    ServiceInfoResponse(ServiceInfo info) {
        this.serviceInfo = info
    }
}
