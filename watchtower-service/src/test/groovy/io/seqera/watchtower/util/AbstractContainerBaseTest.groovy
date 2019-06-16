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

package io.seqera.watchtower.util

import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.seqera.watchtower.domain.User
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import spock.lang.Specification

abstract class AbstractContainerBaseTest extends Specification {

    static GenericContainer DATABASE_CONTAINER

    static {
        createMySqlDatabase()

        DATABASE_CONTAINER.start()
    }

    private static createMySqlDatabase() {
        DATABASE_CONTAINER = new FixedHostPortGenericContainer("mysql:8.0")
                .withFixedExposedPort(3307, 3306)
                .withEnv([MYSQL_ROOT_PASSWORD: 'root', MYSQL_USER: 'watchtower', MYSQL_PASSWORD: 'watchtower', MYSQL_DATABASE: 'watchtower'])
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

    protected HttpRequest appendBasicAuth(User user, MutableHttpRequest request) {
        request.basicAuth(user.userName, user.accessTokens.first().token)
    }

    void cleanup() {
        DomainCreator.cleanupDatabase()
    }
}
