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

package io.seqera.tower.exchange.gate

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonValue
import groovy.transform.CompileStatic
import io.seqera.tower.domain.User
import io.seqera.tower.exchange.BaseResponse
/**
 * Model the response for gate controller access
 *
 * see {@link io.seqera.tower.controller.GateController#access(io.seqera.tower.exchange.gate.AccessGateRequest)}
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
@JsonIgnoreProperties(['user'])
class AccessGateResponse implements BaseResponse {

    static enum State {
        LOGIN_ALLOWED,       // registration ok, sign-in email sent
        PENDING_APPROVAL,    // first time registration, login needs to be approved
        KEEP_CALM_PLEASE     // second time registration, login is not yet approved, just wait

        @JsonValue
        int toValue() { return ordinal() }
    }


    String message
    State state
    User user
}
