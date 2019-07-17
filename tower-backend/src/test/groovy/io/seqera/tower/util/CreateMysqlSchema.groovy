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

package io.seqera.tower.util

import io.micronaut.context.ApplicationContext
import io.seqera.tower.Application
import io.seqera.tower.domain.User
import org.hibernate.Session
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.wait.strategy.Wait
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class CreateMysqlSchema  {

    static void main(String[] args) {
        final env = [MYSQL_ROOT_PASSWORD: 'root', MYSQL_USER: 'tower', MYSQL_PASSWORD: 'tower', MYSQL_DATABASE: 'tower']

        def container = new FixedHostPortGenericContainer("mysql:5.6")
                .withFixedExposedPort(3307, 3306)
                .withEnv(env)
                .waitingFor(Wait.forListeningPort())
                .waitingFor(Wait.forLogMessage(/MySQL init process done.*/, 1))

        container.start()

        def ctx = ApplicationContext.build(Application, 'mysql-create').start()
        try {
            User.withNewTransaction {User.count()} == 0
        }
        finally {
            ctx.stop()
        }

    }

    static printTableNames() {
        User.withNewSession { Session session ->
            session
                    .createSQLQuery("select t.table_name from information_schema.tables t where t.table_schema = 'tower'")
                    .list()
                    .sort()
                    .each { println it }
        }
    }

}