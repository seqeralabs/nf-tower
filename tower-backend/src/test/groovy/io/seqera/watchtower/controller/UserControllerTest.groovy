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
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.test.annotation.MicronautTest
import io.seqera.mail.MailerConfig
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.util.AbstractContainerBaseTest
import io.seqera.watchtower.util.DomainCreator
import org.subethamail.wiser.Wiser

import javax.inject.Inject
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMultipart

@MicronautTest(application = Application.class)
@Transactional
class UserControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject
    MailerConfig mailerConfig

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

        when: 'send the register request'
        HttpResponse response = client.toBlocking().exchange(
                HttpRequest.POST('/user/register', new UsernamePasswordCredentials(email, null)),
                String.class
        )

        then: 'the user has been registered successfully'
        response.status == HttpStatus.OK
        response.body() == 'User registered! Check your mailbox!'
        User registeredUser = User.list().first()
        registeredUser.email == email
        registeredUser.userName
        registeredUser.authToken

        and: "the access link was sent to the user"
        smtpServer.messages.size() == 1
        Message message = smtpServer.messages.first().mimeMessage
        message.allRecipients.contains(new InternetAddress(registeredUser.email))
        message.subject == 'Nextflow Tower Sign in'
        (message.content as MimeMultipart).getBodyPart(0).content.getBodyPart(0).content.contains("Hi ${registeredUser.userName}")
        (message.content as MimeMultipart).getBodyPart(0).content.getBodyPart(0).content.contains('http')
    }

    void "register a user, then register the same user again"() {
        given: 'a valid email'
        String email = 'user@email.com'

        when: 'send the register request'
        HttpResponse response = client.toBlocking().exchange(
                HttpRequest.POST('/user/register', new UsernamePasswordCredentials(email, null)),
                String.class
        )

        then: 'the user has been registered successfully'
        response.status == HttpStatus.OK
        response.body() == 'User registered! Check your mailbox!'
        User.count() == 1
        User registeredUser = User.list().first()
        registeredUser.email == email
        registeredUser.userName
        registeredUser.authToken

        when: 'register the same user again'
        response = client.toBlocking().exchange(
                HttpRequest.POST('/user/register', new UsernamePasswordCredentials(email, null)),
                String.class
        )

        then: 'a new user has not been created'
        response.status == HttpStatus.OK
        response.body() == 'User registered! Check your mailbox!'
        User.withNewTransaction {
            User.count() == 1
        }

        and: "the access link was sent to the user two times"
        smtpServer.messages.size() == 2
        smtpServer.messages.mimeMessage.every { it.allRecipients.contains(new InternetAddress(registeredUser.email)) }
        smtpServer.messages.mimeMessage.every { it.subject == 'Nextflow Tower Sign in' }
        smtpServer.messages.mimeMessage.every { (it.content as MimeMultipart).getBodyPart(0).content.getBodyPart(0).content.contains("Hi ${registeredUser.userName}") }
        smtpServer.messages.mimeMessage.every { (it.content as MimeMultipart).getBodyPart(0).content.getBodyPart(0).content.contains('http') }
    }

    void "try to register a user given a bad email"() {
        given: 'a bad email'
        String email = 'badEmail'

        when: 'send the register request'
        client.toBlocking().exchange(
                HttpRequest.POST('/user/register', new UsernamePasswordCredentials(email, null)),
                String.class
        )

        then: 'the user has not been registered'
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
        e.response.body() == "Can't save a user with bad email format"
        User.withNewTransaction {
            User.count() == 0
        }

    }

    void "update the user data"() {
        given: "an existing user"
        User user = new DomainCreator().createUserWithRole([:], 'ROLE_USER')

        and: 'some new data encapsulated in a user object'
        User userData = new DomainCreator(save: false).createUser(userName: 'user', firstName: 'User', lastName: 'Userson', avatar: 'https://i.pravatar.cc/200', organization: 'Org', description: 'Desc')

        when: "perform the request to update the data"
        String accessToken = doJwtLogin(user, client)
        HttpResponse<String> response = client.toBlocking().exchange(
                HttpRequest.POST("/user/update", userData)
                           .bearerAuth(accessToken),
                String.class
        )

        then: 'the user has been updated'
        response.status == HttpStatus.OK
        response.body() == 'User successfully updated!'
    }

    void "try to update the user data, but give some invalid inputs"() {
        given: "an existing user"
        User user = new DomainCreator().createUserWithRole([:], 'ROLE_USER')

        and: 'some new data encapsulated in a user object'
        User userData = new DomainCreator(save: false).createUser(avatar: 'badUrl')

        when: "perform the request to update the data"
        String accessToken = doJwtLogin(user, client)
        HttpResponse<String> response = client.toBlocking().exchange(
                HttpRequest.POST("/user/update", userData)
                        .bearerAuth(accessToken),
                String.class
        )

        then: 'the user has not been updated'
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
        e.response.body() == "Can't save a user with bad avatar URL format"
        User.withNewTransaction {
            User.count() == 1
        }
    }

    void "delete a user"() {
        given: "an existing user"
        User user = new DomainCreator().createUserWithRole([:], 'ROLE_USER')

        when: "perform the request to delete the user"
        String accessToken = doJwtLogin(user, client)
        HttpResponse<String> response = client.toBlocking().exchange(
                HttpRequest.DELETE("/user/delete")
                           .bearerAuth(accessToken),
                String.class
        )

        then: 'the user has been deleted'
        response.status == HttpStatus.OK
        response.body() == 'User successfully deleted!'
    }

}
