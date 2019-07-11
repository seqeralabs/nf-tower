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

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.security.token.jwt.validator.JwtTokenValidator
import io.micronaut.test.annotation.MicronautTest
import io.reactivex.Flowable
import io.seqera.tower.Application
import io.seqera.tower.domain.User
import io.seqera.tower.domain.UserRole
import io.seqera.tower.service.auth.AuthenticationProviderByAuthToken
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator

import javax.inject.Inject
import java.time.Instant

@MicronautTest(application = Application.class)
@Transactional
class LoginControllerTest extends AbstractContainerBaseTest {

    @Inject
    JwtTokenValidator tokenValidator
    @Inject
    AuthenticationProviderByAuthToken authenticationProviderByAuthToken

    @Inject
    @Client('/')
    RxHttpClient client

    void 'login with valid credentials (email and authToken) for a user'() {
        given: "a user"
        User user = new DomainCreator().createUser(firstName: 'User', lastName: 'Userson', avatar: 'http://image.com', organization: 'org', description: 'description')

        and: "grant a role to the user"
        UserRole userRole = new DomainCreator().createUserRole(user: user)

        when: "do the login request attaching userName and authToken as credentials"
        HttpRequest request = HttpRequest.create(HttpMethod.POST, '/login')
                                         .accept(MediaType.APPLICATION_JSON_TYPE)
                                         .body(new UsernamePasswordCredentials(user.email, user.authToken))
        HttpResponse<AccessRefreshToken> response = client.toBlocking().exchange(request, AccessRefreshToken)

        then:
        response.status == HttpStatus.OK
        response.body.get().accessToken
        response.body.get().refreshToken

        when:
        String accessToken = response.body.get().accessToken
        Authentication authentication = Flowable.fromPublisher(tokenValidator.validateToken(accessToken)).blockingFirst()

        then:
        authentication.attributes
        authentication.attributes.roles == [userRole.role.authority]
        authentication.attributes.iss
        authentication.attributes.exp
        authentication.attributes.iat

        authentication.attributes.id == user.id
        authentication.attributes.email == user.email
        authentication.attributes.userName == user.userName
        authentication.attributes.accessToken == user.accessTokens.first().token
        authentication.attributes.firstName == user.firstName
        authentication.attributes.lastName == user.lastName
        authentication.attributes.avatar == user.avatar
        authentication.attributes.organization == user.organization
        authentication.attributes.description == user.description
    }

    void 'try to login with valid credentials (email and authToken) for a user which has an expired authToken'() {
        given: "a user"
        User user = new DomainCreator().createUser(authTime: Instant.now().minus(authenticationProviderByAuthToken.authMailDuration))

        when: "do the login request attaching userName and authToken as credentials"
        HttpRequest request = HttpRequest.create(HttpMethod.POST, '/login')
                                         .accept(MediaType.APPLICATION_JSON_TYPE)
                                         .body(new UsernamePasswordCredentials(user.email, user.authToken))
        client.toBlocking().exchange(request, AccessRefreshToken)

        then: "the server responds UNAUTHORIZED"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
    }

    void 'try to login without supplying credentials'() {
        when: "do the login request"
        HttpRequest request = HttpRequest.create(HttpMethod.POST, '/login')
                                         .accept(MediaType.APPLICATION_JSON_TYPE)
        client.toBlocking().exchange(request)

        then: "the server responds BAD REQUEST"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
    }

    void 'try to login supplying a bad combination of credentials (username and authToken)'() {
        given: 'a user'
        User user = new DomainCreator().createUser()

        when: "do the login request attaching a bad authToken"
        HttpRequest request = HttpRequest.create(HttpMethod.POST, '/login')
                                          .accept(MediaType.APPLICATION_JSON_TYPE)
                                          .body(new UsernamePasswordCredentials(user.email, 'badToken'))
        client.toBlocking().exchange(request, AccessRefreshToken)

        then: "the server responds UNAUTHORIZED"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
    }

}
