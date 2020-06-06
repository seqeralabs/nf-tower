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

import static io.seqera.tower.enums.WorkflowStatus.*

import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ValidationException
import java.time.OffsetDateTime

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.TransactionService
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.micronaut.context.ApplicationContext
import io.seqera.tower.domain.HashSequenceGenerator
import io.seqera.tower.domain.ProcessLoad
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.TaskData
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowComment
import io.seqera.tower.domain.WorkflowKey
import io.seqera.tower.domain.WorkflowLoad
import io.seqera.tower.domain.WorkflowMetrics
import io.seqera.tower.domain.WorkflowProcess
import io.seqera.tower.enums.WorkflowStatus
import io.seqera.tower.exceptions.NonExistingWorkflowException
import io.seqera.tower.exchange.trace.TraceBeginRequest
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.service.audit.AuditEventPublisher
import io.seqera.tower.service.progress.ProgressService

@Slf4j
@Transactional
@Singleton
class WorkflowServiceImpl implements WorkflowService {

    @Inject ProgressService progressService
    @Inject AuditEventPublisher auditEventPublisher
    @Inject ApplicationContext appCtx

    @NotTransactional
    String createWorkflowKey() {
        final transactionService = appCtx.getBean(TransactionService)
        final record = transactionService.withTransaction { new WorkflowKey() .save() }
        final workflowId = HashSequenceGenerator.getHash(record.id)
        transactionService.withTransaction { record.workflowId=workflowId; record.save() }
        return workflowId
    }

    @Override
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
            checkRequiredFields(request.workflow)
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

    private void checkRequiredFields(Workflow workflow) {
        // validate
        if( !workflow.start ) throw new IllegalArgumentException("Missing field 'start' for workflow id=${workflow.id}")
        if( !workflow.workDir ) throw new IllegalArgumentException("Missing field 'workDir' for workflow id=${workflow.id}")
        if( !workflow.userName ) throw new IllegalArgumentException("Missing field 'userName' for workflow id=${workflow.id}")
        if( !workflow.sessionId ) throw new IllegalArgumentException("Missing field 'sessionId' for workflow id=${workflow.id}")
        if( !workflow.commandLine ) throw new IllegalArgumentException("Missing field 'commandLine' for workflow id=${workflow.id}")
        if( !workflow.scriptName ) throw new IllegalArgumentException("Missing field 'scriptName' for workflow id=${workflow.id}")
        if( !workflow.projectDir ) throw new IllegalArgumentException("Missing field 'projectDir' for workflow id=${workflow.id}")
        if( !workflow.homeDir ) throw new IllegalArgumentException("Missing field 'homeDir' for workflow id=${workflow.id}")
        if( !workflow.scriptFile ) throw new IllegalArgumentException("Missing field 'scriptFile' for workflow id=${workflow.id}")
        if( !workflow.launchDir ) throw new IllegalArgumentException("Missing field 'launchDir' for workflow id=${workflow.id}")
        if( !workflow.runName ) throw new IllegalArgumentException("Missing field 'runName' for workflow id=${workflow.id}")

    }

    private Workflow saveNewWorkflow(Workflow workflow, User owner) {
        // configure missing fields
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


    @Override
    Workflow createWorkflow(TraceBeginRequest request, User user) {
        checkRequiredFields(request.workflow)

        if( !request.towerLaunch ) {
            final workflow = saveNewWorkflow(request.workflow, user)
            saveProcessNames(workflow, request.processNames)
            return workflow
        }

        else {
            if( !request.workflow.id )
                throw new IllegalStateException("Missing launch workflow id=$request.launchId")
            final Workflow workflow = get(request.workflow.id)
            if( !workflow )
                throw new IllegalStateException("Unable to find launch workflow id=$request.workflow.id")
            if( workflow.owner.id != user.id )
                throw new IllegalStateException("Illegal workflow owner user -- current id=${workflow.owner.id}; expected id=$user.id")
            // TODO: check the status is SUBMITTED

            workflow.start = OffsetDateTime.now()
            workflow.status = RUNNING
            // copy attributes from request
            workflow.resume = request.workflow.resume
            workflow.sessionId = request.workflow.sessionId
            workflow.projectDir = request.workflow.projectDir
            workflow.profile = request.workflow.profile
            workflow.homeDir = request.workflow.homeDir
            workflow.workDir = request.workflow.workDir
            workflow.container = request.workflow.container
            workflow.commitId = request.workflow.commitId
            workflow.repository = request.workflow.repository
            workflow.containerEngine = request.workflow.containerEngine
            workflow.scriptFile = request.workflow.scriptFile
            workflow.launchDir = request.workflow.launchDir
            workflow.runName = request.workflow.runName
            workflow.scriptId = request.workflow.scriptId
            workflow.revision = request.workflow.revision
            workflow.commandLine = request.workflow.commandLine
            workflow.projectName = request.workflow.projectName
            workflow.scriptName = request.workflow.scriptName
            workflow.status = request.workflow.status
            workflow.configFiles = request.workflow.configFiles
            workflow.configText = request.workflow.configText
            workflow.params = request.workflow.params
            workflow.manifest = request.workflow.manifest
            workflow.nextflow = request.workflow.nextflow
            // finally save it
            workflow.save()
            // add process names
            saveProcessNames(workflow, request.processNames)
            return workflow
        }
    }


    protected void saveProcessNames(Workflow workflow, List<String> processNames) {
        // save the process names
        for( int i=0; i<processNames?.size(); i++ ) {
            final name = processNames[i]
            final p = new WorkflowProcess(name: name, position: i, workflow: workflow)
            p.save()
        }
    }

    @Override
    @CompileDynamic
    Workflow updateWorkflow(Workflow workflow, List<WorkflowMetrics> metrics) {
        Workflow existingWorkflow = Workflow.get(workflow.id)
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't find workflow with id=${workflow.id}")
        }

        updateMutableFields(existingWorkflow, workflow)
        associateMetrics(existingWorkflow, metrics)

        existingWorkflow.save()
        return existingWorkflow
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
            final warns = metrics.sanitize()
            if( warns ) {
                log.warn "Workflow Id=$workflow.id report reports metrics warnings:\n${warns.join('\n')}"
            }
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

    @Override
    void markForRunning(String workflowId) {
        final workflow = Workflow.get(workflowId)
        if (!workflow) {
            throw new NonExistingWorkflowException("Can't find task for workflow Id: ${workflowId}")
        }
        // if complete report an error
        if( workflow.checkIsComplete() ) {
            throw new IllegalStateException("Unexpected execution status workflow with Id: ${workflowId}")
        }
        // if status is UNKNOWN 
        if( workflow.status==UNKNOWN ) {
            // change status to running
            workflow.status = RUNNING
            workflow.save()
            // notify event
            auditEventPublisher.workflowStatusChangeFromRequest(workflow.id, "new=$RUNNING; was=$UNKNOWN")
        }
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
