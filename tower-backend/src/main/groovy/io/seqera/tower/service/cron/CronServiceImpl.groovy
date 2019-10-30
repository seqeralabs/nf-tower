package io.seqera.tower.service.cron

import javax.inject.Inject
import javax.inject.Singleton
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.seqera.tower.domain.Workflow
import io.seqera.tower.service.WorkflowService
import io.seqera.tower.service.progress.ProgressOperationsImpl
import io.seqera.tower.service.progress.ProgressServiceImpl
import org.springframework.transaction.annotation.Propagation
/**
 * Implements basic cron service to execute tasks at scheduled time
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
@CompileStatic
class CronServiceImpl implements CronService {

    ScheduledExecutorService scheduler

    @Inject
    WorkflowService workflowService

    @Inject
    ProgressServiceImpl progressSvc

    @Inject
    ProgressOperationsImpl progressOp

    @Value('${tower.cron.initial-delay:`10s`}')
    Duration initialDelay

    @Value('${tower.cron.delay:`30s`}')
    Duration schedulerDelay

    @Value('${tower.cron.enabled:`false`}')
    boolean enabled

    int progressSaveCounter

    @Override
    void start() {
        if( enabled ) {
            start0()
        }
        else {
            log.info "Cron service DISABLED"
        }
    }

    private void start0() {
        log.info "Starting cron service"
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay({scheduledTasks()} as Runnable, initialDelay.toMillis(), schedulerDelay.toMillis(), TimeUnit.MILLISECONDS);
    }


    protected void scheduledTasks() {
        saveLoadRecords()
        deleteWorkflowMarkedForDeletion()
    }

    protected void deleteWorkflowMarkedForDeletion() {
        try {
            deleteWorkflowMarkedForDeletion0()
        }
        catch( Exception e ) {
            log.debug "Failed to delete"
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected void deleteWorkflowMarkedForDeletion0() {
        def workflow = workflowService.findMarkedForDeletion(1)
        if( workflow ) {
            log.info "Dropping workflow marked for deletion=$workflow.id"
            workflowService.delete(workflow[0])
        }
    }

    @Override
    void stop() {
        if( enabled )
            stop0()
    }

    private void stop0() {
        log.debug "Stopping cron service"
        try {
            scheduler.shutdown()
            scheduler.awaitTermination(30,TimeUnit.SECONDS)
        }
        catch (Exception e ) {
            log.error("Error shutting down scheduler service | ${e.message}")
        }
    }

    void saveLoadRecords() {
        try {
            saveRecordsTx()
        }
        catch( Exception e ) {
            log.error "Unexpected error while save load records | ${e.message}"
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected void saveRecordsTx() {
        final workflow = findWorkflowWithMissingProgress()
        if( workflow != null ) {
            saveLoadRecords(workflow)
        }
    }

    protected void saveLoadRecords(Workflow workflow) {
        log.debug "Reconciling execution progress for workflow Id=$workflow.id (${++progressSaveCounter})"
        def data = progressSvc.getProgressQuery(workflow)
        progressOp.persistProgressData(workflow, data)
        log.debug "Reconciling execution progress for workflow Id=${workflow.id} DONE"
    }

    protected Workflow findWorkflowWithMissingProgress() {
        def query = "from Workflow w where w.status is not null and w.status != 'RUNNING' and w not in (select distinct l.workflow from WorkflowLoad l)"
        def result = Workflow.executeQuery(query, [max:1])
        assert result.size() < 2
        return (Workflow) result[0]
    }
}
