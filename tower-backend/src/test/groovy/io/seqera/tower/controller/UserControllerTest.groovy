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
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.User
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator

@MicronautTest(application = Application.class)
@Transactional
class UserControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client


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
