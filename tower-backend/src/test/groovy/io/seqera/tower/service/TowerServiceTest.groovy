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
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest
class TowerServiceTest extends Specification {

    @Inject
    ApplicationContext ctx

    @Property(name = 'tower.serverUrl', value = 'https://tower.nf')
    def 'should get default api endpoint' () {
        given:
        def twr = ctx.getBean(TowerService)
        
        expect:
        twr.getTowerApiEndpoint() == 'https://api.tower.nf'
        twr.getTowerApiEndpointEmptyDefault() == null
    }

    @Property(name = 'tower.serverUrl', value = 'http://foo.custom.org')
    def 'should get custom api endpoint' () {
        given:
        def twr = ctx.getBean(TowerService)

        expect:
        twr.getTowerApiEndpoint() == 'http://foo.custom.org/api'
        twr.getTowerApiEndpointEmptyDefault() == 'http://foo.custom.org/api'
    }


    def 'should read build info' () {
        given:
        def service = new TowerServiceImpl()
        when:
        def p = service.readBuildInfo()
        then:
        p.containsKey('version')
    }

    def 'should get login path' () {
        given:
        def service = new TowerServiceImpl()

        when:
        def ctx = ApplicationContext.run()
        def result = service.getLoginPath(ctx)
        then:
        result == '/login'
    }

    def 'should get openid login path' () {
        given:
        def service = new TowerServiceImpl()
        when:
        def props = [
                TOWER_OIDC_CLIENT: 'a',
                TOWER_OIDC_SECRET: 'b',
                TOWER_OIDC_ISSUER: 'c' ]
        def ctx = ApplicationContext.run(props, 'auth-oidc')
        def result = service.getLoginPath(ctx)
        then:
        result == '/oauth/login/oidc'
    }

    def 'should get default login path' () {
        given:
        def service = new TowerServiceImpl()
        when:
        def props = [
                TOWER_OIDC_CLIENT: 'a',
                TOWER_OIDC_SECRET: 'b',
                TOWER_OIDC_ISSUER: 'c',
                TOWER_GITHUB_CLIENT: 'x',
                TOWER_GITHUB_SECRET: 'y',
        ]
        def ctx = ApplicationContext.run(props, 'auth-oidc', 'auth-github')
        def result = service.getLoginPath(ctx)
        then:
        result == '/login'
    }

    def 'should not get auth types' () {
        given:
        def service = new TowerServiceImpl()
        when:
        def ctx = ApplicationContext.run()
        def result = service.getAuthTypes(ctx)
        then:
        result == []
    }

    def 'should get auth types' () {
        given:
        def service = new TowerServiceImpl()
        when:
        def ctx = ApplicationContext.run('auth-foo','auth-bar')
        def result = service.getAuthTypes(ctx)
        then:
        result == ['foo','bar']
    }
}
