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

package io.seqera.tower.domain

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Transactional
@MicronautTest(application = Application.class)
class WorkflowLoadTest extends AbstractContainerBaseTest {

    def 'should save and load list of executors' () {
        given:
        def creator = new DomainCreator()
        def load = creator.createWorkflowLoad(executors: ['alpha','beta','delta'])

        when:
        def record = WorkflowLoad.get( load.id )
        then:
        record.executors ==  ['alpha','beta','delta']

    }
}
