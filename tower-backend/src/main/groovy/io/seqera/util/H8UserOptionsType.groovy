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

package io.seqera.util

import io.seqera.tower.domain.UserOptions
import io.seqera.util.H8AbstractJsonType

/**
 * Implements a custom Hibernate type for class {@link UserOptions}
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class H8UserOptionsType extends H8AbstractJsonType<UserOptions> {

}
