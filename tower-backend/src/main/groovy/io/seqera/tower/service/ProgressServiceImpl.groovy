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

import javax.inject.Singleton

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.seqera.tower.domain.ProcessProgress
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowMetrics
import io.seqera.tower.domain.WorkflowProgress
import io.seqera.tower.exchange.progress.ProgressData
import io.seqera.tower.exchange.workflow.WorkflowGet

@Slf4j
@Transactional
@Singleton
class ProgressServiceImpl implements ProgressService {

    @CompileDynamic  // <-- TODO make this static removing the `findAllByWorkflow` dynamic finder 
    WorkflowGet buildWorkflowGet(Workflow workflow) {
        WorkflowGet result = new WorkflowGet(workflow: workflow)

        if (workflow.checkIsStarted()) {
            result.progress = computeWorkflowProgress(workflow.id)
        } else {
            result.progress = new ProgressData(workflowProgress: workflow.workflowTasksProgress, processesProgress: workflow.processesProgress.sort { it.process })
            result.metrics = WorkflowMetrics.findAllByWorkflow(workflow)
        }

        return result
    }

    ProgressData computeWorkflowProgress(Long workflowId) {
        List<List<Object>> tasks = Task.executeQuery("""\
            select
               t.process,
               t.status,
               count(*),
               sum(t.cpus) as totalCpus,
               sum(t.cpus * t.realtime) as cpuTime,
               sum(t.pcpu * t.realtime / 100) as cpuLoad,
               sum(t.peakRss) as memoryRss,
               sum(t.memory) as memoryReq,
               sum(t.rchar) as diskReads,
               sum(t.wchar) as diskWrites,
               sum(t.volCtxt) as volCtxt, 
               sum(t.invCtxt) as invCtxt
               
             from Task t
             where t.workflow.id = :workflowId
             group by t.process, t.status""", [workflowId: workflowId])

        // aggregate tasks by name and status
        def aggregate = new HashMap<String,ProcessProgress>(20)
        for( List cols : tasks ) {
            def name = cols[0] as String
            def progress = aggregate.get(name)
            if( progress == null ) {
                progress = new ProcessProgress(process: name)
                aggregate.put(name, progress)
            }
            progress.sumCols(cols)
        }

        // aggregate workflow process
        final processProgresses = new ArrayList<ProcessProgress>(aggregate.values()).sort{it.process}
        final workflowProgress = new WorkflowProgress()
        for( ProcessProgress p : processProgresses ) {
            workflowProgress.sumProgress(p)
        }

        new ProgressData(workflowProgress: workflowProgress, processesProgress: processProgresses)
    }


}
