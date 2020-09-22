package io.seqera.tower.controller

import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.util.AbstractContainerBaseTest
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

import javax.inject.Inject

@MicronautTest(application = Application.class)
class TokenGeneratorControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    void "get generated token"() {
        when: "request random token"
        HttpResponse<String> response = client.toBlocking().exchange(
                HttpRequest.GET("/token/"), String.class
        )

        then: "the request has been successfully completed"
        response.status == HttpStatus.OK
    }
}
