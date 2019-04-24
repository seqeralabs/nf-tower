package watchtower.service.service

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import watchtower.service.domain.Workflow

import javax.inject.Singleton

@Singleton
@Transactional
class WorkflowService {

    Workflow save(String runId, String runName, String event, Date utcTime) {
        Workflow workflow = new Workflow(runId: runId, runName: runName, event: event, utcTime: utcTime)

        workflow.save()
        workflow
    }

}