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
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowMetrics
import io.seqera.tower.exchange.progress.ProcessProgress
import io.seqera.tower.exchange.progress.ProgressData
import io.seqera.tower.exchange.progress.WorkflowProgress
import io.seqera.tower.exchange.workflow.WorkflowGet

@Slf4j
@Transactional
@Singleton
class ProgressServiceImpl implements ProgressService {

    @CompileDynamic
    WorkflowGet buildWorkflowGet(Workflow workflow) {
        final result = new WorkflowGet(workflow: workflow)

        // fetch progress
        result.progress = fetchWorkflowProgress(workflow)

        // fetch metrics
        if( workflow.complete )
            result.metrics = WorkflowMetrics.findAllByWorkflow(workflow)

        return result
    }

    private void updatePeaks(Workflow workflow, WorkflowProgress progress) {

        if( workflow.peakLoadCpus < progress.loadCpus ) {
            workflow.peakLoadCpus = progress.loadCpus
        }
        if( workflow.peakLoadTasks < progress.loadTasks ) {
            workflow.peakLoadTasks = progress.loadTasks
        }
        if( workflow.peakLoadMemory < progress.loadMemory ) {
            workflow.peakLoadMemory = progress.loadMemory
        }

        progress.peakLoadCpus = workflow.peakLoadCpus
        progress.peakLoadTasks = workflow.peakLoadTasks
        progress.peakLoadMemory = workflow.peakLoadMemory

        if( workflow.isDirty() )
            workflow.save()
    }

    @CompileDynamic
    ProgressData fetchWorkflowProgress(Workflow workflow) {
        final result = computeWorkflowProgress(workflow.id)
        updatePeaks(workflow, result.workflowProgress)
        return result
    }

    ProgressData computeWorkflowProgress(Long workflowId) {
        List<List<Object>> tasks = Task.executeQuery("""\
            select
               p.name,
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

             from WorkflowProcess p
               left join Task t on p.workflow = t.workflow and p.name = t.process
             where
               p.workflow.id = :workflowId
             group by p.name, t.status
             order by p.position """, [workflowId: workflowId])

        // aggregate tasks by name and status
        final workflowProgress = new WorkflowProgress()
        final aggregate = new LinkedHashMap<String,ProcessProgress>(20)
        for( List cols : tasks ) {
            final row = new ProgressRow(cols)
            // aggregate by process name
            def name = row.process
            def process = aggregate.get(name)
            if( process == null ) {
                process = new ProcessProgress(process: name)
                aggregate.put(name, process)
            }
            process.plus(row)
            
            // aggregate all nums for workflow
            workflowProgress.plus(row)
        }

        // aggregate workflow process
        final processProgresses = new ArrayList<ProcessProgress>(aggregate.values()).sort{it.process}
        new ProgressData(workflowProgress: workflowProgress, processesProgress: processProgresses)
    }

}
