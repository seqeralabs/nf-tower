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

import io.seqera.tower.exchange.user.GetUserStatusResponse
import spock.lang.Unroll

import javax.inject.Inject

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.User
import io.seqera.tower.exchange.user.EnableUserResponse
import io.seqera.tower.exchange.user.GetUserResponse
import io.seqera.tower.exchange.user.ListUserResponse
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator

@MicronautTest(application = Application.class)
@Transactional
class UserControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client


    void "get data of the logged in user"() {
        given: "an existing user"
        User user = new DomainCreator().createUserWithRole(
                [email: 'email@email.com', userName: 'username',
                 firstName: 'Firstname', lastName: 'Lastname', organization: 'Organization', description: 'Description', avatar: 'http://avatar.com'],
                'ROLE_USER')

        when: "perform the request to obtain the info the user"
        String accessToken = doJwtLogin(user, client)
        HttpResponse<GetUserResponse> response = client.toBlocking().exchange(
                HttpRequest.GET("/user/")
                        .bearerAuth(accessToken),
                GetUserResponse.class
        )

        then: 'the user info has been obtained properly'
        response.status == HttpStatus.OK
        response.body().user.id == user.id
        response.body().user.email == user.email
        response.body().user.userName == user.userName
        response.body().user.firstName == user.firstName
        response.body().user.lastName == user.lastName
        response.body().user.organization == user.organization
        response.body().user.description == user.description
        response.body().user.avatar == user.avatar
    }

    @Unroll
    void "get the status of a user"() {
        given: "an existing user (disabled or not)"
        User user = new DomainCreator().createUserWithRole([disabled: disabled], 'ROLE_USER')

        when: "perform the request to obtains the status"
        String accessToken = doJwtLogin(user, client)
        HttpResponse<GetUserStatusResponse> response = client.toBlocking().exchange(
                HttpRequest.GET("/user/status")
                           .bearerAuth(accessToken),
                GetUserStatusResponse.class
        )

        then: 'the user has been updated'
        response.status == HttpStatus.OK
        response.body().disabled == disabled

        where: 'the user status is'
        disabled << [true, false, null]
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

    def 'should list users' () {
        given: "an existing user"
        User admin = new DomainCreator().createUserWithRole([:], 'ROLE_USER')
        User u1 = new DomainCreator().createUser()
        User u2 = new DomainCreator().createUser()

        when:
        String accessToken = doJwtLogin(admin, client)
        def req = HttpRequest.GET("/user/list")
        def resp = client.toBlocking().exchange(req.bearerAuth(accessToken), ListUserResponse)
        then:
        resp.status() == HttpStatus.OK
        resp.body().users.size()==3


        when:
        req = HttpRequest.GET("/user/list?max=1")
        resp = client.toBlocking().exchange(req.bearerAuth(accessToken), ListUserResponse)
        then:
        resp.status() == HttpStatus.OK
        resp.body().users.size()==1
    }

    def 'should allow user login' () {
        given: "an existing user"
        User admin = new DomainCreator().createUserWithRole([:], 'ADMIN')
        User user = new DomainCreator().createUser(email: 'foo@gmail.com',trusted: false)

        when:
        String accessToken = doJwtLogin(admin, client)
        def req = HttpRequest.GET("/user/allow/login/${user.id}")
        def resp = client.toBlocking().exchange(req.bearerAuth(accessToken), EnableUserResponse)
        then:
        resp.status() == HttpStatus.OK

        and:
        user.refresh().trusted
    }

    def 'should get a user' () {
        given: "an existing user"
        User admin = new DomainCreator().createUserWithRole([:], 'ADMIN')
        User user = new DomainCreator().createUser(email: 'foo@gmail.com',trusted: false)

        when:
        String accessToken = doJwtLogin(admin, client)
        def req = HttpRequest.GET("/user/get/${user.id}")
        def resp = client.toBlocking().exchange(req.bearerAuth(accessToken), GetUserResponse)
        then:
        resp.status() == HttpStatus.OK
        and:
        resp.body().user.id == user.id
        resp.body().user.email == user.email

    }

}
