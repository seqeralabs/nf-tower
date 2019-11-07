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
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.seqera.tower.domain.Team
import io.seqera.tower.exchange.team.CreateTeamRequest
import io.seqera.tower.exchange.team.CreateTeamResponse
import io.seqera.tower.exchange.team.DeleteTeamResponse
import io.seqera.tower.exchange.team.ListTeamsResponse
import io.seqera.tower.service.UserService
import io.seqera.tower.service.team.TeamService
/**
 * Team management controller
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Controller("/team")
@Transactional
@CompileStatic
@Secured(['ROLE_USER'])
class TeamController extends BaseController {

    @Inject TeamService teamService
    @Inject UserService userService

    @Get("/")
    HttpResponse<ListTeamsResponse> list(Authentication auth) {
        final user = userService.getByAuth(auth)
        List<Team> teams = teamService.findAllByUser(user)
        HttpResponse.ok(new ListTeamsResponse(teams: teams))
    }

    @Post("/")
    HttpResponse<CreateTeamResponse> create(CreateTeamRequest request, Authentication auth) {
        final user = userService.getByAuth(auth)
        if( !user )
            return HttpResponse.badRequest(new CreateTeamResponse(message: "Missing authenticated user"))

        final team = teamService.create(request.name, user)
        if( !team )
            return HttpResponse.badRequest(new CreateTeamResponse(message: "Failed to create team"))

        HttpResponse.ok(new CreateTeamResponse(team: team))
    }

    @Delete("/{teamId}")
    HttpResponse<DeleteTeamResponse> delete(Long teamId) {
        // TODO add check on permission to delete team
        final result = teamService.deleteById(teamId)
        (result
                ? HttpResponse.ok(new DeleteTeamResponse())
                : HttpResponse.badRequest(new DeleteTeamResponse(message: "Unable to delete with Id=$teamId")))
    }
}
