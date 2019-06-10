package io.seqera.watchtower.util

import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
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

    protected String doLogin(User user, HttpClient client) {
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
