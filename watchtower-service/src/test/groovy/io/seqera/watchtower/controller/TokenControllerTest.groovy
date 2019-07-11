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

import javax.inject.Inject

import grails.gorm.transactions.TransactionService
import grails.gorm.transactions.Transactional
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.AccessToken
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.exchange.token.CreateAccessTokenRequest
import io.seqera.watchtower.exchange.token.CreateAccessTokenResponse
import io.seqera.watchtower.exchange.token.ListAccessTokensResponse
import io.seqera.watchtower.service.AccessTokenService
import io.seqera.watchtower.util.AbstractContainerBaseTest
import io.seqera.watchtower.util.DomainCreator
import io.seqera.watchtower.util.DomainHelper

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(application = Application.class)
@Transactional
class TokenControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject
    TransactionService tx

    @Inject
    AccessTokenService tokenService

    def 'should return the list of tokens owned by the user' () {
        given:
        User owner = tx.withNewTransaction { new DomainCreator().generateAllowedUser() }

        when:
        String accessToken = doJwtLogin(owner, client)
        HttpResponse<ListAccessTokensResponse> response = client.toBlocking().exchange(
                HttpRequest.GET("/token/list") .bearerAuth(accessToken),
                ListAccessTokensResponse.class
        )

        then:
        response.status == HttpStatus.OK
        response.body().tokens.size() == 1

    }

    def 'should return an empty list' () {
        given:
        User owner = tx.withNewTransaction {
            def u = new DomainCreator().generateAllowedUser();
            tokenService.deleteByUser(u)
            return u
        }

        when:
        String accessToken = doJwtLogin(owner, client)
        HttpResponse<ListAccessTokensResponse> response = client.toBlocking().exchange(
                HttpRequest.GET("/token/list") .bearerAuth(accessToken),
                ListAccessTokensResponse.class
        )

        then:
        response.status == HttpStatus.OK
        response.body().tokens.size() == 0

    }


    def 'should return the list of tokens as json' () {
        given:
        User owner = tx.withNewTransaction { new DomainCreator().generateAllowedUser() }

        when:
        String accessToken = doJwtLogin(owner, client)
        def  response = client.toBlocking().retrieve( HttpRequest.GET("/token/list") .bearerAuth(accessToken)  )

        println JsonOutput.prettyPrint(response)

        then:
        response

    }

    def 'should delete a token by id' () {
        given:
        User user
        AccessToken token
        tx.withNewTransaction {
            user = new DomainCreator().generateAllowedUser()
            token = tokenService.createToken('foo',user)
        }


        when:
        String auth = doJwtLogin(user, client)
        def url = "/token/delete/${token.id}"
        def resp = client
                .toBlocking()
                .exchange( HttpRequest.DELETE(url).bearerAuth(auth) )

        then:
        resp.status == HttpStatus.NO_CONTENT

        and:
        // default token still exists
        tx.withNewTransaction { tokenService.getByNameAndUser('default', user) } != null

    }


    def 'should delete all user tokens' () {
        given:
        User user1 = tx.withNewTransaction { new DomainCreator().createUser() }
        and:
        User user2 = tx.withNewTransaction {
            def u=new DomainCreator().generateAllowedUser()
            tokenService.createToken('foo',u)
            tokenService.createToken('bar',u)
            return u
        }

        expect:
        tx.withNewTransaction {tokenService.countByUser(user2)} == 3

        when:
        String auth = doJwtLogin(user2, client)
        def resp = client
                .toBlocking()
                .exchange( HttpRequest.DELETE('/token/delete-all').bearerAuth(auth) )

        then:
        resp.status == HttpStatus.NO_CONTENT

        and:
        tx.withNewTransaction {tokenService.countByUser(user2)} == 0
        tx.withNewTransaction {tokenService.countByUser(user1)} == 1
  
    }


    def 'should create token' () {
        given:
        User user = tx.withNewTransaction { new DomainCreator().generateAllowedUser() }

        when:
        String auth = doJwtLogin(user, client)
        def req = HttpRequest.POST('/token/create', new CreateAccessTokenRequest(name: 'foo'))
        HttpResponse<CreateAccessTokenResponse> resp = client
                .toBlocking()
                .exchange( req.bearerAuth(auth), CreateAccessTokenResponse )

        then:
        resp.status == HttpStatus.OK
        resp.body().token.name == 'foo'

        and:
        tokenService.getByNameAndUser('foo', user ).id == resp.body().token.id
    }

    def 'should *not* create token' () {
        given:
        User user = tx.withNewTransaction { new DomainCreator().generateAllowedUser() }

        when:
        // tries to create with a name already used `default`
        String auth = doJwtLogin(user, client)
        def req = HttpRequest.POST('/token/create', new CreateAccessTokenRequest(name: 'default'))
        client
                .toBlocking()
                .exchange( req.bearerAuth(auth), CreateAccessTokenResponse  )

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
        e.message == "An access token with name 'default' already exists"

        and:
        tokenService.countByUser(user) == 1
    }

    def 'should serialise empty list' () {

        given:
        def list = new ListAccessTokensResponse(tokens: [])

        when:
        def json = DomainHelper.toJson(list)
        println json
        then:
        new JsonSlurper().parseText(json).tokens == []

        when:
        def resp = DomainHelper.mapper.readValue(json, ListAccessTokensResponse)
        then:
        resp.tokens == []
    }

}
