package io.seqera.watchtower.controller

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.auth.User
import io.seqera.watchtower.util.AbstractContainerBaseTest
import spock.lang.Ignore

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
@Ignore("throws 'IllegalStateException: state should be: open' when executing all tests")
class UserControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client


    void "register a user given an email"() {
        given: 'a valid email'
        String email = 'user@email.com'

        when: 'send the register request'
        HttpResponse response = client.toBlocking().exchange(
                HttpRequest.POST('/user/register', new UsernamePasswordCredentials(email, null)),
                String.class
        )

        then: 'the user has been registered successfully'
        response.status == HttpStatus.OK
        response.body() == 'User registered!'
        User.count() == 1
        User.first().email == email
        User.first().username
        User.first().authToken
    }

    void "register a user, then register the same user again"() {
        given: 'a valid email'
        String email = 'user@email.com'

        when: 'send the register request'
        HttpResponse response = client.toBlocking().exchange(
                HttpRequest.POST('/user/register', new UsernamePasswordCredentials(email, null)),
                String.class
        )

        then: 'the user has been registered successfully'
        response.status == HttpStatus.OK
        response.body() == 'User registered!'
        User.count() == 1
        User.first().email == email
        User.first().username
        User.first().authToken

        when: 'register the same user again'
        response = client.toBlocking().exchange(
                HttpRequest.POST('/user/register', new UsernamePasswordCredentials(email, null)),
                String.class
        )

        then: 'a new user has not been created'
        response.status == HttpStatus.OK
        response.body() == 'User registered!'
        User.count() == 1
    }

    void "try to register a user given a bad email"() {
        given: 'a bad email'
        String email = 'badEmail'

        when: 'send the register request'
        client.toBlocking().exchange(
                HttpRequest.POST('/user/register', new UsernamePasswordCredentials(email, null)),
                String.class
        )

        then: 'the user has not been registered'
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
        e.response.body() == "Can't save a user with bad email format"
        User.count() == 0
    }

}
