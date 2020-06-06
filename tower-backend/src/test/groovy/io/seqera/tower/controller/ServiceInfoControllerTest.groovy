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
import io.seqera.tower.exchange.serviceinfo.ServiceInfoResponse
import io.seqera.tower.service.TowerService
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest
class ServiceInfoControllerTest extends Specification {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject TowerService tower

    def 'should get service info' ( ){
        when:
        def response = client
                .toBlocking()
                .exchange( HttpRequest.GET("/service-info"), ServiceInfoResponse.class )

        then:
        response.status == HttpStatus.OK
        and:
        response.body().serviceInfo == tower.serviceInfo
        and:
        response.body().serviceInfo.loginPath == '/login'
        response.body().serviceInfo.authTypes == []
    }



    def 'should get ping reply' ( ){
        when:
        def response = client
                .toBlocking()
                .exchange( HttpRequest.GET("/ping"), String )

        then:
        response.status == HttpStatus.OK
        and:
        response.body() == 'pong'
    }

}
