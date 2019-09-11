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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Error
import io.seqera.tower.exchange.MessageResponse
/**
 * Implements a base controller class with common helpers
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
abstract class BaseController {

    @Error
    HttpResponse handleException(HttpRequest request, Exception e) {

        final err =
                "Oops.. something went wrong\n" +
                "- request: [${request.method}] ${request.uri}\n" +
                "- params : ${request.parameters.collect { k,v -> "$k=$v"}}\n" +
                "- user   : ${request.userPrincipal.isPresent() ? request.userPrincipal.get().getName() : '-'}\n" +
                "- message: [${e.getClass().getName()}] ${e.message ?: e.toString()}\n"
        log.error(err,e)
        final msg = "Oops.. something went wrong -- ${e.message?:e.toString()}"
        return HttpResponse.badRequest(new MessageResponse(msg))
    }

}
