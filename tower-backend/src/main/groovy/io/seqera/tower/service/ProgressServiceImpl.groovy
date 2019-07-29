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

package io.seqera.tower.service

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.tower.domain.ProcessProgress
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.WorkflowProgress
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowMetrics
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.exchange.progress.ProgressGet
import io.seqera.tower.exchange.workflow.WorkflowGet
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.type.StandardBasicTypes

import javax.inject.Singleton

@Transactional
@Singleton
class ProgressServiceImpl implements ProgressService {

    @CompileDynamic  // <-- TODO make this static removing the `findAllByWorkflow` dynamic finder 
    WorkflowGet buildWorkflowGet(Workflow workflow) {
        WorkflowGet result = new WorkflowGet(workflow: workflow)

        if (workflow.checkIsStarted()) {
            result.progress = computeWorkflowProgress(workflow.id)
        } else {
            result.progress = new ProgressGet(workflowProgress: workflow.workflowTasksProgress, processesProgress: workflow.processesProgress.sort { it.process })
            result.metrics = WorkflowMetrics.findAllByWorkflow(workflow)
        }

        return result
    }

    ProgressGet computeWorkflowProgress(Long workflowId) {
        List<ProcessProgress> processProgresses = computeProcessesProgress(workflowId)

        new ProgressGet(workflowProgress: computeWorkflowProgressState(processProgresses), processesProgress: processProgresses)
    }

    private WorkflowProgress computeWorkflowProgressState(List<ProcessProgress> processProgresses) {
        WorkflowProgress workflowProgress = new WorkflowProgress()
        processProgresses.each { ProcessProgress processProgress ->
            workflowProgress.running = workflowProgress.running + processProgress.running
            workflowProgress.submitted = workflowProgress.submitted + processProgress.submitted
            workflowProgress.failed = workflowProgress.failed + processProgress.failed
            workflowProgress.pending = workflowProgress.pending + processProgress.pending
            workflowProgress.succeeded = workflowProgress.succeeded + processProgress.succeeded
            workflowProgress.cached = workflowProgress.cached + processProgress.cached

            workflowProgress.totalCpus = workflowProgress.totalCpus + processProgress.totalCpus
            workflowProgress.cpuRealtime = workflowProgress.cpuRealtime + processProgress.cpuRealtime
            workflowProgress.memory = workflowProgress.memory + processProgress.memory
            workflowProgress.diskReads = workflowProgress.diskReads + processProgress.diskReads
            workflowProgress.diskWrites = workflowProgress.diskWrites + processProgress.diskWrites
        }

        workflowProgress
    }

    @CompileDynamic
    private List<ProcessProgress> computeProcessesProgress(Long workflowId) {
        Map<String, Map<TaskStatus, List<Map>>> rawProgressByProcessAndStatus = queryProcessesProgress(workflowId)

        rawProgressByProcessAndStatus.collect { String process, Map<TaskStatus, List<Map>> statusCountsOfProcess ->
            ProcessProgress processProgress = new ProcessProgress(process: process)

            statusCountsOfProcess.each { TaskStatus status, List<Map> rawProgresses ->
                Map rawProgress = rawProgresses.first()

                processProgress[status.toProgressTag()] = rawProgress.count
                processProgress.totalCpus = processProgress.totalCpus + (Long) (rawProgress.totalCpus ?: 0)
                processProgress.cpuRealtime = processProgress.cpuRealtime + (Long) (rawProgress.cpuRealtime ?: 0)
                processProgress.memory = processProgress.memory + (Long) (rawProgress.memory ?: 0)
                processProgress.diskReads = processProgress.diskReads + (Long) (rawProgress.diskReads ?: 0)
                processProgress.diskWrites = processProgress.diskWrites + (Long) (rawProgress.diskWrites ?: 0)
            }

            processProgress
        }.sort { it.process }
    }

    @CompileDynamic
    private Map<String, Map> queryProcessesProgress(Long workflowId) {
        List<Map> rawProgressRows = Task.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            workflow {
                eq('id', workflowId)
            }

            projections {
                groupProperty('process', 'process')
                groupProperty('status', 'status')
                countDistinct('id', 'count')
                sum('cpus', 'totalCpus')
                sum('peakRss', 'memory')
                sum('rchar', 'diskReads')
                sum('wchar', 'diskWrites')
                sqlProjection('sum(cpus * realtime) as cpuRealtime', 'cpuRealtime', StandardBasicTypes.LONG)
            }
        }

        Map<String, Map<TaskStatus, List<Map>>> rawProgressByProcessAndStatus = rawProgressRows.groupBy({ Map data -> data.process }, { Map data -> data.status })
        rawProgressByProcessAndStatus
    }

}
