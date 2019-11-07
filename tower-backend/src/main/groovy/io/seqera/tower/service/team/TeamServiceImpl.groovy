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

package io.seqera.tower.service.team

import javax.inject.Inject
import javax.inject.Singleton

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.seqera.tower.domain.Role
import io.seqera.tower.domain.Team
import io.seqera.tower.domain.User
import io.seqera.tower.service.UserService
import io.seqera.util.TupleUtils

/**
 * Implements operation on {@code Team} entity
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Singleton
@CompileStatic
class TeamServiceImpl implements TeamService {

    @Inject UserService userService

    @Override
    List<Team> findAllByUser(User user) {
        final args = TupleUtils.map('userId', user.id)
        return (List<Team>)Team.findAll('select u.teams from User u where u.id = :userId', args)
    }

    @Override
    @CompileDynamic
    Team create(String name, User user) {
        final team = new Team()
        team.name = name
        team.role = Role.find('select x.role from UserRole x where x.user = :user', TupleUtils.map('user',user))
        team.addToUsers(user)
        team.save(failOnError:true)
    }

    @Override
    boolean deleteById(Long teamId) {
        final args = TupleUtils.map('teamId', teamId)
        Team.executeUpdate("delete Team t where t.id=:teamId", args)>0
    }
}
