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

import grails.gorm.transactions.TransactionService
import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.Team
import io.seqera.tower.domain.User
import io.seqera.tower.exchange.team.CreateTeamRequest
import io.seqera.tower.exchange.team.CreateTeamResponse
import io.seqera.tower.exchange.team.DeleteTeamResponse
import io.seqera.tower.exchange.team.ListTeamsResponse
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(application = Application.class)
@Transactional
class TeamControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject
    TransactionService tx

    def 'should return list of teams' () {
        given:
        def creator = new DomainCreator()
        User owner = tx.withNewTransaction {
            def user = creator.generateAllowedUser()
            def teamY = creator.createTeam(name:'Team Y', users:[user])
            def teamZ = creator.createTeam(name:'Team Z', users:[user])

            return user
        }

        when:
        String accessToken = doJwtLogin(owner, client)
        HttpResponse<ListTeamsResponse> response = client.toBlocking().exchange(
                HttpRequest.GET("/team") .bearerAuth(accessToken),
                ListTeamsResponse.class )

        then:
        response.status == HttpStatus.OK
        response.body().teams.size() == 2

    }

    def 'should create a team' () {
        given:
        def creator = new DomainCreator()
        def user = creator.generateAllowedUser()

        when:
        String auth = doJwtLogin(user, client)
        def req = HttpRequest.POST('/team', new CreateTeamRequest(name: 'foo'))
        HttpResponse<CreateTeamResponse> resp = client
                .toBlocking()
                .exchange( req.bearerAuth(auth), CreateTeamResponse )

        then:
        resp.status() == HttpStatus.OK
        resp.body().team.id > 0
        resp.body().team.name == 'foo'
        and:
        tx.withTransaction { Team.count() } == 1
    }

    def 'should delete a team' () {
        given:
        def creator = new DomainCreator()
        User user
        Team team
        tx.withNewTransaction {
            user = creator.generateAllowedUser()
            team = creator.createTeam(name:'Team X', users:[user])
        }

        when:
        String accessToken = doJwtLogin(user, client)
        HttpResponse<DeleteTeamResponse> response = client.toBlocking().exchange(
                HttpRequest.DELETE("/team/${team.id}") .bearerAuth(accessToken),
                DeleteTeamResponse.class )
        then:
        response.status() == HttpStatus.OK
        and:
        tx.withTransaction { Team.get(team.id) } == null

    }
}
