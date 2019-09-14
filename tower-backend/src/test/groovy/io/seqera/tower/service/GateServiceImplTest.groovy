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

import javax.inject.Inject

import io.micronaut.context.ApplicationContext
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.domain.User
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest
class GateServiceImplTest extends Specification {

    @Inject
    ApplicationContext context

    def 'should bind properties' () {
        given:
        GateServiceImpl userService = context.getBean(GateServiceImpl)
        expect:
        userService.appName == 'Nextflow Tower'
        userService.serverUrl == 'http://localhost:8000'
    }

    def 'should load text template' () {
        given:
        def binding = [
                user: 'Mr Bean',
                app_name: 'Nextflow Tower',
                auth_url: 'https://tower.com/login?d78a8ds',
                contact_email: 'support@foo.com',
                server_url:'http://host.com']
        def service = Spy(GateServiceImpl)
        when:
        def text = service.getTextTemplate(binding)
        then:
        text.contains('https://tower.com/login?d78a8ds')
        text.contains('http://host.com')
    }

    def 'should load html template' () {
        given:
        def binding = [
                user: 'Mr Bean',
                app_name: 'Nextflow Tower',
                auth_url: 'https://tower.com/login?1234',
                contact_email: 'support@foo.com',
                server_url:'https://tower.nf']
        def service = Spy(GateServiceImpl)
        when:
        def text = service.getHtmlTemplate(binding)
        then:
        text.contains('https://tower.com/login?1234')
        text.contains('support@foo.com')
    }

    def 'should load logo attachment' () {
        given:
        def service = new GateServiceImpl()
        when:
        def attach = service.getLogoAttachment()
        then:
        attach.resource == '/io/seqera/tower/service/tower-logo.png'
        attach.contentId == '<tower-logo>'
        attach.disposition == 'inline'
        then:
        this.class.getResource(attach.resource) != null
    }



    def 'should encode url' () {
        given:
        def service = new GateServiceImpl ()
        service.serverUrl = 'http://host.com'

        expect:
        service.buildAccessUrl(new User(email:EMAIL, authToken: 'abc')) == EXPECTED

        where:
        EMAIL               | EXPECTED
        'yo@gmail.com'      | 'http://host.com/auth?email=yo%40gmail.com&authToken=abc'
        'yo+xyz@gmail.com'  | 'http://host.com/auth?email=yo%2Bxyz%40gmail.com&authToken=abc'
    }

    def 'should get user enable url' () {
        given:
        def service = new GateServiceImpl ()

        when:
        def url = service.getEnableUrl(SERVER, USER_ID)
        then:
        url == EXPECTED

        where:
        SERVER                      | USER_ID       | EXPECTED
        'http://localhost'          | 1             | 'http://localhost:8001/user?id=1'
        'http://localhost:8000'     | 2             | 'http://localhost:8001/user?id=2'
        'http://foo.com'            | 3             | 'http://admin.foo.com/user?id=3'
    }
}
