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

package io.seqera.watchtower.service

import grails.gorm.DetachedCriteria
import io.seqera.watchtower.domain.Progress
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow

import javax.inject.Singleton

@Singleton
class ProgressServiceImpl implements ProgressService {

    Progress computeProgress(Workflow workflow) {
//        def p = new DetachedCriteria<Task>(Task).build {
//            eq('workflow', workflow)
//
//            projections {
//                groupProperty('status')
//                countDistinct('status')
//            }
//        }.get()
//
//        p as Progress
        null
    }

    void computeProcessStatus(Workflow workflow) {
        throw new UnsupportedOperationException()
    }
}
