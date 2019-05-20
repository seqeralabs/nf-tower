package io.seqera.mail

import io.micronaut.context.ApplicationContext
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class MailerConfigTest extends Specification {

    //@Shared @AutoCleanup EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    def 'should create smtp config' () {

        when:
        def mail = ApplicationContext.run('mail').getBean(MailerConfig)
        then:
        mail.from == 'me@google.com'
        mail.smtp.host == 'google.com'
        mail.smtp.user == 'mr-bean'
        mail.smtp.password == 'super-secret'
        mail.smtp.port == 587
        mail.smtp.auth == true
        mail.smtp.'starttls.enable' == true
        mail.smtp.'starttls.required' == true

    }


    def 'should get mail properties object' () {

        when:
        def props = ApplicationContext.run('mail').getBean(MailerConfig).getMailProperties()
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

    def 'should get proxy from system property' () {
        given:
        def config = Spy(MailerConfig)

        when:
        def props = config.getMailProperties()
        then:
        2 * config.sysProp('http.proxyHost') >> 'sys.proxy.name'
        1 * config.sysProp('http.proxyPort') >> '1234'
        then:
        props.'mail.smtp.proxy.host' == 'sys.proxy.name'
        props.'mail.smtp.proxy.port' == '1234'

    }

}


