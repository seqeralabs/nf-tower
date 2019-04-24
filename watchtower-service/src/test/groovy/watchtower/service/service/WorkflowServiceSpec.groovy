package watchtower.service.service

import grails.gorm.transactions.Rollback
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import watchtower.service.Application
import watchtower.service.domain.Workflow
import watchtower.service.util.DomainCreator

@MicronautTest(application = Application.class)
@Rollback
class WorkflowServiceSpec extends Specification {

    @Shared @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    @Shared
    WorkflowService workflowService = embeddedServer.applicationContext.getBean(WorkflowService)

    void "save a new workflow"() {
        given: 'create a worklow from the helper class'
        Workflow workflow1 = DomainCreator.createWorkflow()

        and: 'create a workflow from the service class'
        Workflow workflow2 = workflowService.save('35cce421-4712-4da5-856b-6557635e543d', 'astonishing_majorana', 'completed', new Date())

        expect: 'the workflows have been created'
        !workflow1.hasErrors()
        !workflow2.hasErrors()


    }

}
