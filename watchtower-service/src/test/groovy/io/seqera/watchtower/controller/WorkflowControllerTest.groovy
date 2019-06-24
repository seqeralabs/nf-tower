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

package io.seqera.watchtower.controller

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.*
import io.seqera.watchtower.pogo.exchange.task.TaskList
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowGet
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowList
import io.seqera.watchtower.util.AbstractContainerBaseTest
import io.seqera.watchtower.util.DomainCreator

import javax.inject.Inject
import java.time.Instant

@MicronautTest(application = Application.class)
@Transactional
class WorkflowControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    void "get a workflow"() {
        given: "a workflow with some summaries"
        DomainCreator domainCreator = new DomainCreator()
        Workflow workflow = domainCreator.createWorkflow(
            manifest: new Manifest(defaultBranch: 'master'),
            stats: new Stats(computeTimeFmt: '(a few seconds)'),
            nextflow: new NextflowMeta(nextflowVersion: "19.05.0-TOWER"),
            summaryEntries: [domainCreator.createSummaryEntry(), domainCreator.createSummaryEntry()],
            tasksProgress: new TasksProgress(running: 0, submitted: 0, failed: 0, pending: 0, succeeded: 0, cached: 0)
        )

        and: "perform the request to obtain the workflow"
        String accessToken = doJwtLogin(domainCreator.generateAllowedUser(), client)
        HttpResponse<WorkflowGet> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/${workflow.id}")
                           .bearerAuth(accessToken),
                WorkflowGet.class
        )

        expect: "the workflow data is properly obtained"
        response.status == HttpStatus.OK
        response.body().workflow.workflowId == workflow.id.toString()
        response.body().workflow.stats
        response.body().workflow.nextflow
        response.body().workflow.manifest
        response.body().summary.size() == 2
        response.body().progress
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
                    start: Instant.now().plusSeconds(i),
                    summaryEntries: [domainCreator.createSummaryEntry(), domainCreator.createSummaryEntry()],
                    tasksProgress: new TasksProgress(running: 0, submitted: 0, failed: 0, pending: 0, succeeded: 0, cached: 0)
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



}
