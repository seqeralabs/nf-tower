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

        createMongoDatabase()
//        createPostgreSqlDatabase()

        DATABASE_CONTAINER.start()
    }

    private static createMongoDatabase() {
//        DATABASE_CONTAINER = new GenericContainer("mongo:4.1")
//                .withExposedPorts(27017)
//                .waitingFor(Wait.forListeningPort())
//        System.setProperty('MONGO_PORT', DATABASE_CONTAINER.getMappedPort(27017).toString())
        DATABASE_CONTAINER = new FixedHostPortGenericContainer("mongo:4.1")
                .withFixedExposedPort(27018, 27017)
                .waitingFor(Wait.forListeningPort())
    }

    private static createPostgreSqlDatabase() {
        DATABASE_CONTAINER = new FixedHostPortGenericContainer("postgres:11.3")
                .withFixedExposedPort(5432, 5432)
                .withEnv([POSTGRES_USER: 'watchtower', POSTGRES_PASSWORD: 'watchtower', POSTGRES_DB: 'watchtower'])
                .waitingFor(Wait.forListeningPort())
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
