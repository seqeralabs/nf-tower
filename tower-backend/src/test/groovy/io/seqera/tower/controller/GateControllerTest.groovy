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
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.User
import io.seqera.tower.exchange.gate.AccessGateRequest
import io.seqera.tower.exchange.gate.AccessGateResponse
import io.seqera.tower.service.mail.MailServiceImpl
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(application = Application.class, environments = ['trusted-test'])
@Transactional
class GateControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    @Value('${tower.contact-email}')
    String contactEmail

    @Inject MailServiceImpl mailService

    def setup() {
        mailService.pendingMails.clear()
    }

    void "register a user given an email"() {
        given: 'a valid email'
        String email = 'user@email.com'

        when: 'send the access request'
        def post = HttpRequest.POST('/gate/access', new AccessGateRequest(email:email))
        HttpResponse resp = client.toBlocking().exchange( post, AccessGateResponse )

        then: 'the user has been registered successfully'
        resp.status == HttpStatus.OK
        resp.body().state == AccessGateResponse.State.PENDING_APPROVAL

        User registeredUser = User.list().first()
        registeredUser.email == email
        registeredUser.userName
        !registeredUser.authToken

        and: "the access link was sent to the user"
        mailService.pendingMails.size() == 1
        mailService.pendingMails[0].to == contactEmail
    }

    void "register a user, then register the same user again"() {
        given: 'a valid email'
        String EMAIL = 'user@email.com'
        new DomainCreator().createUser(email: EMAIL, trusted: false, authToken: null)

        when: 'register the same user again'
        def post = HttpRequest.POST('/gate/access', new AccessGateRequest(EMAIL))
        def response = client.toBlocking().exchange(post, AccessGateResponse)

        then: 'a new user has not been created'
        response.status == HttpStatus.OK
        response.body() .state == AccessGateResponse.State.KEEP_CALM_PLEASE
        User.count() == 1
        User registeredUser = User.list().first()
        registeredUser.email == EMAIL
        registeredUser.userName
        !registeredUser.authToken

        and: "no mail was sent"
        mailService.pendingMails.size() == 1
        mailService.pendingMails[0].to == contactEmail

    }

    void "register a user, no approval need"() {
        given: 'a valid email'
        String EMAIL = 'user@email.com'
        new DomainCreator().createUser(email: EMAIL, trusted: true, authToken: null)

        when:
        def post = HttpRequest.POST('/gate/access', new AccessGateRequest(EMAIL))
        def response = client.toBlocking().exchange(post, AccessGateResponse)

        then:
        response.status == HttpStatus.OK
        response.body() .state == AccessGateResponse.State.LOGIN_ALLOWED
        User.count() == 1
        User registeredUser = User.list().first()
        registeredUser.email == EMAIL
        registeredUser.userName
        registeredUser.authToken
        registeredUser.authTime

        and:
        mailService.pendingMails.size() == 1
        mailService.pendingMails[0].to == EMAIL

    }

    void "try to register a user given a bad email"() {
        given: 'a bad email'
        String email = 'badEmail'

        when: 'send the register request'
        def post = HttpRequest.POST('/gate/access', new AccessGateRequest(email))
        client.toBlocking().exchange(post, AccessGateResponse)

        then: 'the user has not been registered'
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
        e.message == "Can't save a user with bad email format"
        and:
        mailService.pendingMails.size() == 0

    }

}
