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

import io.micronaut.http.HttpStatus
import io.seqera.tower.exceptions.TowerException
import io.seqera.tower.exchange.BaseResponse
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class BaseControllerTest extends Specification {

    static class FakeResponse implements BaseResponse {
        String message
    }

    def 'should handle error' () {
        given:
        def ctrl = new BaseController() { }
        def tower = new TowerException("Oops") {  }

        when:
        def resp = ctrl.handle(tower, FakeResponse)
        then:
        resp.body().message == 'Oops'
        resp.status() == HttpStatus.BAD_REQUEST


        when:
        resp = ctrl.handle(new Exception("This is an error"), FakeResponse)
        then:
        resp.body().message.startsWith 'This is an error - Error ID:'
        resp.status() == HttpStatus.BAD_REQUEST
    }
}
