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

package io.seqera.tower.controller

import javax.inject.Inject

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.exchange.serviceinfo.ServiceInfoResponse
import io.seqera.tower.util.AbstractContainerBaseTest
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(application = Application, environments = ['auth-oidc-test'])
class ServiceInfoControllerOpenidTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    def 'should get service info' () {
        given:

        when:
        def response = client
                .toBlocking()
                .exchange(HttpRequest.GET("/service-info"), ServiceInfoResponse)

        then:
        response.status == HttpStatus.OK
        with(response.body().serviceInfo) {
            loginPath == '/oauth/login/oidc'
            authTypes == ['oidc-test']
        }
    }


}
