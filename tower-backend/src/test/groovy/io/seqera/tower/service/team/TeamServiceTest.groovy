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

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(application = Application)
@Transactional
class TeamServiceTest extends AbstractContainerBaseTest {

    @Inject
    TeamService teamService

    def 'should find teams for given user' () {
        given:
        def creator = new DomainCreator()
        and:
        def user1 = creator.createUser(email: 'foo@host.com')
        def teamX = creator.createTeam(name: 'Team X', users: [user1])

        and:
        def user2 = creator.createUser(email: 'bar@host.com')
        def teamY = creator.createTeam(name:'Team Y', users:[user2])
        def teamZ = creator.createTeam(name:'Team Z', users:[user2])

        when:
        def result1 = teamService.findAllByUser(user1)
        then:
        result1.size() ==1
        result1[0].name == teamX.name

        when:
        def result2 = teamService.findAllByUser(user2)
        then:
        result2.size() ==2
        result2 *.name == ['Team Y','Team Z']

    }
}
