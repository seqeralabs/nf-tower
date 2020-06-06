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
import io.seqera.util.CompactUuid
import io.seqera.tower.exceptions.TowerException
import io.seqera.tower.exchange.BaseResponse
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

        final msg = "Oops.. something went wrong -- ${e.message?:e.toString()}"
        try {
            final err =
                    "Oops.. something went wrong\n" +
                            "- request: [${request.method}] ${request.uri}\n" +
                            "- params : ${request.parameters?.asMap()?.collect { k,v -> "$k=$v"}}\n" +
                            "- user   : ${request.userPrincipal.isPresent() ? request.userPrincipal.get().getName() : '-'}\n" +
                            "- message: [${e.getClass().getName()}] ${e.message ?: e.toString()}\n"
            log.error(err,e)
        }
        catch(Throwable t) {
            log.error("Damn.. something went really wrong | ${e}", t)
        }
        return HttpResponse.badRequest(new MessageResponse(msg))
    }


    def <R extends BaseResponse> HttpResponse<R> handle (Throwable t, Class<R> responseType) {
        final resp = responseType.newInstance()
        def msg = t.message
        if( t instanceof TowerException && msg ) {
            log.warn msg
            (resp as GroovyObject).setProperty('message', msg)
        }
        else {
            msg = t.message ?: t.cause?.message ?: "Oops .. unable to process request"
            msg += " - Error ID: ${CompactUuid.generate()}"
            (resp as GroovyObject).setProperty('message', msg)
            log.error(msg, t)
        }

        return HttpResponse.badRequest(resp)
    }

}
