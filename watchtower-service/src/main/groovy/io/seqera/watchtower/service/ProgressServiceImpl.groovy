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
import io.seqera.watchtower.domain.ProcessProgress
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.TasksProgress
import io.seqera.watchtower.pogo.enums.TaskStatus

import javax.inject.Singleton

@Transactional
@Singleton
class ProgressServiceImpl implements ProgressService {

    @CompileDynamic
    TasksProgress computeTasksProgress(Long workflowId) {
        List<Object[]> tuples = new DetachedCriteria(Task).build {
            workflow {
                eq('id', workflowId)
            }

            projections {
                groupProperty('status')
                countDistinct('id')
            }
        }.list()

        Map<String, Long> progressProperties = tuples.collectEntries { Object[] tuple ->
            [(tuple[0].toProgressString()): tuple[1]]
        }
        new TasksProgress(progressProperties)
    }

    List<ProcessProgress> computeProcessesProgress(Long workflowId) {
        Map<String, Long> totalCountByProcess = queryProcessesTasksStatus(workflowId)
        Map<String, Long> completedCountByProcess = queryProcessesTasksStatus(workflowId, TaskStatus.COMPLETED)

        totalCountByProcess.collect { String process, Long totalCount ->
            new ProcessProgress(process: process, total: totalCount, completed: completedCountByProcess[process])
        }
    }

    @CompileDynamic
    private Map<String, Long> queryProcessesTasksStatus(Long workflowId, TaskStatus status = null) {
        List<Object[]> tuples = new DetachedCriteria(Task).build {
            if (status) {
                eq('status', status)
            }

            workflow {
                eq('id', workflowId)
            }

            projections {
                groupProperty('process')
                countDistinct('id')
            }
        }.list()

        Map<String, Long> statusCountByProcess = tuples.collectEntries { Object[] tuple ->
            [(tuple[0]): tuple[1]]
        }
        statusCountByProcess
    }
}
