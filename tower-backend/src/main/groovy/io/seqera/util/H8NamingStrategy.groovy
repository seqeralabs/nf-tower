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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.hibernate.cfg.ImprovedNamingStrategy
import org.hibernate.cfg.NamingStrategy
/**
 * Prefix all DB tables with 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class H8NamingStrategy implements NamingStrategy {

    @Delegate
    private NamingStrategy target = ImprovedNamingStrategy.INSTANCE

    @Override
    String classToTableName(final String className) {
        final result = 'tw_' + target.classToTableName(className)
        log.trace "Mapping entity class=$className to table=$result"
        return result
    }

}
