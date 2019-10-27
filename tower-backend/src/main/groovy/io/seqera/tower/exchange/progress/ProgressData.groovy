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

package io.seqera.tower.exchange.progress

import groovy.transform.ToString
import io.seqera.tower.domain.ProcessLoad
import io.seqera.tower.domain.WorkflowLoad

@ToString(includePackage = false, includeNames = true)
class ProgressData {

    static final ProgressData EMPTY = new ProgressData(workflowProgress: new WorkflowLoad(), processesProgress: Collections.<ProcessLoad>emptyList())

    WorkflowLoad workflowProgress
    List<ProcessLoad> processesProgress

}
