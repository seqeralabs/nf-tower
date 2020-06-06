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

import groovy.util.logging.Slf4j
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.seqera.tower.exchange.serviceinfo.ServiceInfoResponse
import io.seqera.tower.service.TowerService

/**
 * Provide backend system info
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller('/')
class ServiceInfoController extends BaseController {

    @Inject TowerService towerService

    @Get('/service-info')
    HttpResponse<ServiceInfoResponse> info() {
        try{
            final resp = towerService.getServiceInfo()
            log.trace "Service info=$resp"
            HttpResponse.ok(new ServiceInfoResponse(resp))
        }
        catch (Throwable t) {
            handle(t, ServiceInfoResponse)
        }
    }

    @Get("/ping")
    HttpResponse<String> ping(HttpRequest req) {
        log.info "Trace ping from ${req.remoteAddress}"
        HttpResponse.ok('pong')
    }

}
