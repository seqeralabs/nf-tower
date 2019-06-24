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
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.watchtower.domain.Progress
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.TaskStatus

import javax.inject.Singleton

@Transactional
@Singleton
class ProgressServiceImpl implements ProgressService {

    @CompileDynamic
    Progress computeProgress(Long workflowId) {
        DetachedCriteria criteria = new DetachedCriteria(Task).build {
            workflow {
                eq('id', workflowId)
            }

            projections {
                groupProperty('status')
                countDistinct('id')
            }
        }
        Map progressProperties = criteria.list()
                .groupBy { Object[] tuple -> tuple[0] }
                .collectEntries { TaskStatus status, List<Object[]> tuples -> [(status.toProgressString()): tuples.first()[1]] }
        new Progress(progressProperties)
    }

    void computeProcessStatus(Workflow workflow) {
        throw new UnsupportedOperationException()
    }
}
