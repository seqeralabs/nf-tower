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
import javax.mail.internet.InternetAddress

import grails.gorm.transactions.Transactional
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.seqera.mail.MailerConfig
import io.seqera.tower.Application
import io.seqera.tower.domain.User
import io.seqera.tower.exchange.gate.AccessGateRequest
import io.seqera.tower.exchange.gate.AccessGateResponse
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
import org.subethamail.wiser.Wiser
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(application = Application.class)
@Transactional
class GateControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject
    MailerConfig mailerConfig

    @Value('${tower.contact-email}')
    String contactEmail

    Wiser smtpServer

    void setup() {
        smtpServer = new Wiser(mailerConfig.smtp.port)
        smtpServer.start()
    }

    void cleanup() {
        smtpServer.stop()
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
        smtpServer.messages.size() == 1
        smtpServer.messages.first().mimeMessage.allRecipients.contains(new InternetAddress(contactEmail))
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
        smtpServer.messages.size() == 0

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
        smtpServer.messages.size() == 1
        smtpServer.messages.first().mimeMessage.allRecipients.contains(new InternetAddress(EMAIL))

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
        User.withNewTransaction { User.count() } == 0

    }

}
