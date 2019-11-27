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

package io.seqera.tower.enums

enum LiveAction {

    WORKFLOW_UPDATE, PROGRESS_UPDATE;

    // weird hack, to get the action rendered properly
    // when in the log output, w/o it's reported as `LiveAction()`
    String toString() { super.toString() }

}
