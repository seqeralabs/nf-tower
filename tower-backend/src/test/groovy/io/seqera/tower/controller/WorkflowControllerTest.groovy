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

package io.seqera.tower.controller

import io.seqera.tower.domain.TasksProgress

import javax.inject.Inject
import java.time.Instant
import java.time.OffsetDateTime

import grails.gorm.transactions.TransactionService
import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.WfManifest
import io.seqera.tower.domain.WfNextflow
import io.seqera.tower.domain.WfStats
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.WorkflowTasksProgress
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.exchange.task.TaskList
import io.seqera.tower.exchange.workflow.GetWorkflowMetricsResponse
import io.seqera.tower.exchange.workflow.WorkflowGet
import io.seqera.tower.exchange.workflow.WorkflowList
import io.seqera.tower.service.WorkflowService
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator

@MicronautTest(application = Application.class)
@Transactional
class WorkflowControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject
    TransactionService tx

    @Inject
    WorkflowService workflowService

    void "get a workflow"() {
        given: "a workflow with some metrics"
        DomainCreator creator = new DomainCreator()
        Workflow workflow = creator.createWorkflow(
                complete: OffsetDateTime.now(),
                manifest: new WfManifest(defaultBranch: 'master'),
                stats: new WfStats(computeTimeFmt: '(a few seconds)'),
                nextflow: new WfNextflow(version: "19.05.0-TOWER", timestamp: Instant.now(), build: '19.01.1'),
        )
        creator.createWorkflowTasksProgress(workflow: workflow)

        creator.createWorkflowMetrics(workflow)
        creator.createWorkflowMetrics(workflow)


        when: "perform the request to obtain the workflow"
        String accessToken = doJwtLogin(creator.generateAllowedUser(), client)
        HttpResponse<WorkflowGet> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/${workflow.id}")
                           .bearerAuth(accessToken),
                WorkflowGet.class
        )

        then: "the workflow data is properly obtained"
        response.status == HttpStatus.OK
        response.body().workflow.workflowId == workflow.id.toString()
        response.body().workflow.stats
        response.body().workflow.nextflow
        response.body().workflow.manifest
        response.body().metrics.size() == 2
        response.body().progress.workflowTasksProgress
    }

    void "get a workflow as non-authenticated user"() {
        given: "a workflow with some summaries"
        DomainCreator domainCreator = new DomainCreator()
        Workflow workflow = domainCreator.createWorkflow()

        when: "perform the request to obtain the workflow as a not allowed user"
        HttpResponse<WorkflowGet> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/${workflow.id}"),
                WorkflowGet.class
        )

        then: "the workflow data is properly obtained"
        response.status == HttpStatus.OK
        response.body().workflow.workflowId == workflow.id.toString()
    }

    void "get a list of workflows"() {
        given: 'a user owner of the workflow'
        User owner
        User.withNewTransaction {
            owner = new DomainCreator().generateAllowedUser()
        }

        and: "some workflows owned by the user and ordered by start date in ascending order"
        DomainCreator domainCreator = new DomainCreator()
        List<Workflow> workflows = (1..4).collect { Integer i ->
            domainCreator.createWorkflow(
                    owner: owner,
                    start: OffsetDateTime.now().plusSeconds(i)
            )
        }

        and: 'some other workflows belonging to other users'
        5.times {
            domainCreator.createWorkflow()
        }

        and: "perform the request to obtain the workflows"
        String accessToken = doJwtLogin(owner, client)
        HttpResponse<WorkflowList> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/list")
                           .bearerAuth(accessToken),
                WorkflowList.class
        )

        expect: "the workflows data is properly obtained"
        response.status == HttpStatus.OK
        response.body().workflows.size() == workflows.size()

        and: 'the workflows are ordered by start date in descending order'
        response.body().workflows.workflow.workflowId == workflows.reverse().id*.toString()
    }

    void "try to get a non-existing workflow"() {
        when: "perform the request to obtain a non-existing workflow"
        String accessToken = doJwtLogin(new DomainCreator().generateAllowedUser(), client)
        client.toBlocking().exchange(
                HttpRequest.GET("/workflow/100")
                        .bearerAuth(accessToken),
                WorkflowGet.class
        )

        then: "a 404 response is obtained"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.NOT_FOUND
    }

    void "get the list of tasks associated with a workflow"() {
        given: 'a task'
        Task firstTask = new DomainCreator().createTask(taskId: 1)
        List<Task> tasks = [firstTask]

        and: 'extract its workflow'
        Workflow workflow = firstTask.workflow

        and: 'generate more tasks associated with the workflow'
        (2..3).each {
            tasks << new DomainCreator().createTask(workflow: workflow, taskId: it)
        }
        tasks << firstTask

        and: "perform the request to obtain the tasks of the workflow"
        String accessToken = doJwtLogin(new DomainCreator().generateAllowedUser(), client)
        HttpResponse<TaskList> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/${workflow.id}/tasks")
                           .bearerAuth(accessToken),
                TaskList.class
        )

        expect: "the tasks data is properly obtained"
        response.status == HttpStatus.OK
        response.body().tasks.size() == 3
    }

    void "try to get the list of tasks from a nonexistent workflow"() {
        when: "perform the request to obtain the tasks from a non-existing workflow"
        String accessToken = doJwtLogin(new DomainCreator().generateAllowedUser(), client)
        HttpResponse<TaskList> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/100/tasks")
                        .bearerAuth(accessToken),
                TaskList.class
        )

        then: "the tasks list is empty"
        response.status == HttpStatus.OK
        !response.body().tasks?.size()
    }


    void "should delete a workflow" () {
        given:
        def creator = new DomainCreator()
        User user
        Workflow workflow
        tx.withNewTransaction {
            user = creator.generateAllowedUser()
            workflow = creator.createWorkflow(owner: user)
            creator.createWorkflowMetrics(workflow)
        }
        
        when:
        String auth = doJwtLogin(user, client)
        def url = "/workflow/delete/${workflow.id}"
        def resp = client
                .toBlocking()
                .exchange( HttpRequest.DELETE(url).bearerAuth(auth) )

        then:
        resp.status == HttpStatus.NO_CONTENT
        and:
        tx.withNewTransaction { workflowService.get(workflow.id) } == null

    }

    void "should not delete a workflow" () {
        given:
        def creator = new DomainCreator()
        User user = creator.generateAllowedUser()

        when:
        String auth = doJwtLogin(user, client)
        def url = "/workflow/delete/1234"
        client
            .toBlocking()
            .exchange( HttpRequest.DELETE(url).bearerAuth(auth) )

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
        e.message == "Oops... Failed to delete workflow with ID 1234"

    }

    void 'should get workflow metrics' () {
        given:
        def creator = new DomainCreator(validate: false)
        def user = creator.generateAllowedUser()
        Workflow workflow = creator.createWorkflow(
                manifest: new WfManifest(defaultBranch: 'master'),
                stats: new WfStats(computeTimeFmt: '(a few seconds)'),
                nextflow: new WfNextflow(version: "19.05.0-TOWER"),
        )

        def metrics = [
                creator.createWorkflowMetrics(workflow),
                creator.createWorkflowMetrics(workflow)
        ]

        when: "perform the request to obtain the metrics"
        String auth = doJwtLogin(user, client)
        HttpResponse<GetWorkflowMetricsResponse> response = client
                .toBlocking()
                .exchange(
                    HttpRequest.GET("/workflow/metrics/${workflow.id}") .bearerAuth(auth),
                    GetWorkflowMetricsResponse )

        then:
        response.status == HttpStatus.OK
        response.body().metrics.size() == 2
        response.body().metrics[0].process == metrics[0].process
        response.body().metrics[1].process == metrics[1].process
    }

    void 'should return error message when metrics not found' () {
        given:
        def creator = new DomainCreator()
        def user = creator.generateAllowedUser()

        when: "perform the request to obtain the meticd"
        def auth = doJwtLogin(user, client)
        HttpResponse<GetWorkflowMetricsResponse> response = client
                .toBlocking()
                .exchange(
                        HttpRequest.GET("/workflow/metrics/123") .bearerAuth(auth),
                        GetWorkflowMetricsResponse )

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.NOT_FOUND
        e.message == "Oops... Can't find workflow ID 123"
    }

}
