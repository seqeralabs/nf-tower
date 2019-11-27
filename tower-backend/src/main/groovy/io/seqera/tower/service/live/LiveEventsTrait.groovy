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

package io.seqera.tower.service.live

import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.LiveAction
import io.seqera.tower.exchange.live.LiveUpdate

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
trait LiveEventsTrait {

    abstract void publishEvent(LiveUpdate traceSseResponse)

    void publishWorkflowEvent(Workflow workflow) {
        publishEvent(LiveUpdate.of(workflow.owner.id, workflow.id, LiveAction.WORKFLOW_UPDATE))
    }

    void publishProgressEvent(Workflow workflow) {
        publishEvent(LiveUpdate.of(workflow.owner.id, workflow.id, LiveAction.PROGRESS_UPDATE))
    }
}
