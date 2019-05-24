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
import io.seqera.mail.MailerConfig
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.auth.User
import io.seqera.watchtower.util.AbstractContainerBaseTest
import org.subethamail.wiser.Wiser
import spock.lang.Ignore

import javax.inject.Inject
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMultipart

@MicronautTest(application = Application.class)
@Transactional
@Ignore("throws 'IllegalStateException: state should be: open' when executing all tests")
class UserControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject
    MailerConfig mailerConfig

    Wiser smtpServer

    void setup() {
        smtpServer = new Wiser(mailerConfig.smtp.port)
        smtpServer.start()
    }

    void cleanup() {
        smtpServer.stop()
    }


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
        response.body() == 'User registered! Check your mailbox!'
        User.count() == 1
        User.first().email == email
        User.first().username
        User.first().authToken

        and: "the access link was sent to the user"
        smtpServer.messages.size() == 1
        Message message = smtpServer.messages.first().mimeMessage
        message.allRecipients.contains(new InternetAddress(User.first().email))
        message.subject == 'NF-Tower Access Link'
        (message.content as MimeMultipart).getBodyPart(0).content.getBodyPart(0).content.contains('You can access NF-Tower')
        (message.content as MimeMultipart).getBodyPart(0).content.getBodyPart(0).content.contains('http')
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
        response.body() == 'User registered! Check your mailbox!'
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
        response.body() == 'User registered! Check your mailbox!'
        User.count() == 1

        and: "the access link was sent to the user two times"
        smtpServer.messages.size() == 2
        smtpServer.messages.mimeMessage.every { it.allRecipients.contains(new InternetAddress(User.first().email)) }
        smtpServer.messages.mimeMessage.every { it.subject == 'NF-Tower Access Link' }
        smtpServer.messages.mimeMessage.every { (it.content as MimeMultipart).getBodyPart(0).content.getBodyPart(0).content.contains('You can access NF-Tower') }
        smtpServer.messages.mimeMessage.every { (it.content as MimeMultipart).getBodyPart(0).content.getBodyPart(0).content.contains('http') }
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
