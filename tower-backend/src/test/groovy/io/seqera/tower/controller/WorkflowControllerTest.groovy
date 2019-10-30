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

import io.micronaut.http.uri.UriBuilder
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.exchange.progress.GetProgressResponse
import spock.lang.Unroll

import javax.inject.Inject
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

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
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.User
import io.seqera.tower.domain.WfManifest
import io.seqera.tower.domain.WfNextflow
import io.seqera.tower.domain.WfStats
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowComment
import io.seqera.tower.exchange.task.TaskList
import io.seqera.tower.exchange.workflow.AddWorkflowCommentRequest
import io.seqera.tower.exchange.workflow.AddWorkflowCommentResponse
import io.seqera.tower.exchange.workflow.DeleteWorkflowCommentRequest
import io.seqera.tower.exchange.workflow.DeleteWorkflowCommentResponse
import io.seqera.tower.exchange.workflow.ListWorkflowCommentsResponse
import io.seqera.tower.exchange.workflow.GetWorkflowMetricsResponse
import io.seqera.tower.exchange.workflow.UpdateWorkflowCommentRequest
import io.seqera.tower.exchange.workflow.UpdateWorkflowCommentResponse
import io.seqera.tower.exchange.workflow.GetWorkflowResponse
import io.seqera.tower.exchange.workflow.ListWorklowResponse
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
        final user = creator.generateAllowedUser()
        Workflow workflow = creator.createWorkflow(
                complete: OffsetDateTime.now(),
                owner: user,
                manifest: new WfManifest(defaultBranch: 'master'),
                stats: new WfStats(computeTimeFmt: '(a few seconds)'),
                nextflow: new WfNextflow(version_: "19.05.0-TOWER", timestamp: Instant.now(), build: '19.01.1'),
        )

        creator.createWorkflowMetrics(workflow)
        creator.createWorkflowLoad(workflow: workflow)


        when: "perform the request to obtain the workflow"
        String accessToken = doJwtLogin(user, client)
        HttpResponse<GetWorkflowResponse> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/${workflow.id}")
                           .bearerAuth(accessToken),
                GetWorkflowResponse.class
        )

        then: "the workflow data is properly obtained"
        response.status == HttpStatus.OK
        response.body().workflow.id.size()>0
        response.body().workflow.stats
        response.body().workflow.nextflow
        response.body().workflow.manifest
        response.body().progress.workflowProgress
    }

    void "try to get a workflow as non-authenticated user"() {
        given: "a workflow"
        DomainCreator domainCreator = new DomainCreator()
        Workflow workflow = domainCreator.createWorkflow()

        when: "perform the request to obtain the workflow as an anonymous user"
        HttpResponse<GetWorkflowResponse> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/${workflow.id}"),
                GetWorkflowResponse.class
        )

        then: "the server responds UNAUTHORIZED"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
    }

    void "get the progress of a workflow"() {
        given: "a workflow with"
        DomainCreator creator = new DomainCreator()
        Workflow workflow = creator.createWorkflow()

        when: "perform the request to obtain the progress"
        String accessToken = doJwtLogin(creator.generateAllowedUser(), client)
        HttpResponse<GetProgressResponse> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/${workflow.id}/progress")
                        .bearerAuth(accessToken),
                GetProgressResponse.class
        )

        then: "the workflow progress has been obtained"
        response.status == HttpStatus.OK
        response.body().progress
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
        HttpResponse<ListWorklowResponse> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/list")
                           .bearerAuth(accessToken),
                ListWorklowResponse.class
        )

        expect: "the workflows data is properly obtained"
        response.status == HttpStatus.OK
        response.body().workflows.size() == workflows.size()

        and: 'the workflows are ordered by start date in descending order'
        response.body().workflows.workflow.id == workflows.reverse().id*.toString()
    }

    @Unroll
    void "get a list of filtered workflows with search=#search"() {
        given: 'a user owner of the workflow'
        User owner
        User.withNewTransaction {
            owner = new DomainCreator().generateAllowedUser()
        }

        and: "some workflows owned by the user and ordered by start date in descending order (recent first)"
        final now = OffsetDateTime.now()
        DomainCreator domainCreator = new DomainCreator()
        (1..4).collect { Integer i ->
            domainCreator.createWorkflow(
                    owner: owner,
                    projectName: "project${i}", runName: "runName${i}", commitId: "commitId${i}",
                    start: now.minusSeconds(i)
            )
        }

        and: 'some other workflows belonging to other users'
        5.times {
            domainCreator.createWorkflow()
        }

        and: 'build the filter URL'
        URI uri = UriBuilder.of("/workflow/list")
                            .queryParam('max', 10)
                            .queryParam('offset', 0)
                            .queryParam('search', search)
                  .build()

        and: "perform the request to obtain the workflows"
        String accessToken = doJwtLogin(owner, client)
        HttpResponse<ListWorklowResponse> response = client.toBlocking().exchange(
                HttpRequest.GET(uri)
                           .bearerAuth(accessToken),
                ListWorklowResponse.class
        )

        expect: "the workflows data is properly obtained"
        response.status == HttpStatus.OK
        response.body().workflows.size() == expectedWorkflowRunNames.size()
        response.body().workflows.workflow.runName == expectedWorkflowRunNames

        where: 'the search params are'
        search      | expectedWorkflowRunNames
        'project%'  | ["runName1", "runName2", "runName3", "runName4"]
        'runName%'  | ["runName1", "runName2", "runName3", "runName4"]
        'PrOjEct%'  | ["runName1", "runName2", "runName3", "runName4"]
        'rUnNAme%'  | ["runName1", "runName2", "runName3", "runName4"]
        'project1'  | ["runName1"]
        'runName1'  | ["runName1"]
        '%a%'       | ["runName1", "runName2", "runName3", "runName4"]
    }

    void "try to get a non-existing workflow"() {
        when: "perform the request to obtain a non-existing workflow"
        String accessToken = doJwtLogin(new DomainCreator().generateAllowedUser(), client)
        client.toBlocking().exchange(
                HttpRequest.GET("/workflow/100")
                        .bearerAuth(accessToken),
                GetWorkflowResponse.class
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

    @Unroll
    void "get the list of filtered tasks associated with a workflow"() {
        given: 'a task'
        Task firstTask = new DomainCreator().createTask(taskId: 1, status: TaskStatus.SUBMITTED, hash: "Hash1", tag: "Tag1", process: "Process1")

        and: 'extract its workflow'
        Workflow workflow = firstTask.workflow

        and: 'generate more tasks associated with the workflow'
        [TaskStatus.RUNNING, TaskStatus.FAILED, TaskStatus.COMPLETED].eachWithIndex { status, i ->
            Integer taskId = 2 + i
            new DomainCreator().createTask(workflow: workflow, status: status, taskId: taskId, hash: "Hash${taskId}", tag: "Tag${taskId}", process: "Process${taskId}")
        }

        and: 'build the filter URL'
        URI uri = UriBuilder.of("/workflow/${workflow.id}/tasks")
                            .queryParam('length', 10)
                            .queryParam('start', 0)
                            .queryParam('order[0][column]', orderProperty) //These two params are not being sent. More research required
                            .queryParam('order[0][dir]', orderDirection)
                            .queryParam('search', search)
                  .build()

        and: "perform the request to obtain the tasks of the workflow"
        String accessToken = doJwtLogin(new DomainCreator().generateAllowedUser(), client)
        HttpResponse<TaskList> response = client.toBlocking().exchange(
                HttpRequest.GET(uri)
                        .bearerAuth(accessToken),
                TaskList.class
        )

        expect: "the tasks data is properly obtained"
        response.status == HttpStatus.OK
        response.body().tasks.size() == expectedTaskIds.size()
        response.body().tasks.task.taskId == expectedTaskIds

        where: 'the search params are'
        search      | expectedTaskIds  | orderProperty | orderDirection
        'hash*'     | [1l, 2l, 3l, 4l] | 'hash'        | 'asc'
        'tag*'      | [1l, 2l, 3l, 4l] | 'process'     | 'asc'
        'process*'  | [1l, 2l, 3l, 4l] | 'tag'         | 'asc'

        'hash1'     | [1l]             | 'hash'        | 'asc'
        'HASH1'     | [1l]             | 'hash'        | 'asc'
        'process2'  | [2l]             | 'hash'        | 'asc'
        'PROCESS2'  | [2l]             | 'hash'        | 'asc'
        'tag3'      | [3l]             | 'hash'        | 'asc'
        'TAG3'      | [3l]             | 'hash'        | 'asc'

        'submit*'   | [1l]             | 'hash'        | 'asc'
        'SUBMITTED' | [1l]             | 'hash'        | 'asc'
        'submitted' | [1l]             | 'hash'        | 'asc'
        'run*'      | [2l]             | 'hash'        | 'asc'
        'fail*'     | [3l]             | 'hash'        | 'asc'
        'comp*'     | [4l]             | 'hash'        | 'asc'
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
        def now = OffsetDateTime.now()
        def creator = new DomainCreator()
        User user
        Workflow workflow
        tx.withNewTransaction {
            user = creator.generateAllowedUser()
            workflow = creator.createWorkflow(owner: user)
            creator.createWorkflowMetrics(workflow)
            new WorkflowComment(author: user, text: 'Hello', workflow: workflow, dateCreated: now, lastUpdated: now).save(failOnError:true)
        }
        
        when:
        String auth = doJwtLogin(user, client)
        def url = "/workflow/${workflow.id}"
        def resp = client
                .toBlocking()
                .exchange( HttpRequest.DELETE(url).bearerAuth(auth) )

        then:
        resp.status == HttpStatus.NO_CONTENT
        and:
        tx.withNewTransaction { workflowService.get(workflow.id) }.deleted

    }

    void "should not delete a workflow" () {
        given:
        def creator = new DomainCreator()
        User user = creator.generateAllowedUser()

        when:
        String auth = doJwtLogin(user, client)
        def url = "/workflow/1234"
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
                nextflow: new WfNextflow(version_: "19.05.0-TOWER"),
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
                    HttpRequest.GET("/workflow/${workflow.id}/metrics") .bearerAuth(auth),
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
                        HttpRequest.GET("/workflow/123/metrics") .bearerAuth(auth),
                        GetWorkflowMetricsResponse )

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.NOT_FOUND
        e.message == "Oops... Can't find workflow ID 123"
    }


    void 'should get workflow comments' () {
        given:
        def creator = new DomainCreator(validate: false)
        def user = creator.generateAllowedUser()
        Workflow workflow = creator.createWorkflow()

        def t0 = OffsetDateTime.now()
        WorkflowComment.withNewTransaction {
            new WorkflowComment(
                    author: user,
                    text: 'First hello',
                    workflow: workflow,
                    dateCreated: t0,
                    lastUpdated: t0,
                    )
                    .save(failOnError:true)

            new WorkflowComment(
                    author: user,
                    text: 'Second hello',
                    workflow: workflow,
                    dateCreated: t0.plusMinutes(5),
                    lastUpdated: t0.plusMinutes(5),
                    )
                    .save(failOnError:true)
        }

        when: "perform the request to obtain the comments"
        String auth = doJwtLogin(user, client)
        HttpResponse<ListWorkflowCommentsResponse> response = client
                .toBlocking()
                .exchange(
                        HttpRequest.GET("/workflow/${workflow.id}/comments") .bearerAuth(auth),
                        ListWorkflowCommentsResponse )

        then:
        response.status == HttpStatus.OK
        response.body().comments.size() == 2
        response.body().comments[0].text == 'Second hello'
        response.body().comments[1].text == 'First hello'
    }

    def 'should add a workflow comment' () {
        given:
        def creator = new DomainCreator(validate: false)
        def user = creator.generateAllowedUser()
        Workflow workflow = creator.createWorkflow()

        when: "perform the request to obtain the comments"
        String auth = doJwtLogin(user, client)
        def ts =  OffsetDateTime.now()
        def req = new AddWorkflowCommentRequest(text: 'Great job', timestamp: ts)
        def post = HttpRequest.POST("/workflow/${workflow.id}/comment/add", req)
        def resp = client
                .toBlocking()
                .exchange( post.bearerAuth(auth), AddWorkflowCommentResponse )

        then:
        resp.status == HttpStatus.OK
        resp.body().commentId != null

        and:
        workflowService.getComments(workflow).size() ==1
        workflowService.getComments(workflow)[0].id == resp.body().commentId
    }

    def 'should update a workflow comment' () {
        given:
        def creator = new DomainCreator(validate: false)
        def user = creator.generateAllowedUser()
        Workflow workflow = creator.createWorkflow()

        def t0 = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusMinutes(10)
        def comment = tx.withNewTransaction {
            new WorkflowComment(
                    author: user,
                    text: 'First comment',
                    workflow: workflow,
                    dateCreated: t0,
                    lastUpdated: t0 )
                    .save(failOnError:true)
        }

        when: "perform the request to obtain the comments"
        def COMMENT = 'The new comment'
        String auth = doJwtLogin(user, client)
        def ts =  OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        def req = new UpdateWorkflowCommentRequest(commentId: comment.id, text: COMMENT, timestamp: ts)
        def put = HttpRequest.PUT("/workflow/${workflow.id}/comment", req)
        def resp = client
                .toBlocking()
                .exchange( put.bearerAuth(auth), UpdateWorkflowCommentResponse )

        then:
        resp.status == HttpStatus.OK

        and:
        workflowService.getComments(workflow).size() ==1
        workflowService.getComments(workflow)[0].text == COMMENT
        workflowService.getComments(workflow)[0].dateCreated == t0
        workflowService.getComments(workflow)[0].lastUpdated == ts
    }

    def 'should delete a workflow comment' () {
        given:
        def creator = new DomainCreator(validate: false)
        def user = creator.generateAllowedUser()
        Workflow workflow = creator.createWorkflow()

        def t0 = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusMinutes(10)
        def comment1 = tx.withNewTransaction {
            new WorkflowComment(
                    author: user,
                    text: 'First comment',
                    workflow: workflow,
                    dateCreated: t0,
                    lastUpdated: t0 )
                    .save(failOnError:true)
        }

        def comment2 = tx.withNewTransaction {
            new WorkflowComment(
                    author: user,
                    text: 'Second comment',
                    workflow: workflow,
                    dateCreated: t0,
                    lastUpdated: t0 )
                    .save(failOnError:true)
        }

        when: "perform the request to obtain the comments"
        String auth = doJwtLogin(user, client)
        def ts =  OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        def req = new DeleteWorkflowCommentRequest(commentId: comment1.id, timestamp: ts)
        def delete = HttpRequest.DELETE("/workflow/${workflow.id}/comment", req)
        def resp = client
                .toBlocking()
                .exchange( delete.bearerAuth(auth), DeleteWorkflowCommentResponse )

        then:
        resp.status == HttpStatus.OK

        and:
        // comment1 has been deleted
        tx.withNewTransaction { WorkflowComment.get(comment1.id) } == null

        // comment2 still here
        tx.withNewTransaction { WorkflowComment.get(comment2.id) } != null
    }
}
