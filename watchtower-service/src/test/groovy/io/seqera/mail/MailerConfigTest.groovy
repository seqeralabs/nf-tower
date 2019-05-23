package io.seqera.mail

import io.micronaut.context.ApplicationContext
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest
class MailerConfigTest extends Specification {

    @Inject
    MailerConfig mailerConfig


    void 'should create smtp config' () {
        expect:
        mailerConfig.from == 'me@google.com'
        mailerConfig.smtp.host == 'google.com'
        mailerConfig.smtp.user == 'mr-bean'
        mailerConfig.smtp.password == 'super-secret'
        mailerConfig.smtp.port == 587
        mailerConfig.smtp.auth == true
        mailerConfig.smtp.'starttls.enable' == true
        mailerConfig.smtp.'starttls.required' == true
    }


    void 'should get mail properties object' () {
        when:
        def props = mailerConfig.mailProperties

        then:
        props.'mail.smtp.host' == 'google.com'
        props.'mail.smtp.user' == 'mr-bean'
        props.'mail.smtp.password' == 'super-secret'
        props.'mail.smtp.port' == '587'
        props.'mail.smtp.auth' == 'true'
        props.'mail.smtp.starttls.enable' == 'true'
        props.'mail.smtp.starttls.required' == 'true'
        props.'mail.smtp.proxy.host' == 'proxy.com'
        props.'mail.smtp.proxy.port' == '5566'
        props.'mail.transport.protocol' == 'smtp'
    }

    void 'should get proxy from system property' () {
        given:
        def config = Spy(MailerConfig)

        when:
        def props = config.mailProperties

        then:
        2 * config.sysProp('http.proxyHost') >> 'sys.proxy.name'
        1 * config.sysProp('http.proxyPort') >> '1234'
        then:
        props.'mail.smtp.proxy.host' == 'sys.proxy.name'
        props.'mail.smtp.proxy.port' == '1234'

    }

}


