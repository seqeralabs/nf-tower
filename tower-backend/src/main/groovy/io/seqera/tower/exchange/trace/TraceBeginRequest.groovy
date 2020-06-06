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

package io.seqera.tower.exchange.trace

import com.fasterxml.jackson.annotation.JsonSetter
import groovy.transform.CompileStatic
import groovy.transform.ToString
import io.seqera.tower.domain.Workflow
/**
 * Model a workflow trace begin request
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
@ToString(includeNames = true, includePackage = false)
class TraceBeginRequest {

    Workflow workflow
    List<String> processNames
    Boolean towerLaunch

    @JsonSetter
    @Deprecated
    void setLaunchId(String id) {
        this.towerLaunch = id!=null
    }

}
