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

package io.seqera.tower.exchange.team

import io.seqera.tower.domain.Team
import io.seqera.tower.exchange.BaseResponse

/**
 * Retrieve user's {@link Team}
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class ListTeamsResponse implements BaseResponse {
    String message
    List<Team> teams
}
