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

import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ValidationException
import java.time.OffsetDateTime

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.tower.domain.ProcessLoad
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.TaskData
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowComment
import io.seqera.tower.domain.WorkflowLoad
import io.seqera.tower.domain.WorkflowMetrics
import io.seqera.tower.domain.WorkflowProcess
import io.seqera.tower.enums.WorkflowStatus
import io.seqera.tower.exceptions.NonExistingWorkflowException
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.service.progress.ProgressService

@Transactional
@Singleton
class WorkflowServiceImpl implements WorkflowService {

    ProgressService progressService

    @Inject
    WorkflowServiceImpl(ProgressService progressService) {
        this.progressService = progressService
    }

    @CompileDynamic
    @Transactional(readOnly = true)
    Workflow get(String id) {
        Workflow.findById(id)
    }

    @CompileDynamic
    List<Workflow> listByOwner(User owner, Long max, Long offset, String sqlRegex) {
        new DetachedCriteria<Workflow>(Workflow).build {
            eq('owner', owner)
            ne('deleted', true)
            if (sqlRegex) {
                or {
                    ilike('projectName', sqlRegex)
                    ilike('runName', sqlRegex)
                }
            }

            order('start', 'desc')
        }.list(max: max, offset: offset)
    }

    Workflow processTraceWorkflowRequest(TraceWorkflowRequest request, User owner) {
        if( request.workflow.checkIsRunning() ) {
            final workflow = saveNewWorkflow(request.workflow, owner)

            // save the process names
            for( int i=0; i<request.processNames?.size(); i++ ) {
                final name = request.processNames[i]
                final p = new WorkflowProcess(name: name, position: i, workflow: workflow)
                p.save()
            }

            return workflow
        }
        else {
            updateWorkflow(request.workflow, request.metrics)
        }
    }

    private Workflow saveNewWorkflow(Workflow workflow, User owner) {
        workflow.submit = workflow.start
        workflow.owner = owner
        workflow.status = WorkflowStatus.RUNNING
        // invoke validation explicitly due to gorm bug
        // https://github.com/grails/gorm-hibernate5/issues/110
        if (workflow.validate()) {
            workflow.save(flush:true)
        }
        else
            throw new ValidationException("Invalid workflow object: ${workflow.errors}")

        return workflow
    }

    @CompileDynamic
    private Workflow updateWorkflow(Workflow workflow, List<WorkflowMetrics> metrics) {
        Workflow existingWorkflow = Workflow.get(workflow.id)
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't find workflow record with ID=${workflow.id}")
        }

        updateMutableFields(existingWorkflow, workflow)
        associateMetrics(existingWorkflow, metrics)

        existingWorkflow.save()
        existingWorkflow
    }

    private void updateMutableFields(Workflow workflowToUpdate, Workflow originalWorkflow) {
        workflowToUpdate.resume = originalWorkflow.resume
        workflowToUpdate.success = originalWorkflow.success
        workflowToUpdate.complete = originalWorkflow.complete
        workflowToUpdate.duration = originalWorkflow.duration

        workflowToUpdate.exitStatus = originalWorkflow.exitStatus
        workflowToUpdate.errorMessage = originalWorkflow.errorMessage
        workflowToUpdate.errorReport = originalWorkflow.errorReport
        workflowToUpdate.status = originalWorkflow.status

        workflowToUpdate.stats = originalWorkflow.stats
    }

    private void associateMetrics(Workflow workflow, List<WorkflowMetrics> allMetrics) {
        for( WorkflowMetrics metrics : allMetrics ) {
            metrics.workflow = workflow
            metrics.save()
        }
    }

    void delete(Workflow workflowToDelete) {
        ProcessLoad.where { workflow == workflowToDelete }.deleteAll()
        WorkflowLoad.where { workflow == workflowToDelete }.deleteAll()
        WorkflowProcess.where { workflow == workflowToDelete }.deleteAll()
        WorkflowMetrics.where { workflow == workflowToDelete }.deleteAll()
        WorkflowComment.where { workflow == workflowToDelete }.deleteAll()
        Task.where { workflow == workflowToDelete }.deleteAll()
        // delete orphan task-data records
        final delete = """
                delete TaskData d
                where
                  d.sessionId = :sessionId
                  and d not in (
                    select t.data
                    from
                      Task t, Workflow w
                    where
                      t.workflow.id = w.id
                      and w.sessionId = :sessionId
                  )
                """.stripIndent()
        TaskData.executeUpdate(delete,[sessionId: workflowToDelete.sessionId])
        // finally delete workflow record
        workflowToDelete.delete()

    }

    boolean markForDeletion(String workflowId) {
        final result = Workflow.executeUpdate("update Workflow set deleted=true where id=:workflowId", [workflowId:workflowId])
        return result > 0
    }

    List<Workflow> findMarkedForDeletion(int max) {
        def args = new HashMap(1)
        if( max>0 )
            args.max = max
        Workflow.executeQuery("from Workflow where deleted=true", Collections.emptyList(), args)
    }

    @CompileDynamic
    List<WorkflowMetrics> findMetrics(Workflow workflow) {
        WorkflowMetrics.findAllByWorkflow(workflow)
    }

    @CompileDynamic
    List<WorkflowComment> getComments(Workflow owner) {
        WorkflowComment.where { workflow == owner }.list(sort: 'lastUpdated', order:'desc')
    }

    WorkflowComment createComment(Workflow workflow, String text, OffsetDateTime timestamp) {
        if( timestamp == null )
            timestamp = OffsetDateTime.now()

        final comment = new WorkflowComment()
        comment.workflow = workflow
        comment.text = text
        comment.lastUpdated = timestamp
        comment.dateCreated = timestamp
        return comment.save()
    }

    WorkflowComment updateComment(Workflow workflow, Serializable commentId, String text, OffsetDateTime timestamp) {
        if( timestamp == null )
            timestamp = OffsetDateTime.now()

        final comment = new WorkflowComment()
        comment.workflow = workflow
        comment.text = text
        comment.lastUpdated = timestamp
        comment.dateCreated = timestamp
        return comment.save()
    }

    @CompileDynamic
    List<String> getProcessNames(Workflow workflow) {
        List<WorkflowProcess> all = WorkflowProcess.executeQuery("from WorkflowProcess p where workflow=:workflow order by p.position", [workflow: workflow])
        all.collect { WorkflowProcess it -> it.name }
    }
}
