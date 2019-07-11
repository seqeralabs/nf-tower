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

package io.seqera.tower.service

import io.seqera.mail.Attachment
import io.seqera.mail.Mail
import io.seqera.tower.domain.User
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class UserServiceImplTest extends Specification {

    def 'should load text template' () {
        given:
        def binding = [
                user: 'Mr Bean',
                app_name: 'Nextflow Tower',
                auth_url: 'https://tower.com/login?d78a8ds',
                frontend_url:'http://host.com']
        def service = Spy(UserServiceImpl)
        when:
        def text = service.getTextTemplate(binding)
        then:
        text.contains('Hi Mr Bean,')
        text.contains('https://tower.com/login?d78a8ds')
        text.contains('http://host.com')
        text.contains('This email was sent by Nextflow Tower')
    }

    def 'should load html template' () {
        given:
        def binding = [
                user: 'Mr Bean',
                app_name: 'Nextflow Tower',
                auth_url: 'https://tower.com/login?1234',
                frontend_url:'https://tower.nf']
        def service = Spy(UserServiceImpl)
        when:
        def text = service.getHtmlTemplate(binding)
        then:
        text.contains('Hi Mr Bean,')
        text.contains('href="https://tower.com/login?1234"')
        text.contains('https://tower.nf')
        text.contains('This email was sent by Nextflow Tower')
    }

    def 'should load logo attachment' () {
        given:
        def service = new UserServiceImpl()
        when:
        def attach = service.getLogoAttachment()
        then:
        attach.resource == '/io/seqera/tower/service/seqera-logo.png'
        attach.params.contentId == '<seqera-logo>'
        attach.params.disposition == 'inline'
        then:
        this.class.getResource(attach.resource) != null
    }


    def 'should send auth email' () {
        given:
        def TEXT_TPL = 'mail text template'
        def HTML_TPL = 'mail text template'
        def ATTACH = new Attachment(new File('LOGO'))
        def RECIPIENT = 'alice@domain.com'
        def LINK = 'http://domain.com/link?register'
        def HOST = 'http://foo.com'
        def user = new User(email: RECIPIENT)
        def mailer = Mock(MailService)
        def service = Spy(UserServiceImpl)
        service.mailService = mailer
        service.frontendUrl = HOST
        service.appName = 'Nextflow Tower'

        when:
        service.sendAccessEmail(user)

        then:
        1 * service.buildAccessUrl(user) >> LINK
        1 * service.getTextTemplate(_) >> { Map binding -> assert binding.auth_url==LINK; assert binding.frontend_url==HOST; TEXT_TPL }
        1 * service.getHtmlTemplate(_) >> { Map binding -> assert binding.auth_url==LINK; assert binding.frontend_url==HOST;HTML_TPL }
        1 * service.getLogoAttachment() >> ATTACH
        1 * mailer.sendMail(_ as Mail) >> { Mail mail ->
            assert mail.subject == 'Nextflow Tower Sign in'
            assert mail.to == RECIPIENT
            assert mail.text == TEXT_TPL
            assert mail.body == HTML_TPL
            assert mail.attachments == [ATTACH]
        }

    }

    def 'should encode url' () {
        given:
        def service = new UserServiceImpl(frontendUrl: 'http://host.com')

        expect:
        service.buildAccessUrl(new User(email:EMAIL, authToken: 'abc')) == EXPECTED

        where:
        EMAIL               | EXPECTED
        'yo@gmail.com'      | 'http://host.com/auth?email=yo%40gmail.com&authToken=abc'
        'yo+xyz@gmail.com'  | 'http://host.com/auth?email=yo%2Bxyz%40gmail.com&authToken=abc'
    }
}
