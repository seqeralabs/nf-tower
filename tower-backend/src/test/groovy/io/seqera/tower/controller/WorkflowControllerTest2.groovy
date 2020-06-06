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

import javax.inject.Inject

import grails.gorm.transactions.Transactional
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.exchange.task.TaskList
import io.seqera.tower.exchange.workflow.ListWorkflowResponse
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator

@MicronautTest(application = Application.class)
@Transactional
@Property(name='tower.workflow.list.max-allowed', value = '110')
@Property(name='tower.workflow.tasks.max-allowed', value = '200')
class WorkflowControllerTest2 extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    def 'should not allow more than max workflows' () {
        given:
        def creator = new DomainCreator()
        def owner = creator.createAllowedUser()

        and: "perform the request to obtain the workflows"
        String login = doJwtLogin(owner, client)

        when:
        client.toBlocking().exchange(
                HttpRequest.GET("/workflow/list?max=1000") .bearerAuth(login), ListWorkflowResponse )

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
        e.message == "Workflow list max parameter cannot be greater than 110 (current value=1000)"
    }

    def 'should not allow more than max tasks' () {
        given:
        def creator = new DomainCreator()
        def owner = creator.createAllowedUser()

        and: "perform the request to obtain the workflows"
        String login = doJwtLogin(owner, client)

        when:
        client.toBlocking().exchange(
                HttpRequest.GET("/workflow/1234/tasks?length=1000") .bearerAuth(login), TaskList )

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
        e.message == "Workflow tasks length parameter cannot be greater than 200 (current value=1000)"
    }
}
