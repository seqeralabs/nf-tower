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

package io.seqera.tower.domain

import groovy.transform.CompileDynamic

/**
 * Model workflow stats
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileDynamic
class WfStats {

    String computeTimeFmt

    Integer cachedCount
    Integer failedCount
    Integer ignoredCount
    Integer succeedCount

    String cachedCountFmt
    String succeedCountFmt
    String failedCountFmt
    String ignoredCountFmt

    Float cachedPct
    Float failedPct
    Float succeedPct
    Float ignoredPct

    Long cachedDuration
    Long failedDuration
    Long succeedDuration

    static constraints = {
        computeTimeFmt(nullable: true, maxSize: 50)
        cachedCount(nullable: true)
        failedCount(nullable: true)
        ignoredCount(nullable: true)
        succeedCount(nullable: true)
        cachedCountFmt(nullable: true)
        succeedCountFmt(nullable: true)
        failedCountFmt(nullable: true)
        ignoredCountFmt(nullable: true)
        cachedPct(nullable: true)
        failedPct(nullable: true)
        succeedPct(nullable: true)
        ignoredPct(nullable: true)
        cachedDuration(nullable: true)
        failedDuration(nullable: true)
        succeedDuration(nullable: true)
    }

}
