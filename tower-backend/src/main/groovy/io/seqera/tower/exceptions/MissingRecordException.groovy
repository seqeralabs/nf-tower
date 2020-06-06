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

package io.seqera.tower.exceptions
/**
 * Exception thrown when an entity record does not exist
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class MissingRecordException extends TowerException {

    MissingRecordException(Class type, String key) {
        super("Unable to find record with type: ${type.getSimpleName()} and key: $key")
    }
}
