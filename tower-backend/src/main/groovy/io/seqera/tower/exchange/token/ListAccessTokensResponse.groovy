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

package io.seqera.tower.exchange.token

import groovy.transform.CompileStatic
import io.seqera.tower.domain.AccessToken
import io.seqera.tower.exchange.BaseResponse

/**
 * Model a list of {@link AccessToken} owned by the user
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class ListAccessTokensResponse implements BaseResponse{
    List<AccessToken> tokens
    String message
}
