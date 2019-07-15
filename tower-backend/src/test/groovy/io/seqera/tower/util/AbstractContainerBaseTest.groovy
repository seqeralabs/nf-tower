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

import groovy.util.logging.Slf4j
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.seqera.tower.domain.User
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import spock.lang.Specification

@Slf4j
abstract class AbstractContainerBaseTest extends Specification {

    static GenericContainer DATABASE_CONTAINER
    static boolean isMySql = System.getenv('MICRONAUT_ENVIRONMENTS')=='mysql'

    static {
        if( isMySql ) {
            log.info "++++ Testing with MYSQL ++++"
            DATABASE_CONTAINER = createMySqlDatabase()
            DATABASE_CONTAINER.start()
        }
    }

    private static GenericContainer createMySqlDatabase() {
        new FixedHostPortGenericContainer("mysql:5.6")
                .withFixedExposedPort(3307, 3306)
                .withEnv([MYSQL_ROOT_PASSWORD: 'root', MYSQL_USER: 'tower', MYSQL_PASSWORD: 'tower', MYSQL_DATABASE: 'tower'])
                .waitingFor(Wait.forListeningPort())
                .waitingFor(Wait.forLogMessage(/MySQL init process done.*/, 1))
    }

    protected String doJwtLogin(User user, HttpClient client) {
        HttpRequest request = HttpRequest.create(HttpMethod.POST, '/login')
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(new UsernamePasswordCredentials(user.email, user.authToken))
        HttpResponse<AccessRefreshToken> response = client.toBlocking().exchange(request, AccessRefreshToken)

        response.body.get().accessToken
    }

    void cleanup() {
        DomainCreator.cleanupDatabase()
    }
}
